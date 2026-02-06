package org.robolectric.junit.platform

import java.lang.reflect.Method
import java.util.Optional
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
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
import org.robolectric.runner.common.ClassLifecycleManager
import org.robolectric.runner.common.DiscoveryHelpers
import org.robolectric.runner.common.ExecutionResult
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.LifecycleHelper
import org.robolectric.runner.common.ParameterResolutionHelper
import org.robolectric.runner.common.RobolectricDependencies
import org.robolectric.runner.common.RobolectricSandboxExecutor
import org.robolectric.runner.common.RunnerLogger
import org.robolectric.runner.common.RunnerMetrics
import org.robolectric.runner.common.SandboxLifecycleManager
import org.robolectric.runner.common.SystemPropertiesSupport
import org.robolectric.runner.common.TestBootstrapper

/**
 * Robolectric [TestEngine] implementation for the JUnit Platform.
 *
 * ## Architecture Overview
 *
 * The [RobolectricTestEngine] enables running Android tests within a sandboxed JVM environment
 * managed by Robolectric, while fully integrating with the JUnit Platform ecosystem (IDE, Gradle,
 * etc.).
 *
 * ### Key Components:
 * 1. **JUnit Platform Bridge**: Implements [TestEngine] to participate in the JUnit discovery and
 *    execution phases.
 * 2. **Discovery**: Maps JUnit [DiscoverySelectors][org.junit.platform.engine.DiscoverySelector]
 *    (classes, methods) to Robolectric test descriptors. It identifies tests annotated with either
 *    JUnit 4's `@org.junit.Test` or JUnit Jupiter's `@org.junit.jupiter.api.Test`.
 * 3. **Execution & Sandboxing**: For every test method, it initializes a dedicated Robolectric
 *    [AndroidSandbox]. This involves:
 *     - Calculating Robolectric [org.robolectric.annotation.Config] via
 *       [org.robolectric.pluginapi.config.ConfigurationStrategy].
 *     - Resolving the Android Manifest via [org.robolectric.internal.DefaultManifestFactory].
 *     - Selecting the appropriate Android SDK via [org.robolectric.pluginapi.SdkPicker].
 *     - Applying bytecode instrumentation and interceptors via
 *       [org.robolectric.config.AndroidConfigurer].
 * 4. **Environment Setup**: Bootstraps the Android environment within the sandbox using
 *    [org.robolectric.android.internal.AndroidTestEnvironment], including application state, system
 *    properties, and loopers.
 * 5. **Reflective Invocation**: Executes the test method reflectively using the bootstrapped class
 *    loaded by the [SandboxClassLoader][org.robolectric.internal.bytecode.SandboxClassLoader].
 *
 * ## Execution Steps:
 * 1. **Discovery Phase**: Scan the classpath based on request selectors and build a tree of
 *    descriptors.
 * 2. **Execution Phase**:
 *     - Start execution for each [TestDescriptor].
 *     - For method descriptors: a. Initialize the Robolectric global
 *       [org.robolectric.util.inject.Injector]. b. Resolve the test's specific Android
 *       configuration and manifest. c. Create or reuse an [AndroidSandbox] for the requested SDK.
 *       d. Configure the sandbox with required [org.robolectric.internal.bytecode.Interceptors] and
 *       [ShadowMap][org.robolectric.internal.bytecode.ShadowMap]. e. Run the environment setup and
 *       test method on the sandbox's main thread. f. Clean up the environment after execution.
 *     - Report [TestExecutionResult] back to the platform.
 */
@OptIn(ExperimentalRunnerApi::class)
class RobolectricTestEngine : TestEngine {

  override fun getId(): String = ENGINE_ID

