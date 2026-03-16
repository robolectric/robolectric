package org.robolectric.util

import com.google.common.truth.Truth.assertThat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PerfStatsCollectorPerformanceTest {
  private val collector = PerfStatsCollector()

  @Test
  fun testIncrementCountConcurrency() {
    val threadCount = 10
    val incrementsPerThread = 100000
    val executor = Executors.newFixedThreadPool(threadCount)
    val latch = CountDownLatch(threadCount)

    val start = System.nanoTime()
    for (i in 0 until threadCount) {
      executor.execute {
        try {
          for (j in 0 until incrementsPerThread) {
            collector.incrementCount("metric_$i")
          }
        } finally {
          latch.countDown()
        }
      }
    }

    latch.await(10, TimeUnit.SECONDS)
    val end = System.nanoTime()
    executor.shutdown()

    println("Took ${TimeUnit.NANOSECONDS.toMillis(end - start)} ms for ${threadCount * incrementsPerThread} increments across $threadCount threads (different metrics)")

    val metrics = collector.metrics
    assertThat(metrics).hasSize(threadCount)
    for (metric in metrics) {
      assertThat(metric.count).isEqualTo(incrementsPerThread)
    }
  }

  @Test
  fun testIncrementCountSameMetricConcurrency() {
    val threadCount = 10
    val incrementsPerThread = 100000
    val executor = Executors.newFixedThreadPool(threadCount)
    val latch = CountDownLatch(threadCount)

    val start = System.nanoTime()
    for (i in 0 until threadCount) {
      executor.execute {
        try {
          for (j in 0 until incrementsPerThread) {
            collector.incrementCount("same_metric")
          }
        } finally {
          latch.countDown()
        }
      }
    }

    latch.await(10, TimeUnit.SECONDS)
    val end = System.nanoTime()
    executor.shutdown()

    println("Took ${TimeUnit.NANOSECONDS.toMillis(end - start)} ms for ${threadCount * incrementsPerThread} increments across $threadCount threads (same metric)")

    val metrics = collector.metrics
    assertThat(metrics).hasSize(1)
    assertThat(metrics.first().count).isEqualTo(threadCount * incrementsPerThread)
  }
}
