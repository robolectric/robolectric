package org.robolectric.runner.common

/**
 * Builder for creating [RobolectricIntegration] instances with sensible defaults.
 *
 * This builder provides a fluent API for configuring Robolectric integrations for different test
 * frameworks.
 *
 * ## Usage
 *
 * ```kotlin
 * val integration = RobolectricIntegrationBuilder()
 *   .sandboxSharing(SandboxSharingStrategy.PER_CLASS)
 *   .parameterResolver(MyCustomResolver)
 *   .enableDebugLogging()
 *   .enableMetrics()
 *   .build()
 * ```
 *
 * ## Default Configuration
 * - Sandbox sharing: [SandboxSharingStrategy.PER_CLASS]
 * - Parameter resolver: [DefaultRobolectricParameterResolver]
 * - Debug logging: Disabled (unless system property is set)
 * - Metrics: Disabled (unless system property is set)
 */
@ExperimentalRunnerApi
@Suppress("TooManyFunctions")
class RobolectricIntegrationBuilder {

  private var sandboxSharingStrategy: SandboxSharingStrategy = SandboxSharingStrategy.PER_CLASS
  private var parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver
  private var debugLogging: Boolean = false
  private var metricsEnabled: Boolean = false
  private var timingEnabled: Boolean = false
  private var dependencies: RobolectricDependencies? = null
  private var classLoadingBridge: FrameworkClassLoadingBridge = DefaultFrameworkClassLoadingBridge
  private var lifecycleAnnotations: LifecycleAnnotations = LifecycleAnnotations.JUNIT5
  private var testFilter: TestFilter = TestFilter.ACCEPT_ALL

  /**
   * Sets the set of lifecycle annotations (e.g. `@BeforeEach`/`@AfterEach`) the integration should
   * honor. Defaults to [LifecycleAnnotations.JUNIT5].
   *
   * Consumed by [DiscoveryHelpers] when the caller opts into it, and available via
   * [getLifecycleAnnotations] for framework adapters that need to drive their own lifecycle.
   */
  fun lifecycleAnnotations(annotations: LifecycleAnnotations): RobolectricIntegrationBuilder {
    this.lifecycleAnnotations = annotations
    return this
  }

  /**
   * Sets a [TestFilter] to apply during discovery. Note: `:runner:common` does not automatically
   * apply this filter during execution — framework adapters must pass it to
   * [DiscoveryHelpers.discoverTestMethods] / [DiscoveryHelpers.discoverAllVariants]. The built
   * integration exposes it via [getTestFilter] so adapters can retrieve it from a single place.
   */
  fun testFilter(filter: TestFilter): RobolectricIntegrationBuilder {
    this.testFilter = filter
    return this
  }

  /**
   * Sets the sandbox sharing strategy.
   *
   * @param strategy How sandboxes should be shared across tests
   * @return This builder for chaining
   */
  fun sandboxSharing(strategy: SandboxSharingStrategy): RobolectricIntegrationBuilder {
    this.sandboxSharingStrategy = strategy
    return this
  }

  /**
   * Sets the parameter resolver for test method parameter injection.
   *
   * @param resolver The resolver to use for parameter injection
   * @return This builder for chaining
   */
  fun parameterResolver(resolver: ParameterResolver): RobolectricIntegrationBuilder {
    this.parameterResolver = resolver
    return this
  }

  /**
   * Enables debug logging for this integration.
   *
   * This is equivalent to setting `-Drobolectric.runner.debug=true`.
   *
   * @return This builder for chaining
   */
  fun enableDebugLogging(): RobolectricIntegrationBuilder {
    this.debugLogging = true
    return this
  }

  /**
   * Enables metrics collection for this integration.
   *
   * This is equivalent to setting `-Drobolectric.runner.metrics=true`.
   *
   * @return This builder for chaining
   */
  fun enableMetrics(): RobolectricIntegrationBuilder {
    this.metricsEnabled = true
    return this
  }

  /**
   * Enables timing metrics for this integration.
   *
   * This requires metrics to be enabled as well. Equivalent to setting
   * `-Drobolectric.runner.metrics.timing=true`.
   *
   * @return This builder for chaining
   */
  fun enableTiming(): RobolectricIntegrationBuilder {
    this.timingEnabled = true
    return this
  }

