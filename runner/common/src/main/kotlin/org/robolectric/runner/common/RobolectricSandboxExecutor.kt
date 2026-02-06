package org.robolectric.runner.common

import java.lang.reflect.Method
import java.util.concurrent.Callable
import org.robolectric.internal.AndroidSandbox

/**
 * Centralized executor for running test code within a Robolectric sandbox.
 *
 * This class encapsulates the common pattern of:
 * 1. Creating a sandbox using [SandboxLifecycleManager]
 * 2. Running on the sandbox's main thread
 * 3. Executing test logic within the sandbox context
 * 4. Proper cleanup and error handling
 *
 * This eliminates code duplication across different test engine implementations (JUnit Platform,
 * JUnit Jupiter, Kotest).
 *
 * ## Usage
 *
 * ```kotlin
 * val executor = RobolectricSandboxExecutor(lifecycleManager)
 *
 * executor.executeSandboxed(testClass, testMethod) { sandbox ->
 *   // Test logic here - runs in sandbox with proper setup
 *   val testInstance = createTestInstance(sandbox, testClass)
 *   invokeTestMethod(testInstance, testMethod)
 * }
 * ```
 *
 * ## Advanced Usage with Full Lifecycle
 *
 * ```kotlin
 * val result = executor.executeTest(
 *   testClass = MyTest::class.java,
 *   testMethod = testMethod,
 *   beforeEachAnnotations = listOf(BeforeEach::class.java),
 *   afterEachAnnotations = listOf(AfterEach::class.java),
 * ) { context ->
 *   // Test code runs here with context available
 * }
 * ```
 *
 * @property lifecycleManager The lifecycle manager for creating and managing sandboxes
 */
@ExperimentalRunnerApi
class RobolectricSandboxExecutor(private val lifecycleManager: SandboxLifecycleManager) {

  /**
   * Executes a test block within a properly configured Robolectric sandbox.
   *
   * This method:
   * 1. Creates a sandbox for the given test class/method
   * 2. Runs the test block on the sandbox main thread
   * 3. Handles sandbox lifecycle (setup/teardown)
   * 4. Propagates test exceptions properly
   *
   * @param testClass The test class being executed
   * @param testMethod The specific test method (optional, for method-level configuration)
   * @param testName Human-readable test name for logging/debugging
   * @param block The test logic to execute within the sandbox
   * @throws Exception Any exception thrown by the test block
   */
  fun executeSandboxed(
    testClass: Class<*>,
    testMethod: java.lang.reflect.Method? = null,
    testName: String = testMethod?.name ?: testClass.simpleName,
    block: (AndroidSandbox) -> Unit,
  ) {
    // Create sandbox using lifecycle manager
    val sandboxContext =
      if (testMethod != null) {
        lifecycleManager.createSandbox(testClass, testMethod)
      } else {
        lifecycleManager.createSandbox(testClass)
      }

    val sandbox = sandboxContext.sandbox
    val startTime = System.currentTimeMillis()
    var success = false

    // Log test start
    RunnerLogger.logTestStart(testClass.simpleName, testName, sandboxContext.sdk.apiLevel)

    try {
      // Execute on sandbox main thread with timing
      sandbox.runOnMainThread(
        Callable<Void?> {
          RunnerMetrics.timed(RunnerMetrics.PHASE_TEST_EXECUTION) {
            lifecycleManager.executeInSandbox(sandboxContext, testName) { block(sandbox) }
          }
          null
        }
      )
      success = true
    } catch (e: java.lang.reflect.InvocationTargetException) {
      // Unwrap reflection exceptions to get the actual test failure
      throw e.targetException ?: e
    } finally {
      val duration = System.currentTimeMillis() - startTime
      RunnerLogger.logTestEnd(testClass.simpleName, testName, duration, success)
      RunnerMetrics.recordTestExecution(success)
    }
  }

  /**
   * Executes a test block and returns a result, useful for engines that need to capture
   * success/failure without throwing exceptions.
   *
   * @param testClass The test class being executed
   * @param testMethod The specific test method (optional)
   * @param testName Human-readable test name
   * @param block The test logic to execute
   * @return ExecutionResult with success status and optional error
   */
  fun executeSandboxedSafe(
    testClass: Class<*>,
    testMethod: Method? = null,
    testName: String = testMethod?.name ?: testClass.simpleName,
    block: (AndroidSandbox) -> Unit,
  ): ExecutionResult {
    val startTime = System.currentTimeMillis()
    return try {
      executeSandboxed(testClass, testMethod, testName, block)
      ExecutionResult.success(System.currentTimeMillis() - startTime)
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
      ExecutionResult.failure(e, System.currentTimeMillis() - startTime)
    } catch (@Suppress("TooGenericExceptionCaught") e: Error) {
      ExecutionResult.failure(e, System.currentTimeMillis() - startTime)
    }
  }

