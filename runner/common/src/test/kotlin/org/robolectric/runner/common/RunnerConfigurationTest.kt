package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class RunnerConfigurationTest {
  private var originalDebug: Boolean = false
  private var originalMetrics: Boolean = false

  @Before
  fun setUp() {
    originalDebug = RunnerLogger.isDebugEnabled
    originalMetrics = RunnerMetrics.isEnabled
  }

  @After
  fun tearDown() {
    RunnerLogger.isDebugEnabled = originalDebug
    RunnerMetrics.reset()
  }

  @Test
  fun `default configuration has expected values`() {
    val config = RunnerConfiguration.default()
    assertThat(config.sandboxSharing).isEqualTo(SandboxSharingStrategy.PER_CLASS)
    assertThat(config.debugLogging).isFalse()
    assertThat(config.metricsEnabled).isFalse()
    assertThat(config.timingEnabled).isFalse()
    assertThat(config.lifecycleAnnotations).isEqualTo(LifecycleAnnotations.JUNIT5)
    assertThat(config.testFilter).isEqualTo(TestFilter.ACCEPT_ALL)
  }

  @Test
  fun `builder creates configuration with custom values`() {
    val customFilter = TestFilter { _, _ -> true }
    val config =
      RunnerConfiguration.builder()
        .sandboxSharing(SandboxSharingStrategy.PER_TEST)
        .enableDebugLogging()
        .enableMetrics()
        .enableTiming()
        .lifecycleAnnotations(LifecycleAnnotations.JUNIT4)
        .testFilter(customFilter)
        .build()
    assertThat(config.sandboxSharing).isEqualTo(SandboxSharingStrategy.PER_TEST)
    assertThat(config.debugLogging).isTrue()
    assertThat(config.metricsEnabled).isTrue()
    assertThat(config.timingEnabled).isTrue()
    assertThat(config.lifecycleAnnotations).isEqualTo(LifecycleAnnotations.JUNIT4)
    assertThat(config.testFilter).isEqualTo(customFilter)
  }

  @Test
  fun `enableTiming also enables metrics`() {
    val config = RunnerConfiguration.builder().enableTiming().build()
    assertThat(config.metricsEnabled).isTrue()
    assertThat(config.timingEnabled).isTrue()
  }

  @Test
  fun `forJUnit4 sets JUNIT4 lifecycle annotations`() {
    val config = RunnerConfiguration.builder().forJUnit4().build()
    assertThat(config.lifecycleAnnotations).isEqualTo(LifecycleAnnotations.JUNIT4)
  }

  @Test
  fun `forJUnit5 sets JUNIT5 lifecycle annotations`() {
    val config = RunnerConfiguration.builder().forJUnit5().build()
    assertThat(config.lifecycleAnnotations).isEqualTo(LifecycleAnnotations.JUNIT5)
  }

  @Test
  fun `validate passes for valid configuration`() {
    val config = RunnerConfiguration.builder().enableMetrics().enableTiming().build()
    config.validate() // Should not throw
  }

  @Test
  fun `validate fails when timing is enabled without metrics`() {
    val config = RunnerConfiguration(timingEnabled = true, metricsEnabled = false)
    try {
      config.validate()
      assertThat(false).isTrue()
    } catch (e: IllegalStateException) {
      assertThat(e.message).contains("Timing metrics require metrics to be enabled")
    }
  }

  @Test
  fun `createIntegration returns DefaultRobolectricIntegration`() {
    val config = RunnerConfiguration.default()
    val integration = config.createIntegration()
    assertThat(integration).isInstanceOf(DefaultRobolectricIntegration::class.java)
  }
}
