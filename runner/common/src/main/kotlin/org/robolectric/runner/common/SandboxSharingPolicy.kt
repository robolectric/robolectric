package org.robolectric.runner.common

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import org.robolectric.runner.common.SandboxLifecycleManager.SandboxContext

/**
 * Strategy object owning all per-sharing-strategy state and lifecycle logic for
 * [DefaultRobolectricIntegration]. Lets the integration class delegate instead of running four-way
 * `when` branches over [SandboxSharingStrategy].
 *
 * One instance per [DefaultRobolectricIntegration]. Each policy owns its own caches and is
 * responsible for emitting the right [RunnerMetrics] events.
 *
 * This is an internal detail of [DefaultRobolectricIntegration]; third-party framework adapters
 * should not need to implement it directly. If they do, they should use the façade (see
 * [RobolectricIntegration]) rather than building new policies.
 */
@ExperimentalRunnerApi
internal sealed interface SandboxSharingPolicy {

  /** Called once per test class before any of its tests run. */
  fun beforeClass(testClass: Class<*>)

  /** Called before each test method. May create or look up a cached sandbox. */
  fun beforeTest(testClass: Class<*>, testMethod: Method)

  /** Called after each test method. May release per-test state. */
  fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean)

  /** Called once per test class after all of its tests finish. */
  fun afterClass(testClass: Class<*>)

  /**
   * Returns the cached [SandboxContext] for a given test, if any. Returns null if the caller hasn't
   * prepared one yet (e.g. the strategy needs a testMethod to key its cache but was called with
   * null).
   */
  fun getSandboxContext(testClass: Class<*>, testMethod: Method?): SandboxContext?

  companion object {
    fun create(
      strategy: SandboxSharingStrategy,
      lifecycleManager: SandboxLifecycleManager,
      classLifecycleManager: ClassLifecycleManager,
      resolveSourceClassLoader: (Class<*>) -> ClassLoader?,
    ): SandboxSharingPolicy =
      when (strategy) {
        SandboxSharingStrategy.PER_TEST -> PerTestPolicy(lifecycleManager, resolveSourceClassLoader)
        SandboxSharingStrategy.PER_CLASS ->
          PerClassPolicy(classLifecycleManager, resolveSourceClassLoader)
        SandboxSharingStrategy.PER_SDK -> PerSdkPolicy(lifecycleManager, resolveSourceClassLoader)
        SandboxSharingStrategy.GLOBAL -> GlobalPolicy(lifecycleManager, resolveSourceClassLoader)
      }
  }
}

private fun testKey(testClass: Class<*>, testMethod: Method) =
  "${testClass.name}.${testMethod.name}"

@ExperimentalRunnerApi
private class PerTestPolicy(
  private val lifecycleManager: SandboxLifecycleManager,
  private val resolveSourceClassLoader: (Class<*>) -> ClassLoader?,
) : SandboxSharingPolicy {
  private val testContexts = ConcurrentHashMap<String, SandboxContext>()

  override fun beforeClass(testClass: Class<*>) = Unit

  override fun beforeTest(testClass: Class<*>, testMethod: Method) {
    val context =
      lifecycleManager.createSandbox(testClass, testMethod, resolveSourceClassLoader(testClass))
    testContexts[testKey(testClass, testMethod)] = context
    RunnerMetrics.recordSandboxCreation()
  }

  override fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean) {
    testContexts.remove(testKey(testClass, testMethod))
    RunnerMetrics.recordSandboxTeardown()
  }

  override fun afterClass(testClass: Class<*>) = Unit

  override fun getSandboxContext(testClass: Class<*>, testMethod: Method?): SandboxContext? {
    if (testMethod == null) return null
    return testContexts[testKey(testClass, testMethod)]
  }
}

