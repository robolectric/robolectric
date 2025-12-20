package org.robolectric.runner.common

import org.robolectric.android.AndroidSdkShadowMatcher
import org.robolectric.annotation.Config
import org.robolectric.config.AndroidConfigurer
import org.robolectric.interceptors.AndroidInterceptors
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.bytecode.ClassHandlerBuilder
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import org.robolectric.internal.bytecode.Interceptors
import org.robolectric.internal.bytecode.ShadowProviders
import org.robolectric.pluginapi.Sdk

/**
 * Configures Robolectric sandboxes with instrumentation, shadows, and class handlers.
 *
 * This class encapsulates the sandbox configuration logic, including:
 * - Creating instrumentation configurations with interceptors
 * - Building and applying shadow maps
 * - Configuring class handlers for bytecode instrumentation
 *
 * ## Usage
 *
 * ```kotlin
 * val deps = RobolectricDependencies.create()
 * val configurator = SandboxConfigurator(
 *   deps.androidConfigurer,
 *   deps.shadowProviders,
 *   deps.classHandlerBuilder
 * )
 *
 * val instConfig = configurator.createInstrumentationConfig(config)
 * val sandbox = sandboxManager.getAndroidSandbox(instConfig, sdk, ...)
 * configurator.configureSandbox(sandbox, config, sdk)
 * ```
 *
 * @property androidConfigurer Configures Android-specific instrumentation
 * @property shadowProviders Provides shadow class implementations
 * @property classHandlerBuilder Builds class handlers for instrumentation
 */
@ExperimentalRunnerApi
class SandboxConfigurator(
  private val androidConfigurer: AndroidConfigurer,
  private val shadowProviders: ShadowProviders,
  private val classHandlerBuilder: ClassHandlerBuilder,
) {
  /**
   * Creates an instrumentation configuration for the given Robolectric config.
   *
   * This method builds an [InstrumentationConfiguration] with Android interceptors and applies
   * configuration-specific settings.
   *
   * @param config The Robolectric configuration
   * @return The created InstrumentationConfiguration
   */
  fun createInstrumentationConfig(config: Config): InstrumentationConfiguration {
    val builder = InstrumentationConfiguration.newBuilder()
    val interceptors = Interceptors(AndroidInterceptors.all())
    androidConfigurer.configure(builder, interceptors)
    androidConfigurer.withConfig(builder, config)
    return builder.build()
  }

  /**
   * Configures a sandbox with shadows and class handlers.
   *
   * This method:
   * 1. Builds a shadow map from base shadows and config-specified shadows
   * 2. Applies the shadow map to the sandbox
   * 3. Creates a class handler with shadow matching for the target SDK
   * 4. Configures the sandbox with the class handler and interceptors
   *
   * @param sandbox The AndroidSandbox to configure
   * @param config The Robolectric configuration containing shadow specifications
   * @param sdk The target Android SDK for shadow matching
   */
  fun configureSandbox(sandbox: AndroidSandbox, config: Config, sdk: Sdk) {
    // Build shadow map with base shadows and config-specified shadows
    val shadowMapBuilder = shadowProviders.baseShadowMap.newBuilder()
    config.shadows.forEach { shadowClass -> shadowMapBuilder.addShadowClasses(shadowClass.java) }
    val shadowMap = shadowMapBuilder.build()

    // Replace the sandbox's shadow map
    sandbox.replaceShadowMap(shadowMap)

    // Create class handler with SDK-specific shadow matching
    val shadowMatcher = AndroidSdkShadowMatcher(sdk.apiLevel)
    val interceptors = Interceptors(AndroidInterceptors.all())
    val classHandler = classHandlerBuilder.build(shadowMap, shadowMatcher, interceptors)

    // Configure sandbox with class handler
    sandbox.configure(classHandler, interceptors)
  }
}
