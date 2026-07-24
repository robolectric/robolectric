package org.robolectric.junit.jupiter

import java.lang.reflect.Method
import java.util.Optional
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.commons.JUnitException
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
import org.robolectric.pluginapi.Sdk
import org.robolectric.runner.common.ClassLifecycleManager
import org.robolectric.runner.common.DefaultRobolectricParameterResolver
import org.robolectric.runner.common.ExecutionPolicyResolver
import org.robolectric.runner.common.ExecutionPolicyResolver.ExecutionPolicy
import org.robolectric.runner.common.ExecutionResult
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.LifecycleHelper
import org.robolectric.runner.common.MethodSdkResolver
import org.robolectric.runner.common.ParameterResolver
import org.robolectric.runner.common.RobolectricDependencies
import org.robolectric.runner.common.RobolectricSandboxExecutor
import org.robolectric.runner.common.RunnerLogger
import org.robolectric.runner.common.SandboxLifecycleManager
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
 * 1. **Discovery**: Hierarchically resolves test classes and methods, including JUnit Jupiter's
 *    `@Nested` annotation and per-SDK variant descriptors (`@Config(sdk = […])` +
 *    `-Drobolectric.enabledSdks`). Classes annotated `@ExtendWith(RobolectricExtension)` are
 *    skipped — they belong to the standard `junit-jupiter` engine — so classpath coexistence never
 *    double-runs a class. `@Disabled` is honored; methods of kinds this engine cannot execute
 *    (`@TestTemplate`-based, `@TestFactory`) are discovered as failing descriptors instead of being
 *    silently dropped.
 * 2. **Sandbox Initialization**: Uses [ClassLifecycleManager] for class-level lifecycle
 *    orchestration (`@BeforeAll`/`@AfterAll`); per-method execution is routed by the shared
 *    [ExecutionPolicyResolver], giving identical semantics to [RobolectricExtension].
 * 3. **Parameter Injection**: Injects Android components like `Context` and `ActivityController`
 *    into test-method parameters via the configured
 *    [org.robolectric.runner.common.ParameterResolver]. Note this engine does **not** invoke
 *    Jupiter `@ExtendWith` extensions; declarative extension support exists only on the standard
 *    engine via [RobolectricExtension].
 */
