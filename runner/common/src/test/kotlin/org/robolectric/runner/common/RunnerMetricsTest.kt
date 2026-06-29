package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Tests for [RunnerMetrics]. */
@OptIn(ExperimentalRunnerApi::class)
class RunnerMetricsTest {

  @BeforeEach
  fun setUp() {
    RunnerMetrics.reset()
    RunnerMetrics.enable()
  }

  @AfterEach
  fun tearDown() {
    RunnerMetrics.reset()
  }

  @Test
  fun `sandbox creation counter increments`() {
    RunnerMetrics.recordSandboxCreation()
    RunnerMetrics.recordSandboxCreation()

    val summary = RunnerMetrics.getSummary()
    assertThat(summary).contains("Sandbox creations:  2")
  }

  @Test
  fun `sandbox teardown counter increments`() {
    RunnerMetrics.recordSandboxTeardown()

    val summary = RunnerMetrics.getSummary()
    assertThat(summary).contains("Sandbox teardowns:  1")
  }

  @Test
  fun `cache hit and miss counters work`() {
    RunnerMetrics.recordSandboxCacheHit()
    RunnerMetrics.recordSandboxCacheHit()
    RunnerMetrics.recordSandboxCacheMiss()

    val summary = RunnerMetrics.getSummary()
    assertThat(summary).contains("Cache hits:         2")
    assertThat(summary).contains("Cache misses:       1")
    assertThat(summary).contains("Cache hit rate:     66.7%")
  }

  @Test
  fun `test execution counter tracks success and failure`() {
    RunnerMetrics.recordTestExecution(success = true)
    RunnerMetrics.recordTestExecution(success = true)
    RunnerMetrics.recordTestExecution(success = false)

    val summary = RunnerMetrics.getSummary()
    assertThat(summary).contains("Test executions:    3")
    assertThat(summary).contains("Test failures:      1")
    assertThat(summary).contains("Success rate:       66.7%")
  }

  @Test
  fun `timing metrics record and calculate stats`() {
    RunnerMetrics.enableTiming()

    RunnerMetrics.recordTiming("test_phase", 100)
    RunnerMetrics.recordTiming("test_phase", 200)
    RunnerMetrics.recordTiming("test_phase", 300)

    val stats = RunnerMetrics.getTimingStats("test_phase")
    assertThat(stats).isNotNull()
    assertThat(stats!!.count).isEqualTo(3)
    assertThat(stats.totalMs).isEqualTo(600)
    assertThat(stats.minMs).isEqualTo(100)
    assertThat(stats.maxMs).isEqualTo(300)
    assertThat(stats.avgMs).isEqualTo(200.0)
  }

  @Test
  fun `timed block records duration`() {
    RunnerMetrics.enableTiming()

    val result =
      RunnerMetrics.timed("timed_block") {
        Thread.sleep(50)
        "result"
      }

    assertThat(result).isEqualTo("result")

    val stats = RunnerMetrics.getTimingStats("timed_block")
    assertThat(stats).isNotNull()
    assertThat(stats!!.count).isEqualTo(1)
    assertThat(stats.totalMs).isAtLeast(50)
  }

  @Test
  fun `metrics are not collected when disabled`() {
    // Disable metrics
    RunnerMetrics.isEnabled = false
    RunnerMetrics.reset() // Clear any previous data

    RunnerMetrics.recordSandboxCreation()
    RunnerMetrics.recordTestExecution(success = true)

    // Re-enable to get summary (but counters should still be 0)
    RunnerMetrics.isEnabled = true
    val summary = RunnerMetrics.getSummary()
    assertThat(summary).contains("Sandbox creations:  0")
    assertThat(summary).contains("Test executions:    0")
  }

  @Test
  fun `timing is not recorded when timing is disabled`() {
    // Ensure metrics enabled but timing disabled
    RunnerMetrics.reset()
    RunnerMetrics.isEnabled = true
    RunnerMetrics.isTimingEnabled = false

    RunnerMetrics.recordTiming("should_not_record", 100)

    val stats = RunnerMetrics.getTimingStats("should_not_record")
    assertThat(stats).isNull()
  }

  @Test
  fun `getAllTimingStats returns all phases`() {
    RunnerMetrics.enableTiming()

    RunnerMetrics.recordTiming("phase1", 100)
    RunnerMetrics.recordTiming("phase2", 200)

    val allStats = RunnerMetrics.getAllTimingStats()
    assertThat(allStats).containsKey("phase1")
    assertThat(allStats).containsKey("phase2")
  }

  @Test
  fun `reset clears all metrics`() {
    RunnerMetrics.enableTiming()
    RunnerMetrics.recordSandboxCreation()
    RunnerMetrics.recordTiming("test", 100)

    RunnerMetrics.reset()
    RunnerMetrics.enable()

    val summary = RunnerMetrics.getSummary()
    assertThat(summary).contains("Sandbox creations:  0")
    assertThat(RunnerMetrics.getAllTimingStats()).isEmpty()
  }

  @Test
  fun `summary includes timing section when timing is enabled`() {
    RunnerMetrics.enableTiming()
    RunnerMetrics.recordTiming(RunnerMetrics.PHASE_SANDBOX_CREATION, 100)

    val summary = RunnerMetrics.getSummary()
    assertThat(summary).contains("--- Timing Metrics ---")
    assertThat(summary).contains("sandbox_creation")
  }
}