  /**
   * Execute a test with full lifecycle management.
   *
   * This method handles:
   * - Sandbox creation/reuse
   * - Environment setup
   * - Class bootstrapping
   * - Parameter resolution
   * - Lifecycle method invocation (@BeforeEach/@AfterEach)
   * - Cleanup on success/failure
   *
   * @param testClass The test class being executed
   * @param testMethod The test method to execute
   * @param testName Human-readable test name for logging
   * @param beforeEachAnnotations Annotations marking setup methods (e.g., BeforeEach)
   * @param afterEachAnnotations Annotations marking teardown methods (e.g., AfterEach)
   * @param parameterResolver Resolver for test method parameters
   * @param testBody The test logic to execute
   * @return ExecutionResult indicating success, failure, or skipped
   */
  @Suppress("LongParameterList", "SpreadOperator")
  fun executeTest(
    testClass: Class<*>,
    testMethod: Method,
    testName: String = testMethod.name,
    beforeEachAnnotations: List<Class<out Annotation>> = emptyList(),
    afterEachAnnotations: List<Class<out Annotation>> = emptyList(),
    @Suppress("UNUSED_PARAMETER")
    parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver,
    testBody: (TestExecutionContext) -> Unit,
  ): ExecutionResult {
    val startTime = System.currentTimeMillis()

    return try {
      val sandboxContext = lifecycleManager.createSandbox(testClass, testMethod)
      val sandbox = sandboxContext.sandbox
      val context = TestExecutionContext.fromSandboxContext(sandboxContext, testClass, testMethod)

      RunnerLogger.logTestStart(testClass.simpleName, testName, sandboxContext.sdk.apiLevel)

      sandbox.runOnMainThread(
        Callable<Void?> {
          lifecycleManager.executeInSandbox(sandboxContext, testName) {
            // Bootstrap test class
            val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
            val testInstance = TestBootstrapper.createTestInstance(bootstrappedClass)

            // Run @BeforeEach methods
            beforeEachAnnotations.forEach { annotation ->
              LifecycleHelper.invokeLifecycleMethods(bootstrappedClass, testInstance, annotation)
            }

            try {
              // Resolve parameters and invoke test
              if (testMethod.parameterCount > 0) {
                val args =
                  ParameterResolutionHelper.resolveParameters(testMethod.parameters, sandbox)
                val bootstrappedMethod =
                  bootstrappedClass.getMethod(
                    testMethod.name,
                    *TestBootstrapper.bootstrapParameterTypes(sandbox, testMethod.parameterTypes),
                  )
                bootstrappedMethod.isAccessible = true
                bootstrappedMethod.invoke(testInstance, *args)
              } else {
                TestBootstrapper.invokeTestMethod(testInstance, testMethod, emptyArray())
              }

              // Execute custom test body if provided additional logic
              testBody(context)
            } finally {
              // Run @AfterEach methods (in reverse order)
              afterEachAnnotations.reversed().forEach { annotation ->
                LifecycleHelper.invokeLifecycleMethods(bootstrappedClass, testInstance, annotation)
              }
            }
          }
          null
        }
      )

      val duration = System.currentTimeMillis() - startTime
      RunnerLogger.logTestEnd(testClass.simpleName, testName, duration, success = true)
      RunnerMetrics.recordTestExecution(true)
      RunnerMetrics.recordTiming(RunnerMetrics.PHASE_TEST_EXECUTION, duration)
      ExecutionResult.success(duration)
    } catch (e: java.lang.reflect.InvocationTargetException) {
      val duration = System.currentTimeMillis() - startTime
      val actualError = e.targetException ?: e
      RunnerLogger.logTestEnd(testClass.simpleName, testName, duration, success = false)
      RunnerMetrics.recordTestExecution(false)
      ExecutionResult.failure(actualError, duration)
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      val duration = System.currentTimeMillis() - startTime
      RunnerLogger.logTestEnd(testClass.simpleName, testName, duration, success = false)
      RunnerMetrics.recordTestExecution(false)
      ExecutionResult.failure(e, duration)
    }
  }