  /**
   * Sets custom Robolectric dependencies.
   *
   * Use this if you need to customize the Robolectric injector configuration.
   *
   * @param dependencies Pre-configured dependencies
   * @return This builder for chaining
   */
  fun dependencies(dependencies: RobolectricDependencies): RobolectricIntegrationBuilder {
    this.dependencies = dependencies
    return this
  }

  /**
   * Sets a bridge for framework-specific classloading and transformed class byte lookup.
   *
   * Use this when your test framework executes tests from a custom classloader.
   */
  fun classLoadingBridge(bridge: FrameworkClassLoadingBridge): RobolectricIntegrationBuilder {
    this.classLoadingBridge = bridge
    return this
  }

  /**
   * Copies all values from [configuration] into this builder. Use this to adopt a pre-built
   * [RunnerConfiguration] (e.g. loaded from system properties or an external config file) and then
   * apply any additional builder methods.
   */
  fun fromConfig(configuration: RunnerConfiguration): RobolectricIntegrationBuilder {
    sandboxSharingStrategy = configuration.sandboxSharing
    parameterResolver = configuration.parameterResolver
    debugLogging = configuration.debugLogging
    metricsEnabled = configuration.metricsEnabled
    timingEnabled = configuration.timingEnabled
    lifecycleAnnotations = configuration.lifecycleAnnotations
    testFilter = configuration.testFilter
    configuration.classLoadingBridge?.let { classLoadingBridge = it }
    return this
  }

  /**
   * Builds the [RobolectricIntegration] with the configured settings.
   *
   * @return A new RobolectricIntegration instance
   */
  fun build(): RobolectricIntegration {
    // Apply observability settings
    if (debugLogging) {
      RunnerLogger.isDebugEnabled = true
    }
    if (metricsEnabled) {
      RunnerMetrics.enable()
    }
    if (timingEnabled) {
      RunnerMetrics.enableTiming()
    }

    val deps =
      dependencies
        ?: RobolectricDependencies.create(frameworkClassLoadingBridge = classLoadingBridge)

    return DefaultRobolectricIntegration(
      sandboxSharing = sandboxSharingStrategy,
      parameterResolver = parameterResolver,
      dependencies = deps,
    )
  }

  /** @return the [LifecycleAnnotations] this builder is currently configured with. */
  fun getLifecycleAnnotations(): LifecycleAnnotations = lifecycleAnnotations

  /** @return the [TestFilter] this builder is currently configured with. */
  fun getTestFilter(): TestFilter = testFilter

  companion object {
    /**
     * Creates a builder with default settings for JUnit Platform.
     *
     * Defaults:
     * - Sandbox sharing: PER_CLASS
     * - Uses DiscoveryHelpers for test detection
     */
    @JvmStatic
    fun forJUnitPlatform(): RobolectricIntegrationBuilder {
      return RobolectricIntegrationBuilder().sandboxSharing(SandboxSharingStrategy.PER_CLASS)
    }

    /**
     * Creates a builder with default settings for JUnit Jupiter.
     *
     * Defaults:
     * - Sandbox sharing: PER_CLASS
     * - Parameter injection enabled
     */
    @JvmStatic
    fun forJUnitJupiter(): RobolectricIntegrationBuilder {
      return RobolectricIntegrationBuilder().sandboxSharing(SandboxSharingStrategy.PER_CLASS)
    }

    /**
     * Creates a builder from system properties.
     *
     * Reads:
     * - `robolectric.runner.debug`
     * - `robolectric.runner.metrics`
     * - `robolectric.runner.metrics.timing`
     */
    @JvmStatic
    fun fromSystemProperties(): RobolectricIntegrationBuilder {
      val builder = RobolectricIntegrationBuilder()

      if (System.getProperty("robolectric.runner.debug")?.toBoolean() == true) {
        builder.enableDebugLogging()
      }
      if (System.getProperty("robolectric.runner.metrics")?.toBoolean() == true) {
        builder.enableMetrics()
      }
      if (System.getProperty("robolectric.runner.metrics.timing")?.toBoolean() == true) {
        builder.enableTiming()
      }

      return builder
    }
  }
}
