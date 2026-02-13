package org.robolectric.runner.common

import java.lang.reflect.Method
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap

/**
 * Default implementation of [RobolectricIntegration].
 *
 * This class provides a fully-functional Robolectric integration that can be used by any test
 * framework. It handles:
 * - Sandbox creation and lifecycle
 * - Environment setup and teardown
 * - Class-level and test-level sharing
 * - Observability (logging and metrics)
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
 * @property dependencies The Robolectric dependencies (lazily initialized)
 */
@ExperimentalRunnerApi
class DefaultRobolectricIntegration(
  private val sandboxSharing: SandboxSharingStrategy = SandboxSharingStrategy.PER_CLASS,
  private val parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver,
  private val dependencies: RobolectricDependencies = RobolectricDependencies.create(),
) : RobolectricIntegration {
  private val lifecycleManager = SandboxLifecycleManager(dependencies)
  private val classLifecycleManager = ClassLifecycleManager(lifecycleManager)
  // Per-class sandbox contexts for PER_CLASS strategy
  private val classContexts = ConcurrentHashMap<Class<*>, SandboxLifecycleManager.SandboxContext>()
  // Per-test sandbox contexts for PER_TEST strategy
  private val testContexts = ConcurrentHashMap<String, SandboxLifecycleManager.SandboxContext>()
  // Global sandbox context for GLOBAL strategy
  @Volatile private var globalContext: SandboxLifecycleManager.SandboxContext? = null
  // Per-SDK sandbox contexts for PER_SDK strategy
  private val sdkContexts = ConcurrentHashMap<Int, SandboxLifecycleManager.SandboxContext>()

  override fun beforeClass(testClass: Class<*>) {
    RunnerLogger.debug { "beforeClass: ${testClass.simpleName}" }
    when (sandboxSharing) {
      SandboxSharingStrategy.PER_CLASS -> {
        val context = classLifecycleManager.setupForClass(testClass)
        classContexts[testClass] = context
      }
      SandboxSharingStrategy.GLOBAL -> {
        if (globalContext == null) {
          synchronized(this) {
            if (globalContext == null) {
              globalContext = lifecycleManager.createSandbox(testClass)
              RunnerMetrics.recordSandboxCreation()
            }
          }
        }
      }
      SandboxSharingStrategy.PER_SDK,
      SandboxSharingStrategy.PER_TEST -> {
        // No class-level setup needed
      }
    }
  }

  override fun beforeTest(testClass: Class<*>, testMethod: Method) {
    val testKey = "${testClass.name}.${testMethod.name}"
    RunnerLogger.debug { "beforeTest: $testKey" }
    when (sandboxSharing) {
      SandboxSharingStrategy.PER_TEST -> {
        val context = lifecycleManager.createSandbox(testClass, testMethod)
        testContexts[testKey] = context
        RunnerMetrics.recordSandboxCreation()
      }
      SandboxSharingStrategy.PER_SDK -> {
        val tempContext = lifecycleManager.createSandbox(testClass, testMethod)
        val sdkLevel = tempContext.sdk.apiLevel
        if (!sdkContexts.containsKey(sdkLevel)) {
          sdkContexts[sdkLevel] = tempContext
          RunnerMetrics.recordSandboxCreation()
        } else {
          RunnerMetrics.recordSandboxCacheHit()
        }
      }
      SandboxSharingStrategy.PER_CLASS,
      SandboxSharingStrategy.GLOBAL -> {
        RunnerMetrics.recordSandboxCacheHit()
      }
    }
  }

  override fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean) {
    val testKey = "${testClass.name}.${testMethod.name}"
    RunnerLogger.debug { "afterTest: $testKey, success=$success" }
    when (sandboxSharing) {
      SandboxSharingStrategy.PER_TEST -> {
        testContexts.remove(testKey)
        RunnerMetrics.recordSandboxTeardown()
      }
      SandboxSharingStrategy.PER_CLASS,
      SandboxSharingStrategy.PER_SDK,
      SandboxSharingStrategy.GLOBAL -> {
        // Sandbox persists, just reset environment
      }
    }
    RunnerMetrics.recordTestExecution(success)
  }

  override fun afterClass(testClass: Class<*>) {
    RunnerLogger.debug { "afterClass: ${testClass.simpleName}" }
    when (sandboxSharing) {
      SandboxSharingStrategy.PER_CLASS -> {
        classLifecycleManager.tearDownForClass(testClass)
        classContexts.remove(testClass)
        RunnerMetrics.recordSandboxTeardown()
      }
      SandboxSharingStrategy.PER_TEST,
      SandboxSharingStrategy.PER_SDK,
      SandboxSharingStrategy.GLOBAL -> {
        // No class-level teardown needed
      }
    }
  }

  override fun getContext(testClass: Class<*>): TestExecutionContext? {
    val sandboxContext = getSandboxContext(testClass, null) ?: return null
    return TestExecutionContext.fromSandboxContext(sandboxContext, testClass)
  }

  @Suppress("TooGenericExceptionCaught")
  override fun <T> executeInSandbox(
    testClass: Class<*>,
    testMethod: Method,
    block: (TestExecutionContext) -> T,
  ): T {
    val sandboxContext = getSandboxContext(testClass, testMethod)
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
    } catch (e: java.lang.reflect.InvocationTargetException) {
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

  private fun getSandboxContext(
    testClass: Class<*>,
    testMethod: Method?,
  ): SandboxLifecycleManager.SandboxContext? {
    return when (sandboxSharing) {
      SandboxSharingStrategy.PER_TEST -> {
        if (testMethod != null) {
          val testKey = "${testClass.name}.${testMethod.name}"
          testContexts[testKey]
        } else {
          null
        }
      }
      SandboxSharingStrategy.PER_CLASS -> {
        classContexts[testClass] ?: classLifecycleManager.getClassContext(testClass)
      }
      SandboxSharingStrategy.PER_SDK -> {
        if (testMethod != null) {
          val tempContext = lifecycleManager.createSandbox(testClass, testMethod)
          sdkContexts[tempContext.sdk.apiLevel]
        } else {
          null
        }
      }
      SandboxSharingStrategy.GLOBAL -> globalContext
    }
  }
}
