package org.robolectric.runner.common

/**
 * Immutable configuration DTO for Robolectric runner behavior.
 *
 * `RunnerConfiguration` is the serializable/transferable representation of how a Robolectric
 * integration should behave. [RobolectricIntegrationBuilder] is the canonical constructor — this
 * DTO feeds into it via [RobolectricIntegrationBuilder.fromConfig]. Use whichever shape matches
 * your call site:
 * - **From a config file or system properties?** Build a `RunnerConfiguration` and pass it to the
 *   builder.
 * - **Writing imperative adapter code?** Use the builder directly.
 *
 * @property sandboxSharing Strategy controlling how sandboxes are reused across tests
 * @property debugLogging Whether to enable [RunnerLogger] debug output
 * @property metricsEnabled Whether to enable [RunnerMetrics] counters
 * @property timingEnabled Whether to enable [RunnerMetrics] timing (implies [metricsEnabled])
 * @property parameterResolver Resolver used for test method parameter injection
 * @property lifecycleAnnotations Which annotation family ([LifecycleAnnotations]) describes the
 *   per-test / per-class callbacks. Exposed for framework adapters; `:runner:common` itself does
 *   not read it during execution
 * @property testFilter Filter applied by [DiscoveryHelpers] when adapters opt in. Not auto-applied
 *   during sandbox execution; see [RobolectricIntegrationBuilder.testFilter]
 * @property classLoadingBridge Optional bridge for framework-transformed test classes. `null` (the
 *   default) means use [DefaultFrameworkClassLoadingBridge]
 */
@ExperimentalRunnerApi
data class RunnerConfiguration(
  val sandboxSharing: SandboxSharingStrategy = SandboxSharingStrategy.PER_CLASS,
  val debugLogging: Boolean = false,
  val metricsEnabled: Boolean = false,
  val timingEnabled: Boolean = false,
  val parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver,
  val lifecycleAnnotations: LifecycleAnnotations = LifecycleAnnotations.JUNIT5,
  val testFilter: TestFilter = TestFilter.ACCEPT_ALL,
  val classLoadingBridge: FrameworkClassLoadingBridge? = null,
) {
  /** Produces a fully-configured [RobolectricIntegration]. */
  fun createIntegration(): RobolectricIntegration =
    RobolectricIntegrationBuilder().fromConfig(this).build()

  /** Like [createIntegration] but uses the supplied [dependencies]. */
  fun createIntegration(dependencies: RobolectricDependencies): RobolectricIntegration =
    RobolectricIntegrationBuilder().fromConfig(this).dependencies(dependencies).build()

  /**
   * Applies this configuration to a [RobolectricIntegrationBuilder]. Equivalent to
   * `builder.fromConfig(this)`; exposed as a method on the DTO for readability at call sites that
   * start from the config.
   */
  fun applyTo(builder: RobolectricIntegrationBuilder): RobolectricIntegrationBuilder =
    builder.fromConfig(this)

  /** Ensures that timing metrics require metrics to be enabled. */
  fun validate() {
    check(!(timingEnabled && !metricsEnabled)) {
      "Timing metrics require metrics to be enabled. Set metricsEnabled=true."
    }
  }

  companion object {
    /** Reads observability flags from Robolectric system properties. */
    @JvmStatic
    fun fromSystemProperties(): RunnerConfiguration =
      builder()
        .apply {
          if (System.getProperty("robolectric.runner.debug")?.toBoolean() == true) {
            enableDebugLogging()
          }
          if (System.getProperty("robolectric.runner.metrics")?.toBoolean() == true) {
            enableMetrics()
          }
          if (System.getProperty("robolectric.runner.metrics.timing")?.toBoolean() == true) {
            enableTiming()
          }
        }
        .build()

    @JvmStatic fun default(): RunnerConfiguration = RunnerConfiguration()

    @JvmStatic fun builder(): Builder = Builder()
  }

  /**
   * Mutable builder for [RunnerConfiguration]. Prefer [RobolectricIntegrationBuilder] if your goal
   * is to build an integration directly; use this builder when you need to produce a
   * [RunnerConfiguration] value (e.g. to stash in a registry or pass across module boundaries).
   */
  @Suppress("TooManyFunctions")
  class Builder {
    private var sandboxSharing: SandboxSharingStrategy = SandboxSharingStrategy.PER_CLASS
    private var debugLogging: Boolean = false
    private var metricsEnabled: Boolean = false
    private var timingEnabled: Boolean = false
    private var parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver
    private var lifecycleAnnotations: LifecycleAnnotations = LifecycleAnnotations.JUNIT5
    private var testFilter: TestFilter = TestFilter.ACCEPT_ALL
    private var classLoadingBridge: FrameworkClassLoadingBridge? = null

    fun sandboxSharing(strategy: SandboxSharingStrategy) = apply { this.sandboxSharing = strategy }

    fun enableDebugLogging() = apply { this.debugLogging = true }

    fun disableDebugLogging() = apply { this.debugLogging = false }

    fun enableMetrics() = apply { this.metricsEnabled = true }

    fun disableMetrics() = apply { this.metricsEnabled = false }

    fun enableTiming() = apply {
      this.timingEnabled = true
      this.metricsEnabled = true
    }

    fun disableTiming() = apply { this.timingEnabled = false }

    fun parameterResolver(resolver: ParameterResolver) = apply { this.parameterResolver = resolver }

    fun lifecycleAnnotations(annotations: LifecycleAnnotations) = apply {
      this.lifecycleAnnotations = annotations
    }

    fun testFilter(filter: TestFilter) = apply { this.testFilter = filter }

    fun classLoadingBridge(bridge: FrameworkClassLoadingBridge) = apply {
      this.classLoadingBridge = bridge
    }

    fun forJUnit4() = apply { this.lifecycleAnnotations = LifecycleAnnotations.JUNIT4 }

    fun forJUnit5() = apply { this.lifecycleAnnotations = LifecycleAnnotations.JUNIT5 }

    fun build(): RunnerConfiguration =
      RunnerConfiguration(
        sandboxSharing,
        debugLogging,
        metricsEnabled,
        timingEnabled,
        parameterResolver,
        lifecycleAnnotations,
        testFilter,
        classLoadingBridge,
      )
  }
}
