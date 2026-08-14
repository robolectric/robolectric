package org.robolectric.runner.common

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.LongAdder

/**
 * Thread-safe metrics collection for Robolectric runner infrastructure.
 *
 * Tracks sandbox cache hit/miss rates and timing metrics for test execution phases.
 *
 * ## Usage
 *
 * Enable via system properties:
 * ```
 * -Drobolectric.runner.metrics=true
 * -Drobolectric.runner.metrics.timing=true
 * ```
 *
 * Or programmatically:
 * ```kotlin
 * RunnerMetrics.isEnabled = true
 * RunnerMetrics.isTimingEnabled = true
 * ```
 *
 * Get summary at end of test run:
 * ```kotlin
 * println(RunnerMetrics.getSummary())
 * ```
 */
@Suppress("TooManyFunctions")
@ExperimentalRunnerApi
object RunnerMetrics {

  /** System property to enable metrics collection. */
  const val METRICS_PROPERTY = "robolectric.runner.metrics"

  /** System property to enable timing metrics. */
  const val TIMING_PROPERTY = "robolectric.runner.metrics.timing"

  /** Whether metrics collection is enabled. */
  var isEnabled: Boolean = false

  /** Whether timing metrics are enabled. */
  var isTimingEnabled: Boolean = false

  // ========== Counters ==========

  private val sandboxCreations = LongAdder()
  private val sandboxTeardowns = LongAdder()
  private val sandboxCacheHits = LongAdder()
  private val sandboxCacheMisses = LongAdder()
  private val testExecutions = LongAdder()
  private val testFailures = LongAdder()

  // ========== Timing Data ==========

  /** Timing data for a phase. */
  data class TimingStats(
    val count: Long,
    val totalMs: Long,
    val minMs: Long,
    val maxMs: Long,
    val avgMs: Double,
    val p50Ms: Long,
    val p90Ms: Long,
    val p99Ms: Long,
  ) {
    override fun toString(): String {
      return "count=$count, total=${totalMs}ms, avg=${String.format(Locale.US, "%.2f", avgMs)}ms, " +
        "min=${minMs}ms, max=${maxMs}ms, p50=${p50Ms}ms, p90=${p90Ms}ms, p99=${p99Ms}ms"
    }
  }

  /** Thread-safe timing recorder. */
  private class TimingRecorder {
    private val values = java.util.concurrent.ConcurrentLinkedQueue<Long>()
    private val count = LongAdder()
    private val total = LongAdder()
    private val min = AtomicLong(Long.MAX_VALUE)
    private val max = AtomicLong(Long.MIN_VALUE)

    fun record(durationMs: Long) {
      values.add(durationMs)
      count.increment()
      total.add(durationMs)
      min.updateAndGet { current -> minOf(current, durationMs) }
      max.updateAndGet { current -> maxOf(current, durationMs) }
    }

    fun getStats(): TimingStats {
      val sortedValues = values.toList().sorted()
      val n = sortedValues.size
      if (n == 0) {
        return TimingStats(0, 0, 0, 0, 0.0, 0, 0, 0)
      }
      return TimingStats(
        count = count.sum(),
        totalMs = total.sum(),
        minMs = min.get().takeIf { it != Long.MAX_VALUE } ?: 0,
        maxMs = max.get().takeIf { it != Long.MIN_VALUE } ?: 0,
        avgMs = total.sum().toDouble() / n,
        p50Ms = sortedValues.getOrElse((n * PERCENTILE_50).toInt()) { 0 },
        p90Ms = sortedValues.getOrElse((n * PERCENTILE_90).toInt()) { 0 },
        p99Ms = sortedValues.getOrElse((n * PERCENTILE_99).toInt()) { 0 },
      )
    }
  }

  private val timings = ConcurrentHashMap<String, TimingRecorder>()

  // Percentile constants
  private const val PERCENTILE_50 = 0.50
  private const val PERCENTILE_90 = 0.90
  private const val PERCENTILE_99 = 0.99
  private const val PERCENTAGE_MULTIPLIER = 100.0
  private const val SUMMARY_LINE_WIDTH = 60

  // Predefined timing phases
  const val PHASE_SANDBOX_CREATION = "sandbox_creation"
  const val PHASE_ENVIRONMENT_SETUP = "environment_setup"
  const val PHASE_TEST_EXECUTION = "test_execution"
  const val PHASE_ENVIRONMENT_TEARDOWN = "environment_teardown"
  const val PHASE_CLASS_SETUP = "class_setup"
  const val PHASE_CLASS_TEARDOWN = "class_teardown"

  init {
    // Initialize from system properties
    isEnabled = System.getProperty(METRICS_PROPERTY, "false").toBoolean()
    isTimingEnabled = System.getProperty(TIMING_PROPERTY, "false").toBoolean()

    // Register shutdown hook to print summary
    if (isEnabled) {
      Runtime.getRuntime()
        .addShutdownHook(
          Thread {
            if (sandboxCreations.sum() > 0 || testExecutions.sum() > 0) {
              System.err.println("\n" + getSummary())
            }
          }
        )
    }
  }