  /**
   * Discovers tests within the provided [EngineDiscoveryRequest].
   *
   * It handles [ClassSelector] and [MethodSelector], building a hierarchical tree of descriptors
   * that JUnit Platform uses to display and filter tests.
   *
   * When `-Drobolectric.enabledSdks` is specified, this creates multiple descriptors per test
   * method (one for each SDK), enabling SDK-parameterized test execution.
   */
  override fun discover(
    discoveryRequest: EngineDiscoveryRequest,
    uniqueId: UniqueId,
  ): TestDescriptor {
    val engineDescriptor = EngineDescriptor(uniqueId, "Robolectric")

    // Initialize dependencies to access SdkPicker and ConfigurationStrategy
    val deps = RobolectricDependencies.create()
    val alwaysIncludeMarkers = SystemPropertiesSupport.alwaysIncludeVariantMarkersInTestName()

    discoveryRequest.getSelectorsByType(ClassSelector::class.java).forEach { selector ->
      appendTestClassWithSdkParameterization(
        engineDescriptor,
        selector.getJavaClass(),
        deps,
        alwaysIncludeMarkers,
      )
    }

    discoveryRequest.getSelectorsByType(MethodSelector::class.java).forEach { selector ->
      appendTestMethodWithSdkParameterization(
        engineDescriptor,
        selector.getJavaClass(),
        selector.javaMethod,
        deps,
        alwaysIncludeMarkers,
      )
    }

    return engineDescriptor
  }

  /**
   * Appends a test class with SDK-parameterized test methods.
   *
   * For each test method, creates multiple descriptors if multiple SDKs are selected.
   */
  private fun appendTestClassWithSdkParameterization(
    parent: TestDescriptor,
    testClass: Class<*>,
    deps: RobolectricDependencies,
    alwaysIncludeMarkers: Boolean,
  ) {
    val classId = parent.uniqueId.append("class", testClass.name)
    val classDescriptor =
      parent.children.find { it.uniqueId == classId }
        ?: PlatformDescriptorBuilders.PlatformClassDescriptor(classId, testClass).also {
          parent.addChild(it)
        }

    // Add SDK-parameterized test methods
    testClass.declaredMethods
      .filter { isTestMethod(it) }
      .forEach { method ->
        val sdkContexts = createSdkContextsForMethod(testClass, method, deps)
        sdkContexts.forEachIndexed { index, context ->
          val isLastSdk = index == sdkContexts.size - 1
          val displayName =
            SystemPropertiesSupport.formatTestName(
              method.name,
              context.sdk.apiLevel,
              alwaysIncludeMarkers,
              isLastSdk,
            )
          val sdkSegment = SystemPropertiesSupport.createSdkSegment(context.sdk.apiLevel)
          val methodId = classDescriptor.uniqueId.append("method", "${method.name}[$sdkSegment]")

          if (classDescriptor.children.none { it.uniqueId == methodId }) {
            classDescriptor.addChild(
              SdkParameterizedMethodDescriptor(methodId, method, context.sdk, displayName)
            )
          }
        }
      }
  }

  /** Appends a specific test method with SDK parameterization. */
  private fun appendTestMethodWithSdkParameterization(
    parent: TestDescriptor,
    testClass: Class<*>,
    method: Method,
    deps: RobolectricDependencies,
    alwaysIncludeMarkers: Boolean,
  ) {
    val classDescriptor = findOrCreateClassDescriptor(parent, testClass)
    val sdkContexts = createSdkContextsForMethod(testClass, method, deps)

    sdkContexts.forEachIndexed { index, context ->
      val isLastSdk = index == sdkContexts.size - 1
      val displayName =
        SystemPropertiesSupport.formatTestName(
          method.name,
          context.sdk.apiLevel,
          alwaysIncludeMarkers,
          isLastSdk,
        )
      val sdkSegment = SystemPropertiesSupport.createSdkSegment(context.sdk.apiLevel)
      val methodId = classDescriptor.uniqueId.append("method", "${method.name}[$sdkSegment]")

      if (classDescriptor.children.none { it.uniqueId == methodId }) {
        classDescriptor.addChild(
          SdkParameterizedMethodDescriptor(methodId, method, context.sdk, displayName)
        )
      }
    }
  }

