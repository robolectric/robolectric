package org.robolectric.runner.common

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
    testMethod: java.lang.reflect.Method? = null,
    testName: String = testMethod?.name ?: testClass.simpleName,
    block: (AndroidSandbox) -> Unit,
  ): ExecutionResult {
    return try {
      executeSandboxed(testClass, testMethod, testName, block)
      ExecutionResult.success()
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
      ExecutionResult.failure(e)
    } catch (@Suppress("TooGenericExceptionCaught") e: Error) {
      ExecutionResult.failure(e)
    }
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
    testMethod: java.lang.reflect.Method?,
    sdk: org.robolectric.pluginapi.Sdk,
    testName: String = testMethod?.name ?: testClass.simpleName,
    block: (AndroidSandbox) -> Unit,
  ): ExecutionResult {
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
            IllegalStateException("No sandbox found for SDK ${sdk.apiLevel}")
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
        ExecutionResult.success()
      } catch (e: java.lang.reflect.InvocationTargetException) {
        // Unwrap reflection exceptions to get the actual test failure
        ExecutionResult.failure(e.targetException ?: e)
      }
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
      ExecutionResult.failure(e)
    } catch (@Suppress("TooGenericExceptionCaught") e: Error) {
      ExecutionResult.failure(e)
    }
  }

  /** Result of a sandboxed test execution. */
  data class ExecutionResult(val isSuccess: Boolean, val error: Throwable? = null) {
    companion object {
      fun success(): ExecutionResult = ExecutionResult(true, null)

      fun failure(error: Throwable): ExecutionResult = ExecutionResult(false, error)
    }
  }
}
