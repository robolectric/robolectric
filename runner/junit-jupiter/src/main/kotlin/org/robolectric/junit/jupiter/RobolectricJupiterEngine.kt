package org.robolectric.junit.jupiter

import java.lang.reflect.Method
import java.util.Optional
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
import org.robolectric.annotation.Config
import org.robolectric.internal.AndroidSandbox
import org.robolectric.pluginapi.Sdk
import org.robolectric.pluginapi.config.ConfigurationStrategy
import org.robolectric.runner.common.ClassLifecycleManager
import org.robolectric.runner.common.DefaultRobolectricParameterResolver
import org.robolectric.runner.common.ExecutionResult
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.LifecycleHelper
import org.robolectric.runner.common.ManifestResolver
import org.robolectric.runner.common.ParameterResolver
import org.robolectric.runner.common.RobolectricDependencies
import org.robolectric.runner.common.RobolectricSandboxExecutor
import org.robolectric.runner.common.RunnerLogger
import org.robolectric.runner.common.SandboxLifecycleManager
import org.robolectric.runner.common.SdkFallbackResolver
import org.robolectric.runner.common.TestBootstrapper
import org.robolectric.runner.common.TestMethodInvoker

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
 * 2. **Sandbox Initialization**: Uses [ClassLifecycleManager] for class-level lifecycle
 *    orchestration (`@BeforeAll`/`@AfterAll`) and method execution, while preserving per-method
 *    configuration isolation when needed.
 * 3. **Extension Support**: Native support for [RobolectricExtension], allowing declarative
 *    injection of Android components like `Context` and `ActivityController`.
 */