  /** Enables metrics collection programmatically. */
  fun enable() {
    isEnabled = true
  }

  /** Enables timing metrics programmatically. */
  fun enableTiming() {
    isTimingEnabled = true
  }

  // ========== Counter Methods ==========

  /** Record a sandbox creation. */
  fun recordSandboxCreation() {
    if (isEnabled) sandboxCreations.increment()
  }

  /** Record a sandbox teardown. */
  fun recordSandboxTeardown() {
    if (isEnabled) sandboxTeardowns.increment()
  }

  /** Record a sandbox cache hit (reuse). */
  fun recordSandboxCacheHit() {
    if (isEnabled) sandboxCacheHits.increment()
  }

  /** Record a sandbox cache miss (new creation needed). */
  fun recordSandboxCacheMiss() {
    if (isEnabled) sandboxCacheMisses.increment()
  }

  /** Record a test execution. */
  fun recordTestExecution(success: Boolean) {
    if (isEnabled) {
      testExecutions.increment()
      if (!success) testFailures.increment()
    }
  }

  // ========== Timing Methods ==========

  /** Record timing for a phase. */
  fun recordTiming(phase: String, durationMs: Long) {
    if (isEnabled && isTimingEnabled) {
      timings.computeIfAbsent(phase) { TimingRecorder() }.record(durationMs)
    }
  }

  /** Measure and record timing for a block. */
  inline fun <T> timed(phase: String, block: () -> T): T {
    if (!isEnabled || !isTimingEnabled) {
      return block()
    }
    val start = System.currentTimeMillis()
    try {
      return block()
    } finally {
      recordTiming(phase, System.currentTimeMillis() - start)
    }
  }

  /** Get timing stats for a phase. */
  fun getTimingStats(phase: String): TimingStats? {
    return timings[phase]?.getStats()
  }

  /** Get all timing stats. */
  fun getAllTimingStats(): Map<String, TimingStats> {
    return timings.mapValues { it.value.getStats() }
  }

  // ========== Summary ==========

  /** Get a summary of all metrics. */
  fun getSummary(): String {
    val sb = StringBuilder()
    sb.appendLine("=".repeat(SUMMARY_LINE_WIDTH))
    sb.appendLine("Robolectric Runner Metrics Summary")
    sb.appendLine("=".repeat(SUMMARY_LINE_WIDTH))

    // Counter metrics
    sb.appendLine("\n--- Sandbox Metrics ---")
    sb.appendLine("  Sandbox creations:  ${sandboxCreations.sum()}")
    sb.appendLine("  Sandbox teardowns:  ${sandboxTeardowns.sum()}")
    sb.appendLine("  Cache hits:         ${sandboxCacheHits.sum()}")
    sb.appendLine("  Cache misses:       ${sandboxCacheMisses.sum()}")
    val totalCacheAccess = sandboxCacheHits.sum() + sandboxCacheMisses.sum()
    if (totalCacheAccess > 0) {
      val hitRate = (sandboxCacheHits.sum() * PERCENTAGE_MULTIPLIER) / totalCacheAccess
      sb.appendLine("  Cache hit rate:     ${String.format(Locale.US, "%.1f", hitRate)}%")
    }

    sb.appendLine("\n--- Test Metrics ---")
    sb.appendLine("  Test executions:    ${testExecutions.sum()}")
    sb.appendLine("  Test failures:      ${testFailures.sum()}")
    val totalTests = testExecutions.sum()
    if (totalTests > 0) {
      val successRate = ((totalTests - testFailures.sum()) * PERCENTAGE_MULTIPLIER) / totalTests
      sb.appendLine("  Success rate:       ${String.format(Locale.US, "%.1f", successRate)}%")
    }

    // Timing metrics
    if (isTimingEnabled && timings.isNotEmpty()) {
      sb.appendLine("\n--- Timing Metrics ---")
      val orderedPhases =
        listOf(
          PHASE_SANDBOX_CREATION,
          PHASE_ENVIRONMENT_SETUP,
          PHASE_CLASS_SETUP,
          PHASE_TEST_EXECUTION,
          PHASE_CLASS_TEARDOWN,
          PHASE_ENVIRONMENT_TEARDOWN,
        )
      for (phase in orderedPhases) {
        timings[phase]?.getStats()?.let { stats -> sb.appendLine("  $phase: $stats") }
      }
      // Any other phases
      for ((phase, recorder) in timings) {
        if (phase !in orderedPhases) {
          sb.appendLine("  $phase: ${recorder.getStats()}")
        }
      }
    }

    sb.appendLine("=".repeat(SUMMARY_LINE_WIDTH))
    return sb.toString()
  }

  /** Reset all metrics. Useful for testing. */
  fun reset() {
    isEnabled = false
    isTimingEnabled = false
    sandboxCreations.reset()
    sandboxTeardowns.reset()
    sandboxCacheHits.reset()
    sandboxCacheMisses.reset()
    testExecutions.reset()
    testFailures.reset()
    timings.clear()
  }
}