@Suppress("TooManyFunctions")
@OptIn(ExperimentalRunnerApi::class)
class RobolectricJupiterEngine
@JvmOverloads
constructor(
  private val parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver
) : TestEngine {

  /**
   * Shared dependencies for discovery-time SDK-variant expansion and execution. Lazy so engine
   * instantiation (service loading) stays cheap; created at most once per engine instance.
   */
  private val deps by lazy { RobolectricDependencies.create() }

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
      val testClass = selector.getJavaClass()
      if (usesRobolectricExtension(testClass)) {
        logExtensionOptOut(testClass)
      } else {
        JupiterDescriptorBuilders.appendTestClass(
          engineDescriptor,
          testClass,
          ::isTestMethod,
          Nested::class.java,
          ::unsupportedKindLabel,
          deps,
        )
      }
    }

    discoveryRequest.getSelectorsByType(MethodSelector::class.java).forEach { selector ->
      val testClass = selector.getJavaClass()
      if (usesRobolectricExtension(testClass)) {
        logExtensionOptOut(testClass)
      } else {
        JupiterDescriptorBuilders.appendTestMethod(
          engineDescriptor,
          testClass,
          selector.getJavaMethod(),
        )
      }
    }

    return engineDescriptor
  }

  /** Determines if a method is a test method by checking for standard @Test annotations. */
  private fun isTestMethod(method: Method): Boolean {
    return AnnotationSupport.isAnnotated(method, Test::class.java) ||
      AnnotationSupport.isAnnotated(method, org.junit.Test::class.java)
  }

  /**
   * Returns a label for test-method kinds this engine cannot execute. `@TestTemplate` covers
   * `@ParameterizedTest`, `@RepeatedTest`, and `@RobolectricSdkTest` via meta-annotation.
   */
  private fun unsupportedKindLabel(method: Method): String? =
    when {
      AnnotationSupport.isAnnotated(method, TestTemplate::class.java) -> "@TestTemplate"
      AnnotationSupport.isAnnotated(method, TestFactory::class.java) -> "@TestFactory"
      else -> null
    }

  /**
   * Classes that declare `@ExtendWith(RobolectricExtension)` (directly, via `@Extensions`, via
   * meta-annotation, or on an enclosing class) opted into the extension path on the standard
   * `junit-jupiter` engine; this engine must not double-run them.
   */
  private fun usesRobolectricExtension(testClass: Class<*>): Boolean {
    var current: Class<*>? = testClass
    while (current != null) {
      val optedIn =
        AnnotationSupport.findRepeatableAnnotations(current, ExtendWith::class.java).any {
          extendWith ->
          extendWith.value.any { it == RobolectricExtension::class }
        }
      if (optedIn) {
        return true
      }
      current = current.enclosingClass
    }
    return false
  }

  private fun logExtensionOptOut(testClass: Class<*>) {
    RunnerLogger.debug {
      "Skipping ${testClass.name}: opted into RobolectricExtension on the junit-jupiter engine"
    }
  }

  /** Orchestrates the execution of the test tree. */
  override fun execute(request: ExecutionRequest) {
    val root = request.rootTestDescriptor
    val listener = request.engineExecutionListener

    // Reuse the lazily created dependencies (shared with discovery-time SDK selection)
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
    val disabledReason = disabledReason(descriptor)
    if (disabledReason != null) {
      listener.executionSkipped(descriptor, disabledReason)
      return
    }

    if (descriptor is JupiterDescriptorBuilders.UnsupportedJupiterMethodDescriptor) {
      listener.executionStarted(descriptor)
      listener.executionFinished(
        descriptor,
        TestExecutionResult.failed(JUnitException(unsupportedKindMessage(descriptor))),
      )
    } else if (descriptor is JupiterDescriptorBuilders.JupiterMethodDescriptor) {
      runTestMethod(
        descriptor,
        descriptor.method,
        presetSdk = null,
        listener,
        sandboxExecutor,
        classLifecycleManager,
        deps,
      )
    } else if (descriptor is JupiterDescriptorBuilders.JupiterSdkMethodDescriptor) {
      runTestMethod(
        descriptor,
        descriptor.method,
        presetSdk = descriptor.sdk,
        listener,
        sandboxExecutor,
        classLifecycleManager,
        deps,
      )
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
    val classContextReady = setupError == null

    // Run @BeforeAll
    if (setupError == null) {
      try {
        invokeStaticLifecycle(classLifecycleManager, testClass, BeforeAll::class.java, "beforeAll")
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

    // Always run @AfterAll (even if tests or @BeforeAll failed) — but only when class setup
    // actually produced a context; invoking it without one would just mask the setup error with
    // a "No class context found" failure.
    try {
      if (classContextReady) {
        invokeStaticLifecycle(classLifecycleManager, testClass, AfterAll::class.java, "afterAll")
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

  /**
   * Core execution logic: bootstraps the sandbox and runs the test method. For per-SDK variant
   * descriptors [presetSdk] carries the SDK chosen at discovery; plain descriptors resolve it via
   * [MethodSdkResolver].
   */
  @Suppress("LongParameterList")
  private fun runTestMethod(
    descriptor: TestDescriptor,
    method: Method,
    presetSdk: Sdk?,
    listener: EngineExecutionListener,
    sandboxExecutor: RobolectricSandboxExecutor,
    classLifecycleManager: ClassLifecycleManager,
    deps: RobolectricDependencies,
  ) {
    listener.executionStarted(descriptor)
    val testClass = method.declaringClass

    val methodSdk = presetSdk ?: MethodSdkResolver.selectMethodSdk(deps, testClass, method)
    val classContext = classLifecycleManager.getClassContext(testClass)

    val executionResult =
      if (methodSdk == null) {
        ExecutionResult.failure(
          IllegalStateException("No SDK selected for ${testClass.name}.${method.name}")
        )
      } else {
        when (
          val executionPolicy =
            ExecutionPolicyResolver.resolve(testClass, method, methodSdk, classContext?.sdk)
        ) {
          is ExecutionPolicy.SharedClassEnvironment -> {
            try {
              classLifecycleManager.executeInClassContext(testClass, method.name) {
                runTestInSandbox(checkNotNull(classContext).sandbox, testClass, method)
              }
              ExecutionResult.success()
            } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
              ExecutionResult.failure(e)
            }
          }
          is ExecutionPolicy.IsolatedMethodEnvironment -> {
            sandboxExecutor.executeSandboxedSafe(testClass, method, executionPolicy.sdk) { sandbox
              ->
              runTestInSandbox(sandbox, testClass, method)
            }
          }
          is ExecutionPolicy.FailFastConflict -> {
            ExecutionResult.failure(IllegalStateException(executionPolicy.message))
          }
        }
      }

    val result =
      when (executionResult) {
        is ExecutionResult.Success -> TestExecutionResult.successful()
        is ExecutionResult.Failure -> TestExecutionResult.failed(executionResult.error)
        is ExecutionResult.Skipped -> TestExecutionResult.aborted(null)
      }

    listener.executionFinished(descriptor, result)
  }

  /** Runs the class's static `@BeforeAll`/`@AfterAll` methods inside the class-level sandbox. */
  private fun invokeStaticLifecycle(
    classLifecycleManager: ClassLifecycleManager,
    testClass: Class<*>,
    annotation: Class<out Annotation>,
    label: String,
  ) {
    classLifecycleManager.executeInClassContext(testClass, "${testClass.simpleName}.<$label>") {
      val sandbox = classLifecycleManager.getClassContext(testClass)!!.sandbox
      val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(sandbox, testClass)
      LifecycleHelper.invokeStaticLifecycleMethods(bootstrappedClass, annotation)
    }
  }

  /** Returns the `@Disabled` reason for a class or method descriptor, or null when enabled. */
  private fun disabledReason(descriptor: TestDescriptor): String? {
    val element: Any? =
      when (descriptor) {
        is JupiterDescriptorBuilders.JupiterClassDescriptor -> descriptor.testClass
        is JupiterDescriptorBuilders.JupiterMethodDescriptor -> descriptor.method
        is JupiterDescriptorBuilders.JupiterSdkMethodDescriptor -> descriptor.method
        is JupiterDescriptorBuilders.UnsupportedJupiterMethodDescriptor -> descriptor.method
        else -> null
      }
    val disabled =
      when (element) {
        is Class<*> -> AnnotationSupport.findAnnotation(element, Disabled::class.java)
        is Method -> AnnotationSupport.findAnnotation(element, Disabled::class.java)
        else -> Optional.empty()
      }
    return disabled.map { it.value.ifEmpty { "@Disabled" } }.orElse(null)
  }

  private fun unsupportedKindMessage(
    descriptor: JupiterDescriptorBuilders.UnsupportedJupiterMethodDescriptor
  ): String {
    val method = descriptor.method
    return "${descriptor.kindLabel} methods are not supported by $ENGINE_ID — run " +
      "${method.declaringClass.name}.${method.name} with " +
      "@ExtendWith(RobolectricExtension::class) on the standard junit-jupiter engine instead."
  }

  private fun hasClassLifecycleMethods(testClass: Class<*>): Boolean =
    LifecycleHelper.hasLifecycleMethods(
      testClass,
      listOf(BeforeAll::class.java, AfterAll::class.java),
    )

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