  /**
   * Execute multiple tests efficiently with shared sandbox.
   *
   * This method creates a single sandbox for the class and executes all test methods within it,
   * which is more efficient than creating a sandbox per test.
   *
   * @param testClass The test class containing the methods
   * @param testMethods List of test methods to execute
   * @param beforeAllAnnotations Annotations marking class-level setup (e.g., BeforeAll)
   * @param afterAllAnnotations Annotations marking class-level teardown (e.g., AfterAll)
   * @param testExecutor Function to execute each individual test
   * @return Map of method to ExecutionResult for each test
   */
  @Suppress("LongParameterList")
  fun executeTestClass(
    testClass: Class<*>,
    testMethods: List<Method>,
    beforeAllAnnotations: List<Class<out Annotation>> = emptyList(),
    afterAllAnnotations: List<Class<out Annotation>> = emptyList(),
    testExecutor: (Method, TestExecutionContext) -> ExecutionResult,
  ): Map<Method, ExecutionResult> {
    if (testMethods.isEmpty()) {
      return emptyMap()
    }

    val results = mutableMapOf<Method, ExecutionResult>()
    val classLifecycleManager = ClassLifecycleManager(lifecycleManager)

    try {
      // Setup class-level sandbox
      val classContext = classLifecycleManager.setupForClass(testClass)
      val sandbox = classContext.sandbox

      // Run @BeforeAll methods
      sandbox.runOnMainThread(
        Callable<Void?> {
          classLifecycleManager.executeInClassContext(testClass, "<beforeAll>") {
            val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
            beforeAllAnnotations.forEach { annotation ->
              LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, annotation)
            }
          }
          null
        }
      )

      // Execute each test
      for (method in testMethods) {
        val context = TestExecutionContext.fromSandboxContext(classContext, testClass, method)
        val result = testExecutor(method, context)
        results[method] = result
      }

      // Run @AfterAll methods
      sandbox.runOnMainThread(
        Callable<Void?> {
          classLifecycleManager.executeInClassContext(testClass, "<afterAll>") {
            val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
            afterAllAnnotations.reversed().forEach { annotation ->
              LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, annotation)
            }
          }
          null
        }
      )
    } finally {
      classLifecycleManager.tearDownForClass(testClass)
    }

    return results
  }

  /**
   * Executes a test block with a specific SDK and returns a result.
   *
   * This overload is used for SDK-parameterized test execution where the SDK is determined during
   * test discovery rather than configuration resolution.
   *
   * @param testClass The test class being executed
   * @param testMethod The specific test method (optional)
   * @param sdk The specific SDK to use for this test execution
   * @param testName Human-readable test name
   * @param block The test logic to execute
   * @return ExecutionResult with success status and optional error
   */
  fun executeSandboxedSafe(
    testClass: Class<*>,
    testMethod: Method?,
    sdk: org.robolectric.pluginapi.Sdk,
    testName: String = testMethod?.name ?: testClass.simpleName,
    block: (AndroidSandbox) -> Unit,
  ): ExecutionResult {
    val startTime = System.currentTimeMillis()
    return try {
      // Create sandbox contexts for the method
      val sandboxContexts =
        if (testMethod != null) {
          lifecycleManager.createSandboxes(testClass, testMethod)
        } else {
          lifecycleManager.createSandboxes(testClass)
        }

      // Find the context matching the requested SDK
      val sandboxContext =
        sandboxContexts.find { it.sdk.apiLevel == sdk.apiLevel }
          ?: return ExecutionResult.failure(
            IllegalStateException("No sandbox found for SDK ${sdk.apiLevel}"),
            System.currentTimeMillis() - startTime,
          )

      val sandbox = sandboxContext.sandbox

      try {
        // Execute on sandbox main thread
        sandbox.runOnMainThread(
          Callable<Void?> {
            lifecycleManager.executeInSandbox(sandboxContext, testName) { block(sandbox) }
            null
          }
        )
        ExecutionResult.success(System.currentTimeMillis() - startTime)
      } catch (e: java.lang.reflect.InvocationTargetException) {
        // Unwrap reflection exceptions to get the actual test failure
        ExecutionResult.failure(e.targetException ?: e, System.currentTimeMillis() - startTime)
      }
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
      ExecutionResult.failure(e, System.currentTimeMillis() - startTime)
    } catch (@Suppress("TooGenericExceptionCaught") e: Error) {
      ExecutionResult.failure(e, System.currentTimeMillis() - startTime)
    }
  }
}
