package org.robolectric.junit.jupiter

import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.Optional
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.MethodSelector
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.robolectric.internal.AndroidSandbox
import org.robolectric.runner.common.DefaultRobolectricParameterResolver
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.LifecycleHelper
import org.robolectric.runner.common.RobolectricDependencies
import org.robolectric.runner.common.RobolectricSandboxExecutor
import org.robolectric.runner.common.SandboxLifecycleManager
import org.robolectric.runner.common.TestBootstrapper

/**
 * Robolectric [TestEngine] implementation for JUnit Jupiter (JUnit 5/6).
 *
 * ## Architecture Overview
 *
 * The [RobolectricJupiterEngine] enables running Android tests within a sandboxed JVM environment
 * managed by Robolectric, while fully integrating with the JUnit Platform ecosystem (IDE, Gradle).
 *
 * It supports hierarchical test structures including [Nested] classes and properly merges
 * Robolectric configuration through the test hierarchy.
 *
 * ### Key Integration Points:
 * 1. **Discovery**: Hierarchically resolves test classes and methods, including support for JUnit
 *    Jupiter's `@Nested` annotation.
 * 2. **Sandbox Initialization**: For every test, a dedicated [AndroidSandbox] is created with
 *    proper bytecode instrumentation, SDK selection, and manifest resolution.
 * 3. **Extension Support**: Native support for [RobolectricExtension], allowing declarative
 *    injection of Android components like `Context` and `ActivityController`.
 */
@Suppress("TooManyFunctions")
@OptIn(ExperimentalRunnerApi::class)
class RobolectricJupiterEngine : TestEngine {

  override fun getId(): String = ENGINE_ID

  /**
   * Discovers tests based on the provided [discoveryRequest].
   *
   * It builds a hierarchical tree of descriptors starting from the root.
   */
  override fun discover(
    discoveryRequest: EngineDiscoveryRequest,
    uniqueId: UniqueId,
  ): TestDescriptor {
    val engineDescriptor = EngineDescriptor(uniqueId, "Robolectric JUnit Jupiter")

    discoveryRequest.getSelectorsByType(ClassSelector::class.java).forEach { selector ->
      JupiterDescriptorBuilders.appendTestClass(
        engineDescriptor,
        selector.getJavaClass(),
        ::isTestMethod,
        Nested::class.java,
      )
    }

    discoveryRequest.getSelectorsByType(MethodSelector::class.java).forEach { selector ->
      JupiterDescriptorBuilders.appendTestMethod(
        engineDescriptor,
        selector.getJavaClass(),
        selector.javaMethod,
      )
    }

    return engineDescriptor
  }

  /** Determines if a method is a test method by checking for standard @Test annotations. */
  private fun isTestMethod(method: Method): Boolean {
    return AnnotationSupport.isAnnotated(method, org.junit.jupiter.api.Test::class.java) ||
      AnnotationSupport.isAnnotated(method, org.junit.Test::class.java)
  }

  /** Orchestrates the execution of the test tree. */
  override fun execute(request: ExecutionRequest) {
    val root = request.rootTestDescriptor
    val listener = request.engineExecutionListener

    // Initialize Robolectric dependencies and lifecycle manager
    val deps = RobolectricDependencies.create()
    val lifecycleManager = SandboxLifecycleManager(deps)
    val sandboxExecutor = RobolectricSandboxExecutor(lifecycleManager)

    executeDescriptor(root, listener, sandboxExecutor)
  }

  private fun executeDescriptor(
    descriptor: TestDescriptor,
    listener: EngineExecutionListener,
    sandboxExecutor: RobolectricSandboxExecutor,
  ) {
    if (descriptor is JupiterDescriptorBuilders.JupiterMethodDescriptor) {
      runTestMethod(descriptor, listener, sandboxExecutor)
    } else if (descriptor is JupiterDescriptorBuilders.JupiterClassDescriptor) {
      runClassWithLifecycle(descriptor, listener, sandboxExecutor)
    } else {
      // Engine descriptor
      listener.executionStarted(descriptor)
      ArrayList(descriptor.children).forEach { executeDescriptor(it, listener, sandboxExecutor) }
      listener.executionFinished(descriptor, TestExecutionResult.successful())
    }
  }

  private fun runClassWithLifecycle(
    descriptor: JupiterDescriptorBuilders.JupiterClassDescriptor,
    listener: EngineExecutionListener,
    sandboxExecutor: RobolectricSandboxExecutor,
  ) {
    listener.executionStarted(descriptor)
    val testClass = descriptor.testClass
    var setupError: Throwable? = null

    // Run @BeforeAll
    try {
      sandboxExecutor.executeSandboxed(
        testClass,
        testName = "${testClass.simpleName}.<beforeAll>",
      ) { sandbox ->
        val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
        LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, BeforeAll::class.java)
      }
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      setupError = e
    }

