package org.robolectric.runner.common

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.Callable

/**
 * Default implementation of [RobolectricIntegration].
 *
 * Owns the pieces that are invariant across sharing strategies:
 * - The [SandboxLifecycleManager] / [ClassLifecycleManager] pair
 * - Test-execution and error-unwrapping in [executeInSandbox]
 * - Observability wiring (logging + metrics)
 *
 * Per-strategy cache and lifecycle logic lives in [SandboxSharingPolicy], which this class
 * delegates to. That split keeps each strategy independently testable and removes the four-way
 * `when` branches that used to live in every lifecycle callback.
 *
 * ## Usage
 *
 * ```kotlin
 * val integration = DefaultRobolectricIntegration(
 *   sandboxSharing = SandboxSharingStrategy.PER_CLASS,
 *   parameterResolver = DefaultRobolectricParameterResolver,
 * )
 *
 * // In your framework adapter:
 * integration.beforeClass(testClass)
 * integration.beforeTest(testClass, testMethod)
 * integration.executeInSandbox(testClass, testMethod) { context ->
 *   // Run test
 * }
 * integration.afterTest(testClass, testMethod, success = true)
 * integration.afterClass(testClass)
 * ```
 *
 * @property sandboxSharing The strategy for sharing sandboxes across tests
 * @property parameterResolver The resolver for test method parameters
 * @property dependencies The Robolectric dependencies
 */
@ExperimentalRunnerApi
class DefaultRobolectricIntegration(
  private val sandboxSharing: SandboxSharingStrategy = SandboxSharingStrategy.PER_CLASS,
  private val parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver,
  private val dependencies: RobolectricDependencies = RobolectricDependencies.create(),
) : RobolectricIntegration {
  private val lifecycleManager = SandboxLifecycleManager(dependencies)
  private val classLifecycleManager = ClassLifecycleManager(lifecycleManager)

  private val policy: SandboxSharingPolicy =
    SandboxSharingPolicy.create(
      strategy = sandboxSharing,
      lifecycleManager = lifecycleManager,
      classLifecycleManager = classLifecycleManager,
      resolveSourceClassLoader = ::resolveSourceClassLoader,
    )

  override fun beforeClass(testClass: Class<*>) {
    RunnerLogger.debug { "beforeClass: ${testClass.simpleName}" }
    policy.beforeClass(testClass)
  }

  override fun beforeTest(testClass: Class<*>, testMethod: Method) {
    RunnerLogger.debug { "beforeTest: ${testClass.name}.${testMethod.name}" }
    policy.beforeTest(testClass, testMethod)
  }

  override fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean) {
    RunnerLogger.debug { "afterTest: ${testClass.name}.${testMethod.name}, success=$success" }
    policy.afterTest(testClass, testMethod, success)
    RunnerMetrics.recordTestExecution(success)
  }

  override fun afterClass(testClass: Class<*>) {
    RunnerLogger.debug { "afterClass: ${testClass.simpleName}" }
    policy.afterClass(testClass)
  }

  override fun getContext(testClass: Class<*>): TestExecutionContext? {
    val sandboxContext = policy.getSandboxContext(testClass, null) ?: return null
    return TestExecutionContext.fromSandboxContext(sandboxContext, testClass)
  }

  @Suppress("TooGenericExceptionCaught")
  override fun <T> executeInSandbox(
    testClass: Class<*>,
    testMethod: Method,
    block: (TestExecutionContext) -> T,
  ): T {
    val sandboxContext = policy.getSandboxContext(testClass, testMethod)
    checkNotNull(sandboxContext) {
      "No sandbox context available for ${testClass.name}.${testMethod.name}"
    }
    val context = TestExecutionContext.fromSandboxContext(sandboxContext, testClass, testMethod)
    val startTime = System.currentTimeMillis()
    RunnerLogger.logTestStart(testClass.simpleName, testMethod.name, context.sdk.apiLevel)
    return try {
      val result =
        sandboxContext.sandbox.runOnMainThread(
          Callable {
            lifecycleManager.executeInSandbox(sandboxContext, testMethod.name) { block(context) }
          }
        )
      val duration = System.currentTimeMillis() - startTime
      RunnerLogger.logTestEnd(testClass.simpleName, testMethod.name, duration, success = true)
      RunnerMetrics.recordTiming(RunnerMetrics.PHASE_TEST_EXECUTION, duration)
      result
    } catch (e: InvocationTargetException) {
      val duration = System.currentTimeMillis() - startTime
      RunnerLogger.logTestEnd(testClass.simpleName, testMethod.name, duration, success = false)
      throw e.targetException ?: e
    } catch (e: Throwable) {
      val duration = System.currentTimeMillis() - startTime
      RunnerLogger.logTestEnd(testClass.simpleName, testMethod.name, duration, success = false)
      throw e
    }
  }

  /** Gets the parameter resolver for this integration. */
  fun getParameterResolver(): ParameterResolver = parameterResolver

  /** Gets the lifecycle manager for advanced use cases. */
  fun getLifecycleManager(): SandboxLifecycleManager = lifecycleManager

  /** Gets the class lifecycle manager for class-level operations. */
  fun getClassLifecycleManager(): ClassLifecycleManager = classLifecycleManager

  private fun resolveSourceClassLoader(testClass: Class<*>): ClassLoader? =
    dependencies.frameworkClassLoadingBridge.resolveSourceClassLoader(testClass)
}
