package org.robolectric.runner.common
/** Configuration for Robolectric runner behavior. */
@ExperimentalRunnerApi
data class RunnerConfiguration(
  val sandboxSharing: SandboxSharingStrategy = SandboxSharingStrategy.PER_CLASS,
  val debugLogging: Boolean = false,
  val metricsEnabled: Boolean = false,
  val timingEnabled: Boolean = false,
  val parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver,
  val lifecycleAnnotations: LifecycleAnnotations = LifecycleAnnotations.JUNIT5,
  val testFilter: TestFilter = TestFilter.ACCEPT_ALL,
) {
  init {
    if (debugLogging) RunnerLogger.isDebugEnabled = true
    if (metricsEnabled) RunnerMetrics.enable()
    if (timingEnabled) RunnerMetrics.enableTiming()
  }

  fun createIntegration(): RobolectricIntegration {
    return DefaultRobolectricIntegration(sandboxSharing, parameterResolver)
  }

  fun createIntegration(dependencies: RobolectricDependencies): RobolectricIntegration {
    return DefaultRobolectricIntegration(sandboxSharing, parameterResolver, dependencies)
  }

  fun validate() {
    check(!(timingEnabled && !metricsEnabled)) {
      "Timing metrics require metrics to be enabled. Set metricsEnabled=true."
    }
  }

  companion object {
    @JvmStatic
    fun fromSystemProperties(): RunnerConfiguration {
      return builder()
        .apply {
          if (System.getProperty("robolectric.runner.debug")?.toBoolean() == true)
            enableDebugLogging()
          if (System.getProperty("robolectric.runner.metrics")?.toBoolean() == true) enableMetrics()
          if (System.getProperty("robolectric.runner.metrics.timing")?.toBoolean() == true)
            enableTiming()
        }
        .build()
    }

    @JvmStatic fun default(): RunnerConfiguration = RunnerConfiguration()

    @JvmStatic fun builder(): Builder = Builder()
  }

  @Suppress("TooManyFunctions")
  class Builder {
    private var sandboxSharing: SandboxSharingStrategy = SandboxSharingStrategy.PER_CLASS
    private var debugLogging: Boolean = false
    private var metricsEnabled: Boolean = false
    private var timingEnabled: Boolean = false
    private var parameterResolver: ParameterResolver = DefaultRobolectricParameterResolver
    private var lifecycleAnnotations: LifecycleAnnotations = LifecycleAnnotations.JUNIT5
    private var testFilter: TestFilter = TestFilter.ACCEPT_ALL

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
      )
  }
}