    // Run children only if @BeforeAll succeeded
    if (setupError == null) {
      ArrayList(descriptor.children).forEach { executeDescriptor(it, listener, sandboxExecutor) }
    }

    // Always run @AfterAll (even if tests/setup failed)
    try {
      sandboxExecutor.executeSandboxed(
        testClass,
        testName = "${testClass.simpleName}.<afterAll>",
      ) { sandbox ->
        val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
        LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, AfterAll::class.java)
      }
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      System.err.println("Error in @AfterAll: ${e.message}")
      if (setupError == null) setupError = e
    }

    listener.executionFinished(
      descriptor,
      if (setupError != null) TestExecutionResult.failed(setupError)
      else TestExecutionResult.successful(),
    )
  }

  /** Core execution logic: bootstraps the sandbox and runs the test method. */
  private fun runTestMethod(
    descriptor: JupiterDescriptorBuilders.JupiterMethodDescriptor,
    listener: EngineExecutionListener,
    sandboxExecutor: RobolectricSandboxExecutor,
  ) {
    listener.executionStarted(descriptor)
    val testClass = descriptor.method.declaringClass

    // Execute test using centralized sandbox executor
    val executionResult =
      sandboxExecutor.executeSandboxedSafe(testClass, descriptor.method) { sandbox ->
        // Load bootstrapped test class and create instance
        val bootstrappedTestClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
        val testInstance = TestBootstrapper.createTestInstance(bootstrappedTestClass)

        // Invoke @BeforeEach lifecycle methods
        LifecycleHelper.invokeLifecycleMethods(
          bootstrappedTestClass,
          testInstance,
          BeforeEach::class.java,
        )

        try {
          // Handle method parameters and invoke test
          invokeTestMethod(bootstrappedTestClass, testInstance, descriptor.method, sandbox)
        } finally {
          // ALWAYS invoke @AfterEach methods, even if test failed
          @Suppress("TooGenericExceptionCaught", "SwallowedException")
          try {
            LifecycleHelper.invokeLifecycleMethods(
              bootstrappedTestClass,
              testInstance,
              AfterEach::class.java,
            )
          } catch (e: Throwable) {
            // Log but don't mask test failure
            System.err.println("Error in @AfterEach: ${e.message}")
          }
        }
      }

    val result =
      if (executionResult.isSuccess) {
        TestExecutionResult.successful()
      } else {
        TestExecutionResult.failed(
          (executionResult as org.robolectric.runner.common.ExecutionResult.Failure).error
        )
      }

    listener.executionFinished(descriptor, result)
  }

  /** Invokes the test method, handling parameter resolution if necessary. */
  @Suppress("SpreadOperator")
  private fun invokeTestMethod(
    bootstrappedTestClass: Class<*>,
    testInstance: Any,
    method: Method,
    sandbox: AndroidSandbox,
  ) {
    if (method.parameterCount > 0) {
      // Method has parameters - resolve them
      val bootstrappedParameterTypes =
        TestBootstrapper.bootstrapParameterTypes(sandbox, method.parameterTypes)

      val bootstrappedMethod =
        bootstrappedTestClass.getMethod(method.name, *bootstrappedParameterTypes)
      bootstrappedMethod.isAccessible = true

      // Resolve parameters through sandbox
      val args = resolveParametersInSandbox(method.parameters, sandbox)
      bootstrappedMethod.invoke(testInstance, *args)
    } else {
      // Simple case: no parameters
      val bootstrappedMethod = bootstrappedTestClass.getMethod(method.name)
      bootstrappedMethod.invoke(testInstance)
    }
  }

  /** Resolves method parameters using the default parameter resolver. */
  @Suppress("TooGenericExceptionThrown")
  private fun resolveParametersInSandbox(
    parameters: Array<Parameter>,
    sandbox: AndroidSandbox,
  ): Array<Any?> {
    return parameters
      .map { parameter ->
        DefaultRobolectricParameterResolver.resolveParameter(parameter, sandbox)
          ?: throw IllegalArgumentException(
            "Unsupported parameter type: ${parameter.type.name}. " +
              "Supported types: Context, Application, ActivityController, ServiceController"
          )
      }
      .toTypedArray()
  }

  override fun getGroupId(): Optional<String> = Optional.of("org.robolectric")

  override fun getArtifactId(): Optional<String> = Optional.of("junit-jupiter")

  companion object {
    const val ENGINE_ID = "robolectric-junit-jupiter-engine"
  }
}
