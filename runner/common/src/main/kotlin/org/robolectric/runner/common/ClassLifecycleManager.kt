package org.robolectric.runner.common

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages Robolectric environment for class-level lifecycle (@BeforeAll/@AfterAll).
 *
 * This manager creates and maintains a shared Robolectric environment for an entire test class,
 * enabling expensive setup operations to be performed once before all tests run.
 *
 * ## Usage Pattern
 *
 * ```kotlin
 * // Before @BeforeAll methods
 * val context = manager.setupForClass(testClass)
 *
 * // In @BeforeAll methods - Robolectric environment is available
 * val app = ApplicationProvider.getApplicationContext()
 *
 * // After @AfterAll methods
 * manager.tearDownForClass(testClass)
 * ```
 *
 * ## Thread Safety
 *
 * This manager is thread-safe and uses concurrent data structures to track class-level contexts.
 *
 * @since 4.x
 */
@ExperimentalRunnerApi
class ClassLifecycleManager(private val sandboxLifecycleManager: SandboxLifecycleManager) {

  private val classContexts = ConcurrentHashMap<Class<*>, SandboxLifecycleManager.SandboxContext>()
  private val classEnvironments = ConcurrentHashMap<Class<*>, RobolectricEnvironment>()

  /**
   * Sets up Robolectric environment before @BeforeAll methods execute.
   *
   * This creates a sandbox and initializes the Android environment that will be shared across all
   * tests in the class.
   *
   * @param testClass The test class being executed
   * @return SandboxContext that can be used for executing lifecycle methods
   */
  fun setupForClass(testClass: Class<*>): SandboxLifecycleManager.SandboxContext {
    // Check if we already have a context for this class
    classContexts[testClass]?.let { existingContext ->
      RunnerLogger.logClassContextReused(testClass.simpleName)
      RunnerMetrics.recordSandboxCacheHit()
      return existingContext
    }

    // Record cache miss - need to create new context
    RunnerMetrics.recordSandboxCacheMiss()

    // Create sandbox for the entire class (timed)
    val context =
      RunnerMetrics.timed(RunnerMetrics.PHASE_CLASS_SETUP) {
        sandboxLifecycleManager.createSandbox(testClass, testMethod = null)
      }

    // Store it for later retrieval
    classContexts[testClass] = context

    // Create and setup persistent environment
    val environment = sandboxLifecycleManager.getEnvironment(context)
    classEnvironments[testClass] = environment

    // Initialize application state on main thread (timed)
    RunnerMetrics.timed(RunnerMetrics.PHASE_ENVIRONMENT_SETUP) {
      context.sandbox.runOnMainThread {
        environment.access {
          environment.setupApplicationState("${testClass.simpleName}.<classSetup>")
        }
      }
    }

    // Log class context creation
    RunnerLogger.logClassContextCreated(testClass.simpleName, context.sdk.apiLevel)

    return context
  }

  /**
   * Tears down the Robolectric environment after @AfterAll methods execute.
   *
   * This cleans up the shared sandbox and releases resources.
   *
   * @param testClass The test class that completed execution
   */
  fun tearDownForClass(testClass: Class<*>) {
    val environment = classEnvironments.remove(testClass)
    val context = classContexts.remove(testClass)

    if (environment != null && context != null) {
      RunnerMetrics.timed(RunnerMetrics.PHASE_CLASS_TEARDOWN) {
        context.sandbox.runOnMainThread {
          environment.access { environment.tearDownApplicationState() }
        }
      }
      RunnerMetrics.recordSandboxTeardown()
      RunnerLogger.logClassContextTeardown(testClass.simpleName)
    }
  }

  /**
   * Gets the shared sandbox context for a test class, if it exists.
   *
   * This allows test methods to reuse the class-level sandbox if available, or create their own if
   * needed.
   *
   * @param testClass The test class
   * @return SandboxContext if class-level setup was performed, null otherwise
   */
  fun getClassContext(testClass: Class<*>): SandboxLifecycleManager.SandboxContext? {
    val context = classContexts[testClass]
    if (context != null) {
      RunnerLogger.logSandboxReuse(testClass.simpleName, context.sdk.apiLevel)
    }
    return context
  }

  /**
   * Executes a class-level lifecycle method (e.g., @BeforeAll) within the sandbox.
   *
   * @param testClass The test class
   * @param methodName Name of the lifecycle method for logging
   * @param block The code to execute
   */
  fun <T> executeInClassContext(
    testClass: Class<*>,
    methodName: String,
    configuration: org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration? = null,
    block: () -> T,
  ): T {
    val context = classContexts[testClass]
    check(context != null) {
      "No class context found for $testClass. Did you call setupForClass() first?"
    }

    val environment = classEnvironments[testClass]
    // If configuration is provided and differs, we might need a temporary environment
    // But for now, we assume persistent environment is desired if it exists.
    // If configuration is provided, we should probably use it, but that would break persistence.
    // For "same instance" requirement, we must use the persistent environment.

    try {
      return context.sandbox.runOnMainThread(
        Callable {
          if (environment != null && configuration == null) {
            // Use persistent environment
            environment.access(block)
          } else {
            // Fallback or specific configuration (non-persistent execution)
            sandboxLifecycleManager.executeInSandbox(context, methodName, configuration, block)
          }
        }
      )
    } catch (e: java.lang.reflect.InvocationTargetException) {
      throw e.targetException ?: e
    }
  }
}
