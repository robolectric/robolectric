package org.robolectric.runner.common

import java.util.Properties
import org.robolectric.config.AndroidConfigurer
import org.robolectric.internal.SandboxManager
import org.robolectric.internal.bytecode.ClassHandlerBuilder
import org.robolectric.internal.bytecode.ShadowProviders
import org.robolectric.pluginapi.SdkPicker
import org.robolectric.pluginapi.config.ConfigurationStrategy
import org.robolectric.util.inject.Injector

/**
 * Manages Robolectric dependencies via dependency injection.
 *
 * This class encapsulates all the core Robolectric components needed for sandbox creation and test
 * execution. It provides a centralized way to access these dependencies across different test
 * framework integrations.
 *
 * ## Components
 * - **Injector**: Dependency injection container for Robolectric services
 * - **SandboxManager**: Manages Android sandbox instances with different SDK versions
 * - **SdkPicker**: Selects appropriate Android SDK for tests
 * - **ConfigurationStrategy**: Resolves test configuration from annotations
 * - **AndroidConfigurer**: Configures Android-specific instrumentation
 * - **ShadowProviders**: Provides shadow class implementations
 * - **ClassHandlerBuilder**: Builds class handlers for bytecode instrumentation
 *
 * ## Usage
 *
 * ```kotlin
 * val deps = RobolectricDependencies.create()
 * val sandbox = deps.sandboxManager.getAndroidSandbox(...)
 * ```
 *
 * @property injector The dependency injection container
 * @property sandboxManager Manages sandbox instances
 * @property sdkPicker Selects Android SDKs
 * @property configurationStrategy Resolves test configuration
 * @property androidConfigurer Configures Android instrumentation
 * @property shadowProviders Provides shadow implementations
 * @property classHandlerBuilder Builds class handlers
 */
@ExperimentalRunnerApi
data class RobolectricDependencies(
  val injector: Injector,
  val sandboxManager: SandboxManager,
  val sdkPicker: SdkPicker,
  val configurationStrategy: ConfigurationStrategy,
  val androidConfigurer: AndroidConfigurer,
  val shadowProviders: ShadowProviders,
  val classHandlerBuilder: ClassHandlerBuilder,
) {
  companion object {
    /**
     * Creates a new instance of RobolectricDependencies with default configuration.
     *
     * This method initializes the dependency injection container and obtains all required
     * Robolectric components.
     *
     * @param properties System properties to bind to the injector (defaults to
     *   System.getProperties())
     * @return A new RobolectricDependencies instance with all components initialized
     */
    @JvmStatic
    @JvmOverloads
    fun create(properties: Properties = System.getProperties()): RobolectricDependencies {
      val injector = Injector.Builder().bind(Properties::class.java, properties).build()
      return RobolectricDependencies(
        injector = injector,
        sandboxManager = injector.getInstance(SandboxManager::class.java),
        sdkPicker = injector.getInstance(SdkPicker::class.java),
        configurationStrategy = injector.getInstance(ConfigurationStrategy::class.java),
        androidConfigurer = injector.getInstance(AndroidConfigurer::class.java),
        shadowProviders = injector.getInstance(ShadowProviders::class.java),
        classHandlerBuilder = injector.getInstance(ClassHandlerBuilder::class.java),
      )
    }
  }
}