@Suppress("TooManyFunctions")
@OptIn(ExperimentalRunnerApi::class)
class RobolectricJupiterEngine
@JvmOverloads
constructor(
  private val parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver
) : TestEngine {

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
        selector.javaClass,
        selector.javaMethod,
      )
    }

    return engineDescriptor
  }

  /** Determines if a method is a test method by checking for standard @Test annotations. */
  private fun isTestMethod(method: Method): Boolean {
    return AnnotationSupport.isAnnotated(method, Test::class.java) ||
      AnnotationSupport.isAnnotated(method, org.junit.Test::class.java)
  }

  /** Orchestrates the execution of the test tree. */
  override fun execute(request: ExecutionRequest) {
    val root = request.rootTestDescriptor
    val listener = request.engineExecutionListener

    // Initialize Robolectric dependencies and lifecycle manager
    val deps = RobolectricDependencies.create()
    val lifecycleManager = SandboxLifecycleManager(deps)
    val classLifecycleManager = ClassLifecycleManager(lifecycleManager)
    val sandboxExecutor = RobolectricSandboxExecutor(lifecycleManager)

    executeDescriptor(root, listener, sandboxExecutor, classLifecycleManager, deps)
  }

  private fun executeDescriptor(
    descriptor: TestDescriptor,
    listener: EngineExecutionListener,
    sandboxExecutor: RobolectricSandboxExecutor,
    classLifecycleManager: ClassLifecycleManager,
    deps: RobolectricDependencies,
  ) {
    if (descriptor is JupiterDescriptorBuilders.JupiterMethodDescriptor) {
      runTestMethod(descriptor, listener, sandboxExecutor, classLifecycleManager, deps)
    } else if (descriptor is JupiterDescriptorBuilders.JupiterClassDescriptor) {
      runClassWithLifecycle(descriptor, listener, sandboxExecutor, classLifecycleManager, deps)
    } else {
      // Engine descriptor
      listener.executionStarted(descriptor)
      ArrayList(descriptor.children).forEach {
        executeDescriptor(it, listener, sandboxExecutor, classLifecycleManager, deps)
      }
      listener.executionFinished(descriptor, TestExecutionResult.successful())
    }
  }

  private fun runClassWithLifecycle(
    descriptor: JupiterDescriptorBuilders.JupiterClassDescriptor,
    listener: EngineExecutionListener,
    sandboxExecutor: RobolectricSandboxExecutor,
    classLifecycleManager: ClassLifecycleManager,
    deps: RobolectricDependencies,
  ) {
    listener.executionStarted(descriptor)
    val testClass = descriptor.testClass
    if (!hasClassLifecycleMethods(testClass)) {
      ArrayList(descriptor.children).forEach {
        executeDescriptor(it, listener, sandboxExecutor, classLifecycleManager, deps)
      }
      listener.executionFinished(descriptor, TestExecutionResult.successful())
      return
    }

    var setupError: Throwable? = null

    // Setup class context
    try {
      classLifecycleManager.setupForClass(testClass)
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      setupError = e
    }

    // Run @BeforeAll
    if (setupError == null) {
      try {
        classLifecycleManager.executeInClassContext(
          testClass,
          "${testClass.simpleName}.<beforeAll>",
        ) {
          val sandbox = classLifecycleManager.getClassContext(testClass)!!.sandbox
          val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
          LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, BeforeAll::class.java)
        }
      } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
        setupError = e
      }
    }

    // Run children only if @BeforeAll succeeded
    if (setupError == null) {
      ArrayList(descriptor.children).forEach {
        executeDescriptor(it, listener, sandboxExecutor, classLifecycleManager, deps)
      }
    }

    // Always run @AfterAll (even if tests/setup failed)
    try {
      classLifecycleManager.executeInClassContext(testClass, "${testClass.simpleName}.<afterAll>") {
        val sandbox = classLifecycleManager.getClassContext(testClass)!!.sandbox
        val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
        LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, AfterAll::class.java)
      }
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      RunnerLogger.error("Error in @AfterAll for ${testClass.simpleName}", e)
      if (setupError == null) setupError = e else setupError.addSuppressed(e)
    } finally {
      classLifecycleManager.tearDownForClass(testClass)
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
    classLifecycleManager: ClassLifecycleManager,
    deps: RobolectricDependencies,
  ) {
    listener.executionStarted(descriptor)
    val testClass = descriptor.method.declaringClass

    val methodConfiguration = deps.configurationStrategy.getConfig(testClass, descriptor.method)
    val methodSdk =
      selectMethodSdkWithFallback(deps, testClass, descriptor.method, methodConfiguration)
    val classContext = classLifecycleManager.getClassContext(testClass)

    val executionResult =
      if (methodSdk == null) {
        ExecutionResult.failure(
          IllegalStateException("No SDK selected for ${testClass.name}.${descriptor.method.name}")
        )
      } else {
        when (
          val executionPolicy =
            resolveExecutionPolicy(testClass, descriptor.method, methodSdk, classContext)
        ) {
          is ExecutionPolicy.SharedClassEnvironment -> {
            try {
              classLifecycleManager.executeInClassContext(testClass, descriptor.method.name) {
                runTestInSandbox(executionPolicy.classContext.sandbox, testClass, descriptor.method)
              }
              ExecutionResult.success()
            } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
              ExecutionResult.failure(e)
            }
          }
          is ExecutionPolicy.IsolatedMethodEnvironment -> {
            sandboxExecutor.executeSandboxedSafe(
              testClass,
              descriptor.method,
              executionPolicy.sdk,
            ) { sandbox ->
              runTestInSandbox(sandbox, testClass, descriptor.method)
            }
          }
          is ExecutionPolicy.FailFastConflict -> {
            ExecutionResult.failure(IllegalStateException(executionPolicy.message))
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

  private fun hasClassLifecycleMethods(testClass: Class<*>): Boolean =
    LifecycleHelper.hasLifecycleMethods(
      testClass,
      listOf(BeforeAll::class.java, AfterAll::class.java),
    )

  private fun resolveExecutionPolicy(
    testClass: Class<*>,
    method: Method,
    methodSdk: Sdk,
    classContext: SandboxLifecycleManager.SandboxContext?,
  ): ExecutionPolicy {
    val resolvedClassContext =
      classContext ?: return ExecutionPolicy.IsolatedMethodEnvironment(methodSdk)

    val conflictMessage =
      if (resolvedClassContext.sdk.apiLevel != methodSdk.apiLevel) {
        "Configuration conflict for ${testClass.name}.${method.name}: class lifecycle uses shared SDK " +
          "${resolvedClassContext.sdk.apiLevel} but method requested SDK ${methodSdk.apiLevel}. " +
          "Tests with @BeforeAll/@AfterAll share one Android environment and cannot change SDK per method. " +
          "Move this test to a separate class without @BeforeAll/@AfterAll."
      } else if (hasConflictingMethodLevelConfig(method)) {
        val methodConfig = method.getAnnotation(Config::class.java)
        "Configuration conflict for ${testClass.name}.${method.name}: method-level @Config overrides " +
          "(${
            describeMethodOverrides(methodConfig)
          }) are not supported when the class declares @BeforeAll/@AfterAll. " +
          "Move this test to a separate class without class lifecycle callbacks."
      } else {
        null
      }

    return if (conflictMessage != null) {
      ExecutionPolicy.FailFastConflict(conflictMessage)
    } else {
      ExecutionPolicy.SharedClassEnvironment(resolvedClassContext)
    }
  }

  private fun hasConflictingMethodLevelConfig(method: Method): Boolean {
    val methodConfig = method.getAnnotation(Config::class.java) ?: return false
    return methodConfig.sdk.isNotEmpty() ||
      methodConfig.minSdk != Config.DEFAULT_VALUE_INT ||
      methodConfig.maxSdk != Config.DEFAULT_VALUE_INT ||
      methodConfig.qualifiers != Config.DEFAULT_QUALIFIERS ||
      methodConfig.manifest != Config.DEFAULT_VALUE_STRING ||
      methodConfig.application != Config.DEFAULT_APPLICATION ||
      methodConfig.shadows.isNotEmpty() ||
      methodConfig.instrumentedPackages.isNotEmpty() ||
      methodConfig.fontScale != Config.DEFAULT_FONT_SCALE
  }

  private fun describeMethodOverrides(config: Config?): String {
    if (config == null) return "none"
    val overrides = mutableListOf<String>()
    if (config.sdk.isNotEmpty()) overrides.add("sdk=${config.sdk.joinToString(",")}")
    if (config.minSdk != Config.DEFAULT_VALUE_INT) overrides.add("minSdk=${config.minSdk}")
    if (config.maxSdk != Config.DEFAULT_VALUE_INT) overrides.add("maxSdk=${config.maxSdk}")
    if (config.qualifiers != Config.DEFAULT_QUALIFIERS)
      overrides.add("qualifiers=${config.qualifiers}")
    if (config.application != Config.DEFAULT_APPLICATION) {
      overrides.add("application=${config.application.simpleName}")
    }
    if (config.shadows.isNotEmpty()) overrides.add("shadows=${config.shadows.size}")
    if (config.instrumentedPackages.isNotEmpty()) {
      overrides.add("instrumentedPackages=${config.instrumentedPackages.size}")
    }
    return if (overrides.isEmpty()) "none" else overrides.joinToString(", ")
  }

  private fun selectMethodSdkWithFallback(
    deps: RobolectricDependencies,
    testClass: Class<*>,
    method: Method,
    methodConfiguration: ConfigurationStrategy.Configuration,
  ): Sdk? {
    val methodConfig = methodConfiguration.get(Config::class.java)
    val methodManifest = ManifestResolver.resolveManifest(methodConfig)
    deps.sdkPicker.selectSdks(methodConfiguration, methodManifest).firstOrNull()?.let {
      return it
    }
    return SdkFallbackResolver.resolveFallbackSdk(deps, testClass, method, methodConfig)
  }

  private fun runTestInSandbox(sandbox: AndroidSandbox, testClass: Class<*>, method: Method) {
    TestMethodInvoker.invoke(
      sandbox = sandbox,
      testClass = testClass,
      testMethod = method,
      beforeEachAnnotations = listOf(BeforeEach::class.java),
      afterEachAnnotations = listOf(AfterEach::class.java),
      parameterResolver = parameterResolver,
    )
  }

  override fun getGroupId(): Optional<String> = Optional.of("org.robolectric")

  override fun getArtifactId(): Optional<String> = Optional.of("junit-jupiter")

  companion object {
    const val ENGINE_ID = "robolectric-junit-jupiter-engine"
  }
}

private sealed interface ExecutionPolicy {
  data class SharedClassEnvironment(val classContext: SandboxLifecycleManager.SandboxContext) :
    ExecutionPolicy

  data class IsolatedMethodEnvironment(val sdk: Sdk) : ExecutionPolicy

  data class FailFastConflict(val message: String) : ExecutionPolicy
}
