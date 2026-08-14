package org.robolectric.runner.common

import java.lang.reflect.Method
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.Sdk

/**
 * Canonical execution-policy decision shared by every Jupiter entry point (the
 * `robolectric-junit-jupiter-engine` and `RobolectricExtension`), so a test class behaves
 * identically regardless of which path executes it.
 *
 * The rule is explicit-intent-based:
 * - **No class lifecycle** (no shared class environment): every test runs in an isolated per-method
 *   environment honoring full method-level [Config] — classic Robolectric semantics.
 * - **Class lifecycle present:**
 *     - a method whose effective configuration matches the class environment shares it;
 *     - explicit isolation (`@RobolectricSdkTest` template invocations) opts into isolated per-SDK
 *       sandboxes — allowed, but class-environment state is not visible there;
 *     - implicit divergence (plain method-level `@Config` overrides, or an SDK differing from the
 *       class environment) fails fast with an actionable message.
 */
@ExperimentalRunnerApi
object ExecutionPolicyResolver {

  /** How a single test method should be executed relative to the class-level environment. */
  sealed interface ExecutionPolicy {
    /** Run inside the persistent class-level environment the caller already holds. */
    object SharedClassEnvironment : ExecutionPolicy

    /** Run in an isolated per-method environment on the given SDK. */
    data class IsolatedMethodEnvironment(val sdk: Sdk) : ExecutionPolicy

    /** The combination is not executable; fail the test with [message]. */
    data class FailFastConflict(val message: String) : ExecutionPolicy
  }

  /**
   * Resolves the execution policy for one test method.
   *
   * @param testClass the test class
   * @param method the test method
   * @param methodSdk the SDK selected for the method (see [MethodSdkResolver])
   * @param classContextSdk the SDK of the shared class environment, or null when the class has no
   *   shared environment
   * @param explicitIsolation true when the caller explicitly requested an isolated sandbox (e.g. a
   *   `@RobolectricSdkTest` template invocation), bypassing conflict detection
   */
  fun resolve(
    testClass: Class<*>,
    method: Method,
    methodSdk: Sdk,
    classContextSdk: Sdk?,
    explicitIsolation: Boolean = false,
  ): ExecutionPolicy {
    if (explicitIsolation || classContextSdk == null) {
      return ExecutionPolicy.IsolatedMethodEnvironment(methodSdk)
    }

    val conflictMessage =
      if (classContextSdk.apiLevel != methodSdk.apiLevel) {
        "Configuration conflict for ${testClass.name}.${method.name}: class lifecycle uses " +
          "shared SDK ${classContextSdk.apiLevel} but method requested SDK " +
          "${methodSdk.apiLevel}. Tests with @BeforeAll/@AfterAll share one Android environment " +
          "and cannot change SDK per method. Move this test to a separate class without " +
          "@BeforeAll/@AfterAll, or annotate the method with @RobolectricSdkTest for explicit " +
          "per-SDK isolation."
      } else if (hasConflictingMethodLevelConfig(method)) {
        val methodConfig = method.getAnnotation(Config::class.java)
        "Configuration conflict for ${testClass.name}.${method.name}: method-level @Config " +
          "overrides (${describeMethodOverrides(methodConfig)}) are not supported when the " +
          "class declares @BeforeAll/@AfterAll. Move this test to a separate class without " +
          "class lifecycle callbacks."
      } else {
        null
      }

    return if (conflictMessage != null) {
      ExecutionPolicy.FailFastConflict(conflictMessage)
    } else {
      ExecutionPolicy.SharedClassEnvironment
    }
  }

  /** Returns true when [method] declares `@Config` values that diverge from the class defaults. */
  fun hasConflictingMethodLevelConfig(method: Method): Boolean {
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
    if (config.qualifiers != Config.DEFAULT_QUALIFIERS) {
      overrides.add("qualifiers=${config.qualifiers}")
    }
    if (config.application != Config.DEFAULT_APPLICATION) {
      overrides.add("application=${config.application.simpleName}")
    }
    if (config.shadows.isNotEmpty()) overrides.add("shadows=${config.shadows.size}")
    if (config.instrumentedPackages.isNotEmpty()) {
      overrides.add("instrumentedPackages=${config.instrumentedPackages.size}")
    }
    return if (overrides.isEmpty()) "none" else overrides.joinToString(", ")
  }
}
