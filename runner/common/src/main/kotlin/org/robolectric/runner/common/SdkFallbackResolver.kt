package org.robolectric.runner.common

import java.lang.reflect.Method
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.Sdk

/**
 * Resolves a fallback Android SDK when [org.robolectric.pluginapi.SdkPicker] returns nothing —
 * typically because `-Drobolectric.enabledSdks` filtered every configured SDK out.
 *
 * ## Why this exists
 *
 * The `@Config(sdk = …)` annotation and the `robolectric.enabledSdks` system property can disagree.
 * When they do, the test suite would otherwise silently discover zero tests. This helper picks a
 * sensible fallback SDK and surfaces a stderr warning + [RunnerLogger] entry so the divergence is
 * obvious.
 *
 * ## Reflection
 *
 * `:runner:common` intentionally depends on `:robolectric` as `implementation`, not `api`, to keep
 * `org.robolectric.plugins.SdkCollection` out of its public surface. The fallback path therefore
 * reflects into `SdkCollection.getSdk(int)` / `getKnownSdks()`. If `SdkCollection` moves into
 * `:pluginapi` in the future, this helper can be simplified to a typed call.
 */
@ExperimentalRunnerApi
object SdkFallbackResolver {

  /**
   * Returns a fallback [Sdk] for the given test, or `null` if
   * [SdkCollection][org.robolectric.plugins.SdkCollection] cannot be resolved from the injector at
   * all (e.g. the class is genuinely missing).
   *
   * Callers that cannot proceed without an SDK should treat `null` as a fatal discovery error and
   * surface it with diagnostic detail.
   */
  @JvmStatic
  fun resolveFallbackSdk(
    deps: RobolectricDependencies,
    testClass: Class<*>,
    testMethod: Method,
    config: Config,
  ): Sdk? {
    val sdkCollection = resolveSdkCollection(deps, testClass, testMethod) ?: return null
    return createFallbackSdk(sdkCollection, testClass, testMethod, config)
  }

  /**
   * Low-level access to `SdkCollection`. Returns `null` (and logs) when the class cannot be loaded.
   * Exposed so callers that want the raw collection for some other reason don't have to
   * re-implement the reflection.
   */
  @JvmStatic
  fun resolveSdkCollection(
    deps: RobolectricDependencies,
    testClass: Class<*>,
    testMethod: Method,
  ): Any? {
    return try {
      deps.injector.getInstance(Class.forName("org.robolectric.plugins.SdkCollection"))
    } catch (e: ClassNotFoundException) {
      RunnerLogger.error(
        "SdkCollection class was not found while resolving fallback SDK for " +
          "${testClass.name}.${testMethod.name}",
        e,
      )
      null
    }
  }

  private fun createFallbackSdk(
    sdkCollection: Any,
    testClass: Class<*>,
    testMethod: Method,
    config: Config,
  ): Sdk {
    if (config.sdk.isNotEmpty()) {
      val getSdkMethod = sdkCollection.javaClass.getMethod("getSdk", Int::class.javaPrimitiveType)
      val fallbackSdk = getSdkMethod.invoke(sdkCollection, config.sdk[0]) as Sdk
      val enabledSdksProperty = System.getProperty("robolectric.enabledSdks", "")
      val reason = "enabledSdks=$enabledSdksProperty filtered out configured SDKs"
      RunnerLogger.logSdkFallback(
        testClass.simpleName,
        testMethod.name,
        fallbackSdk.apiLevel,
        reason,
      )
      System.err.println(
        "WARNING: System property 'robolectric.enabledSdks=$enabledSdksProperty' " +
          "filtered out all configured SDKs for ${testClass.simpleName}.${testMethod.name}. " +
          "Configured SDKs: ${config.sdk.joinToString(",")}. " +
          "Falling back to SDK ${fallbackSdk.apiLevel}. " +
          "To avoid this warning, update @Config(sdk=[...]) to include one of the enabled SDKs."
      )
      return fallbackSdk
    }

    val getKnownSdksMethod = sdkCollection.javaClass.getMethod("getKnownSdks")
    @Suppress("UNCHECKED_CAST")
    val knownSdks = getKnownSdksMethod.invoke(sdkCollection) as java.util.SortedSet<Sdk>
    val fallbackSdk = knownSdks.last()
    RunnerLogger.logSdkFallback(
      testClass.simpleName,
      testMethod.name,
      fallbackSdk.apiLevel,
      "no @Config SDKs",
    )
    System.err.println(
      "WARNING: Using default SDK ${fallbackSdk.apiLevel} for " +
        "${testClass.simpleName}.${testMethod.name}"
    )
    return fallbackSdk
  }
}
