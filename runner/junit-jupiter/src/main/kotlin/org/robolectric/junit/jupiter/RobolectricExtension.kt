package org.robolectric.junit.jupiter

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.robolectric.internal.AndroidSandbox
import org.robolectric.runner.common.ClassLifecycleManager
import org.robolectric.runner.common.ExecutionPolicyResolver
import org.robolectric.runner.common.ExecutionPolicyResolver.ExecutionPolicy
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.LifecycleHelper
import org.robolectric.runner.common.MethodSdkResolver
import org.robolectric.runner.common.RunnerLogger
import org.robolectric.runner.common.RunnerMetrics
import org.robolectric.runner.common.TestBootstrapper
import org.robolectric.runner.common.TestMethodInvoker

/**
 * JUnit Jupiter extension for Robolectric.
 *
 * ## How it works
 *
 * Jupiter instantiates the test class on the application classloader, but Robolectric tests must
 * run on classes loaded through the sandbox classloader. The extension therefore treats Jupiter's
 * instance as a placeholder: every test-method (and `@BeforeEach`/`@AfterEach`/`@BeforeAll`/
 * `@AfterAll`) invocation is intercepted, the original invocation is skipped, and the equivalent
 * method is invoked on a sandbox-loaded twin of the test class inside the Robolectric environment.
 *
 * ## Execution policy
 *
 * Where a test runs is decided by [ExecutionPolicyResolver], the same policy the
 * `robolectric-junit-jupiter-engine` uses:
 * - Classes **without** `@BeforeAll`/`@AfterAll` get an isolated per-method environment, honoring
 *   full method-level [org.robolectric.annotation.Config] — classic Robolectric semantics.
 * - Classes **with** `@BeforeAll`/`@AfterAll` share one persistent class-level environment (created
 *   in [beforeAll]); `@RobolectricSdkTest` invocations are the explicit exception and run in their
 *   own per-SDK sandboxes, where class-environment state is not visible.
 * - A plain method-level `@Config` override on a class with `@BeforeAll`/`@AfterAll` is an implicit
 *   conflict and fails fast with an actionable message.
 *
 * Test-method parameters ([android.content.Context], [android.app.Application],
 * `ActivityController`, `ServiceController`) are resolved sandbox-side; the values this extension
 * reports to Jupiter's own parameter-resolution phase are placeholders.
 *
 * ## Usage
 *
 * ```kotlin
 * @ExtendWith(RobolectricExtension::class)
 * class MyTest {
 *   @Test
 *   fun testWithContext(context: Context) {
 *     // context is automatically injected
 *   }
 * }
 * ```
 *
 * ## Mutually exclusive with [RobolectricJupiterEngine]
 *
 * [RobolectricJupiterEngine] skips classes annotated with `@ExtendWith(RobolectricExtension)`
 * during discovery, so having both engines on the classpath does not double-run them: annotated
 * classes belong to the standard `junit-jupiter` engine via this extension.
 */