  /** Creates SandboxContext instances for all SDKs selected for a test method. */
  private fun createSdkContextsForMethod(
    testClass: Class<*>,
    method: Method,
    deps: RobolectricDependencies,
  ): List<SandboxLifecycleManager.SandboxContext> {
    val manager = SandboxLifecycleManager(deps)
    return try {
      manager.createSandboxes(testClass, method)
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
      // If sandbox creation fails, return empty list to skip this test
      emptyList()
    }
  }

  private fun findOrCreateClassDescriptor(
    parent: TestDescriptor,
    testClass: Class<*>,
  ): TestDescriptor {
    val classId = parent.uniqueId.append("class", testClass.name)
    return parent.children.find { it.uniqueId == classId }
      ?: PlatformDescriptorBuilders.PlatformClassDescriptor(classId, testClass).also {
        parent.addChild(it)
      }
  }

  /**
   * Descriptor for an SDK-parameterized test method.
   *
   * This descriptor contains the SDK information needed to execute the test with the correct
   * Android SDK version.
   */
  private class SdkParameterizedMethodDescriptor(
    uniqueId: UniqueId,
    val method: Method,
    val sdk: org.robolectric.pluginapi.Sdk,
    displayName: String,
  ) :
    org.junit.platform.engine.support.descriptor.AbstractTestDescriptor(
      uniqueId,
      displayName,
      org.junit.platform.engine.support.descriptor.MethodSource.from(method),
    ) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST
  }

  /** Determines if a method is a test method by checking for supported @Test annotations. */
  private fun isTestMethod(method: Method): Boolean {
    return DiscoveryHelpers.isTestMethod(method)
  }

  /** Executes the test tree starting from the root descriptor. */
  @OptIn(ExperimentalRunnerApi::class)
  @Suppress("TooGenericExceptionCaught")
  override fun execute(request: ExecutionRequest) {
    val rootDescriptor = request.rootTestDescriptor
    val listener = request.engineExecutionListener

    // Initialize Robolectric dependencies and lifecycle manager
    val deps = RobolectricDependencies.create()
    val lifecycleManager = SandboxLifecycleManager(deps)
    val classLifecycleManager = ClassLifecycleManager(lifecycleManager)
    val sandboxExecutor = RobolectricSandboxExecutor(lifecycleManager)

    listener.executionStarted(rootDescriptor)

    try {
      rootDescriptor.children.forEach { descriptor ->
        executeDescriptor(descriptor, listener, sandboxExecutor, classLifecycleManager, deps)
      }
      listener.executionFinished(rootDescriptor, TestExecutionResult.successful())
    } catch (e: Exception) {
      listener.executionFinished(rootDescriptor, TestExecutionResult.failed(e))
    }
  }

  /**
   * Recursively executes descriptors. If the descriptor is a method, it initializes the Robolectric
   * environment and runs the test within a sandbox.
   */
  @OptIn(ExperimentalRunnerApi::class)
  private fun executeDescriptor(
    descriptor: TestDescriptor,
    listener: EngineExecutionListener,
    sandboxExecutor: RobolectricSandboxExecutor,
    classLifecycleManager: ClassLifecycleManager,
    deps: RobolectricDependencies,
  ) {
    if (descriptor is SdkParameterizedMethodDescriptor) {
      // Handle SDK-parameterized method descriptor
      listener.executionStarted(descriptor)
      val testClass = descriptor.method.declaringClass
      val requestedSdk = descriptor.sdk

      // Check if we have a class-level context
      val classContext = classLifecycleManager.getClassContext(testClass)

      // Calculate method configuration
      val methodConfiguration = deps.configurationStrategy.getConfig(testClass, descriptor.method)

      val executionResult =
        if (classContext != null && classContext.sdk.apiLevel == requestedSdk.apiLevel) {
          try {
            RunnerLogger.logTestStart(
              testClass.simpleName,
              descriptor.method.name,
              requestedSdk.apiLevel,
            )
            val start = System.currentTimeMillis()
            classLifecycleManager.executeInClassContext(
              testClass,
              descriptor.method.name,
              methodConfiguration,
            ) {
              runTestInSandbox(classContext.sandbox, descriptor.method)
            }
            val duration = System.currentTimeMillis() - start
            RunnerLogger.logTestEnd(testClass.simpleName, descriptor.method.name, duration, true)
            RunnerMetrics.recordTestExecution(true)
            RunnerMetrics.recordTiming(RunnerMetrics.PHASE_TEST_EXECUTION, duration)
            ExecutionResult.success()
          } catch (e: Throwable) {
            RunnerLogger.logTestEnd(testClass.simpleName, descriptor.method.name, 0, false)
            RunnerMetrics.recordTestExecution(false)
            ExecutionResult.failure(e)
          }
        } else {
          // Use per-test sandbox with the specific SDK from the descriptor
          sandboxExecutor.executeSandboxedSafe(testClass, descriptor.method, requestedSdk) { sandbox
            ->
            runTestInSandbox(sandbox, descriptor.method)
          }
        }

      val result =
        if (executionResult.isSuccess) {
          TestExecutionResult.successful()
        } else {
          TestExecutionResult.failed((executionResult as ExecutionResult.Failure).error)
        }

      listener.executionFinished(descriptor, result)
    } else if (descriptor is PlatformDescriptorBuilders.PlatformMethodDescriptor) {
      // Handle legacy non-parameterized method descriptor (for backward compatibility)
      listener.executionStarted(descriptor)
      val testClass = descriptor.method.declaringClass

      // Check if we have a class-level context
      val classContext = classLifecycleManager.getClassContext(testClass)

      // Calculate method configuration to check compatibility
      val methodConfiguration = deps.configurationStrategy.getConfig(testClass, descriptor.method)
      val methodConfig = methodConfiguration.get(org.robolectric.annotation.Config::class.java)
      val methodManifest =
        org.robolectric.runner.common.ManifestResolver.resolveManifest(methodConfig)
      val methodSdk = deps.sdkPicker.selectSdks(methodConfiguration, methodManifest).first()

      val executionResult =
        if (classContext != null && classContext.sdk == methodSdk) {
          try {
            RunnerLogger.logTestStart(
              testClass.simpleName,
              descriptor.method.name,
              methodSdk.apiLevel,
            )
            val start = System.currentTimeMillis()
            classLifecycleManager.executeInClassContext(
              testClass,
              descriptor.method.name,
              methodConfiguration,
            ) {
              runTestInSandbox(classContext.sandbox, descriptor.method)
            }
            val duration = System.currentTimeMillis() - start
            RunnerLogger.logTestEnd(testClass.simpleName, descriptor.method.name, duration, true)
            RunnerMetrics.recordTestExecution(true)
            RunnerMetrics.recordTiming(RunnerMetrics.PHASE_TEST_EXECUTION, duration)
            ExecutionResult.success()
          } catch (e: Throwable) {
            RunnerLogger.logTestEnd(testClass.simpleName, descriptor.method.name, 0, false)
            RunnerMetrics.recordTestExecution(false)
            ExecutionResult.failure(e)
          }
        } else {
          // Use centralized sandbox executor (per-test sandbox)
          sandboxExecutor.executeSandboxedSafe(testClass, descriptor.method) { sandbox ->
            runTestInSandbox(sandbox, descriptor.method)
          }
        }

      val result =
        if (executionResult.isSuccess) {
          TestExecutionResult.successful()
        } else {
          TestExecutionResult.failed((executionResult as ExecutionResult.Failure).error)
        }

      listener.executionFinished(descriptor, result)
    } else if (descriptor is PlatformDescriptorBuilders.PlatformClassDescriptor) {
      runClassWithLifecycle(descriptor, listener, sandboxExecutor, classLifecycleManager, deps)
    } else {
      // Container descriptors (Engine, Class)
      listener.executionStarted(descriptor)
      ArrayList(descriptor.children).forEach {
        executeDescriptor(it, listener, sandboxExecutor, classLifecycleManager, deps)
      }
      listener.executionFinished(descriptor, TestExecutionResult.successful())
    }
  }

  private fun runTestInSandbox(sandbox: AndroidSandbox, method: Method) {
    // Load the test class through the sandbox class loader
    val testClass = method.declaringClass
    val bootstrappedTestClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
    val testInstance = TestBootstrapper.createTestInstance(bootstrappedTestClass)

    // Handle method parameters if present
    if (method.parameterCount > 0) {
      // Resolve and bootstrap parameter types
      val bootstrappedParameterTypes =
        TestBootstrapper.bootstrapParameterTypes(sandbox, method.parameterTypes)

      val bootstrappedMethod =
        bootstrappedTestClass.getMethod(method.name, *bootstrappedParameterTypes)

      val args = resolveParameters(method, sandbox)
      bootstrappedMethod.invoke(testInstance, *args)
    } else {
      // No parameters - simple invocation
      TestBootstrapper.invokeTestMethod(testInstance, method, emptyArray())
    }
  }

  @Suppress("TooGenericExceptionCaught")
  private fun runClassWithLifecycle(
    descriptor: PlatformDescriptorBuilders.PlatformClassDescriptor,
    listener: EngineExecutionListener,
    sandboxExecutor: RobolectricSandboxExecutor,
    classLifecycleManager: ClassLifecycleManager,
    deps: RobolectricDependencies,
  ) {
    listener.executionStarted(descriptor)
    val testClass = descriptor.testClass
    var setupError: Throwable? = null

    // Setup class context
    classLifecycleManager.setupForClass(testClass)

    // Run @BeforeClass and @BeforeAll
    try {
      classLifecycleManager.executeInClassContext(
        testClass,
        "${testClass.simpleName}.<beforeClass>",
      ) {
        val classContext = classLifecycleManager.getClassContext(testClass)!!
        RunnerMetrics.recordSandboxCacheHit()
        RunnerLogger.logTestStart(testClass.simpleName, "<beforeClass>", classContext.sdk.apiLevel)
        RunnerMetrics.timed(RunnerMetrics.PHASE_CLASS_SETUP) {
          val bootstrappedClass =
            TestBootstrapper.bootstrapClass<Any>(classContext.sandbox, testClass)
          LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, BeforeClass::class.java)
          LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, BeforeAll::class.java)
        }
        RunnerLogger.logTestEnd(testClass.simpleName, "<beforeClass>", 0, true)
      }
    } catch (e: Throwable) {
      setupError = e
    }

    // Run children only if setup succeeded
    if (setupError == null) {
      ArrayList(descriptor.children).forEach {
        executeDescriptor(it, listener, sandboxExecutor, classLifecycleManager, deps)
      }
    }

    // Always run @AfterClass and @AfterAll (even if tests/setup failed)
    try {
      classLifecycleManager.executeInClassContext(
        testClass,
        "${testClass.simpleName}.<afterClass>",
      ) {
        val sandbox = classLifecycleManager.getClassContext(testClass)!!.sandbox
        RunnerMetrics.timed(RunnerMetrics.PHASE_CLASS_TEARDOWN) {
          val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
          LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, AfterClass::class.java)
          LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, AfterAll::class.java)
        }
      }
    } catch (e: Throwable) {
      System.err.println("Error in @AfterClass: ${e.message}")
      if (setupError == null) setupError = e
    }

    // Tear down class context
    classLifecycleManager.tearDownForClass(testClass)

    listener.executionFinished(
      descriptor,
      if (setupError != null) TestExecutionResult.failed(setupError)
      else TestExecutionResult.successful(),
    )
  }

  /**
   * Resolves method parameters for test methods. Supports Context, Application, ActivityController,
   * and ServiceController injection via the common parameter resolver.
   */
  private fun resolveParameters(method: Method, sandbox: AndroidSandbox): Array<Any?> {
    return ParameterResolutionHelper.resolveParameters(method.parameters, sandbox)
  }

  override fun getGroupId(): Optional<String> = Optional.of("org.robolectric")

  override fun getArtifactId(): Optional<String> = Optional.of("junit-platform")

  companion object {
    const val ENGINE_ID = "robolectric-junit-platform-engine"
  }
}