@ExperimentalRunnerApi
private class PerClassPolicy(
  private val classLifecycleManager: ClassLifecycleManager,
  private val resolveSourceClassLoader: (Class<*>) -> ClassLoader?,
) : SandboxSharingPolicy {
  private val classContexts = ConcurrentHashMap<Class<*>, SandboxContext>()

  override fun beforeClass(testClass: Class<*>) {
    val context =
      classLifecycleManager.setupForClass(testClass, resolveSourceClassLoader(testClass))
    classContexts[testClass] = context
  }

  override fun beforeTest(testClass: Class<*>, testMethod: Method) {
    RunnerMetrics.recordSandboxCacheHit()
  }

  override fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean) = Unit

  override fun afterClass(testClass: Class<*>) {
    classLifecycleManager.tearDownForClass(testClass)
    classContexts.remove(testClass)
    RunnerMetrics.recordSandboxTeardown()
  }

  override fun getSandboxContext(testClass: Class<*>, testMethod: Method?): SandboxContext? =
    classContexts[testClass] ?: classLifecycleManager.getClassContext(testClass)
}

@ExperimentalRunnerApi
private class PerSdkPolicy(
  private val lifecycleManager: SandboxLifecycleManager,
  private val resolveSourceClassLoader: (Class<*>) -> ClassLoader?,
) : SandboxSharingPolicy {
  private val sdkContexts = ConcurrentHashMap<Int, SandboxContext>()
  private val testSdkLevels = ConcurrentHashMap<String, Int>()

  override fun beforeClass(testClass: Class<*>) = Unit

  override fun beforeTest(testClass: Class<*>, testMethod: Method) {
    val key = testKey(testClass, testMethod)
    val existingSdkLevel = testSdkLevels[key]
    if (existingSdkLevel != null && sdkContexts.containsKey(existingSdkLevel)) {
      RunnerMetrics.recordSandboxCacheHit()
      return
    }
    val tempContext =
      lifecycleManager.createSandbox(testClass, testMethod, resolveSourceClassLoader(testClass))
    val sdkLevel = tempContext.sdk.apiLevel
    testSdkLevels[key] = sdkLevel
    if (sdkContexts.putIfAbsent(sdkLevel, tempContext) == null) {
      RunnerMetrics.recordSandboxCreation()
    } else {
      RunnerMetrics.recordSandboxCacheHit()
    }
  }

  override fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean) {
    testSdkLevels.remove(testKey(testClass, testMethod))
  }

  override fun afterClass(testClass: Class<*>) = Unit

  override fun getSandboxContext(testClass: Class<*>, testMethod: Method?): SandboxContext? {
    if (testMethod == null) return null
    val key = testKey(testClass, testMethod)
    val sdkLevel =
      testSdkLevels[key]
        ?: run {
          // Lazy-initialize when callers ask for a context before beforeTest ran (e.g. discovery).
          val tempContext =
            lifecycleManager.createSandbox(
              testClass,
              testMethod,
              resolveSourceClassLoader(testClass),
            )
          testSdkLevels[key] = tempContext.sdk.apiLevel
          tempContext.sdk.apiLevel
        }
    return sdkContexts[sdkLevel]
  }
}

@ExperimentalRunnerApi
private class GlobalPolicy(
  private val lifecycleManager: SandboxLifecycleManager,
  private val resolveSourceClassLoader: (Class<*>) -> ClassLoader?,
) : SandboxSharingPolicy {
  @Volatile private var globalContext: SandboxContext? = null
  private val lock = Any()

  override fun beforeClass(testClass: Class<*>) {
    getOrCreate(testClass)
  }

  override fun beforeTest(testClass: Class<*>, testMethod: Method) {
    RunnerMetrics.recordSandboxCacheHit()
  }

  override fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean) = Unit

  override fun afterClass(testClass: Class<*>) = Unit

  override fun getSandboxContext(testClass: Class<*>, testMethod: Method?): SandboxContext? =
    globalContext

  private fun getOrCreate(testClass: Class<*>): SandboxContext {
    globalContext?.let {
      return it
    }
    return synchronized(lock) {
      globalContext
        ?: lifecycleManager
          .createSandbox(testClass, sourceClassLoader = resolveSourceClassLoader(testClass))
          .also {
            globalContext = it
            RunnerMetrics.recordSandboxCreation()
          }
    }
  }
}