@Suppress("TooManyFunctions")
@OptIn(ExperimentalRunnerApi::class)
class RobolectricExtension :
  BeforeAllCallback, AfterAllCallback, InvocationInterceptor, ParameterResolver {

  companion object {
    private val NAMESPACE = JupiterSharedState.NAMESPACE
    private const val CLASS_LIFECYCLE_KEY = "classLifecycle"

    /**
     * Parameter types the sandbox-side resolver can inject. Must stay in sync with
     * [org.robolectric.runner.common.DefaultRobolectricParameterResolver].
     */
    private val SUPPORTED_PARAMETER_TYPES =
      setOf(
        "android.content.Context",
        "android.app.Application",
        "org.robolectric.android.controller.ActivityController",
        "org.robolectric.android.controller.ServiceController",
      )
  }

  override fun beforeAll(context: ExtensionContext) {
    val testClass = context.requiredTestClass

    // Only classes with @BeforeAll/@AfterAll get a shared class environment; everything else
    // runs in isolated per-method environments (see ExecutionPolicyResolver).
    if (
      !LifecycleHelper.hasLifecycleMethods(
        testClass,
        listOf(BeforeAll::class.java, AfterAll::class.java),
      )
    ) {
      return
    }

    val sandboxLifecycleManager = JupiterSharedState.sandboxManager(context)
    val classLifecycleManager = ClassLifecycleManager(sandboxLifecycleManager)

    classLifecycleManager.setupForClass(testClass)

    // Store the manager for use by the interceptors; closing the store tears the class down
    context
      .getStore(NAMESPACE)
      .put(CLASS_LIFECYCLE_KEY, ClassLifecycleResource(testClass, classLifecycleManager))
  }

  override fun afterAll(context: ExtensionContext) {
    // Cleanup is handled automatically by ClassLifecycleResource
  }

  override fun supportsParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Boolean = parameterContext.parameter.type.name in SUPPORTED_PARAMETER_TYPES

  /**
   * Returns a placeholder (`null`) for Jupiter's own parameter-resolution phase. The original
   * method invocation these values would feed is always skipped; the real arguments are resolved
   * against the sandbox classloader by [TestMethodInvoker].
   */
  override fun resolveParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Any? = null

  override fun interceptTestMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) = interceptSandboxedTest(invocation, invocationContext, extensionContext)

  override fun interceptTestTemplateMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) = interceptSandboxedTest(invocation, invocationContext, extensionContext)

  override fun interceptBeforeEachMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) = skipPlaceholderLifecycle(invocation)

  override fun interceptAfterEachMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) = skipPlaceholderLifecycle(invocation)

  override fun interceptBeforeAllMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) = interceptClassLifecycleMethod(invocation, invocationContext, extensionContext)

  override fun interceptAfterAllMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) = interceptClassLifecycleMethod(invocation, invocationContext, extensionContext)

  /**
   * Runs a test (or test-template) method on a sandbox-loaded instance of the test class.
   *
   * The original invocation targets the placeholder instance Jupiter created on the application
   * classloader, so it is skipped. `@RobolectricSdkTest` invocations run in the per-SDK sandbox
   * published by their template provider; everything else is routed by [ExecutionPolicyResolver].
   */
  private fun interceptSandboxedTest(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) {
    val testClass = extensionContext.requiredTestClass
    val method = invocationContext.executable

    val templateContext = JupiterSharedState.sdkTemplateContext(extensionContext)
    if (templateContext != null) {
      invocation.skip()
      runLogged(testClass, method, templateContext.sdk.apiLevel) {
        val lifecycleManager = JupiterSharedState.sandboxManager(extensionContext)
        templateContext.sandbox.runOnMainThread {
          lifecycleManager.executeInSandbox(templateContext, method.name) {
            invokeTestMethod(templateContext.sandbox, testClass, method)
          }
        }
      }
      return
    }

    val classResource = classLifecycleResource(extensionContext)
    val classContext = classResource?.manager?.getClassContext(testClass)
    val deps = JupiterSharedState.dependencies(extensionContext)
    val methodSdk = MethodSdkResolver.selectMethodSdk(deps, testClass, method)
    if (methodSdk == null) {
      invocation.skip()
      error("No SDK selected for ${testClass.name}.${method.name}")
    }

    when (
      val policy = ExecutionPolicyResolver.resolve(testClass, method, methodSdk, classContext?.sdk)
    ) {
      is ExecutionPolicy.SharedClassEnvironment -> {
        invocation.skip()
        runLogged(testClass, method, checkNotNull(classContext).sdk.apiLevel) {
          checkNotNull(classResource).manager.executeInClassContext(testClass, method.name) {
            invokeTestMethod(classContext.sandbox, testClass, method)
          }
        }
      }
      is ExecutionPolicy.IsolatedMethodEnvironment -> {
        invocation.skip()
        // executeSandboxed does its own logging/metrics and main-thread scheduling.
        JupiterSharedState.sandboxExecutor(extensionContext).executeSandboxed(testClass, method) {
          sandbox ->
          invokeTestMethod(sandbox, testClass, method)
        }
      }
      is ExecutionPolicy.FailFastConflict -> {
        invocation.skip()
        error(policy.message)
      }
    }
  }

  private fun invokeTestMethod(sandbox: AndroidSandbox, testClass: Class<*>, testMethod: Method) {
    TestMethodInvoker.invoke(
      sandbox = sandbox,
      testClass = testClass,
      testMethod = testMethod,
      beforeEachAnnotations = listOf(BeforeEach::class.java),
      afterEachAnnotations = listOf(AfterEach::class.java),
    )
  }

  private fun runLogged(testClass: Class<*>, method: Method, apiLevel: Int, block: () -> Unit) {
    val startTime = System.currentTimeMillis()
    var success = false
    try {
      RunnerLogger.logTestStart(testClass.simpleName, method.name, apiLevel)
      RunnerMetrics.timed(RunnerMetrics.PHASE_TEST_EXECUTION) { block() }
      success = true
    } finally {
      val duration = System.currentTimeMillis() - startTime
      RunnerLogger.logTestEnd(testClass.simpleName, method.name, duration, success)
      RunnerMetrics.recordTestExecution(success)
    }
  }

  /**
   * Skips `@BeforeEach`/`@AfterEach` invocations on the placeholder instance: every test runs
   * sandboxed (or fails fast before per-test lifecycle matters), and [TestMethodInvoker] invokes
   * these methods on the sandbox-side instance instead.
   */
  private fun skipPlaceholderLifecycle(invocation: InvocationInterceptor.Invocation<Void?>) {
    invocation.skip()
  }

  /**
   * Executes a `@BeforeAll`/`@AfterAll` method inside the class-level sandbox when one exists.
   *
   * The static method Jupiter is about to invoke lives on the application classloader; its
   * sandbox-loaded counterpart is invoked instead so Android state is touched inside the sandbox.
   * The invocation is always consumed exactly once: without a class context (or for methods with
   * parameters, which cannot be resolved sandbox-side) the original invocation proceeds — silently
   * dropping it would make Jupiter fail with a confusing "invocation was never proceeded" error.
   */
  private fun interceptClassLifecycleMethod(
    invocation: InvocationInterceptor.Invocation<Void?>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext,
  ) {
    val classResource = classLifecycleResource(extensionContext)
    val testClass = extensionContext.requiredTestClass
    val classContext = classResource?.manager?.getClassContext(testClass)
    val original = invocationContext.executable

    if (classResource == null || classContext == null || original.parameterCount > 0) {
      invocation.proceed()
      return
    }

    invocation.skip()
    classResource.manager.executeInClassContext(testClass, original.name) {
      invokeBootstrappedStatic(classContext.sandbox, original)
    }
  }

  @Suppress("SpreadOperator")
  private fun invokeBootstrappedStatic(sandbox: AndroidSandbox, original: Method) {
    val declaringClass = TestBootstrapper.bootstrapClass<Any>(sandbox, original.declaringClass)
    val parameterTypes = TestBootstrapper.bootstrapParameterTypes(sandbox, original.parameterTypes)
    val method = declaringClass.getDeclaredMethod(original.name, *parameterTypes)
    method.isAccessible = true
    try {
      method.invoke(null)
    } catch (e: InvocationTargetException) {
      throw e.targetException ?: e
    }
  }

  private fun classLifecycleResource(context: ExtensionContext): ClassLifecycleResource? =
    context.getStore(NAMESPACE).get(CLASS_LIFECYCLE_KEY) as? ClassLifecycleResource

  /**
   * Holds the class lifecycle manager and implements [AutoCloseable] so the class-level Robolectric
   * environment is torn down when Jupiter closes the class-level store.
   */
  private class ClassLifecycleResource(
    private val testClass: Class<*>,
    val manager: ClassLifecycleManager,
  ) : AutoCloseable {
    override fun close() {
      manager.tearDownForClass(testClass)
    }
  }
}
