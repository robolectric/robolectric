package org.robolectric.runner.common

import java.lang.reflect.Method
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.Sdk

/**
 * Selects the single SDK a test method should run on: the [org.robolectric.pluginapi.SdkPicker]'s
 * first choice, falling back to [SdkFallbackResolver] when `-Drobolectric.enabledSdks` filtered
 * everything out.
 *
 * Shared by `RobolectricJupiterEngine` and `RobolectricExtension` so both Jupiter paths pick SDKs
 * identically.
 */
@ExperimentalRunnerApi
object MethodSdkResolver {

  /** Returns the selected SDK, or null on hard failure (callers should throw with diagnostics). */
  fun selectMethodSdk(deps: RobolectricDependencies, testClass: Class<*>, method: Method): Sdk? {
    val configuration = deps.configurationStrategy.getConfig(testClass, method)
    val config = configuration.get(Config::class.java)
    val manifest = ManifestResolver.resolveManifest(config)
    deps.sdkPicker.selectSdks(configuration, manifest).firstOrNull()?.let {
      return it
    }
    return SdkFallbackResolver.resolveFallbackSdk(deps, testClass, method, config)
  }
}
