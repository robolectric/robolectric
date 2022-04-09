package org.robolectric.util

import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.robolectric.pluginapi.perf.Metric

@RunWith(JUnit4::class)
class PerfStatsCollectorTest {
  private lateinit var fakeClock: FakeClock
  private lateinit var collector: PerfStatsCollector

  @Before
  @Throws(Exception::class)
  fun setUp() {
    fakeClock = FakeClock()
    collector = PerfStatsCollector(fakeClock)
  }

  @Test
  @Throws(Exception::class)
  fun shouldMeasureElapsedTimeForEvents() {
    val firstEvent = collector.startEvent("first event")
    fakeClock.delay(20)
    firstEvent.finished()
    val metrics = collector.metrics
    assertThat(metrics).containsExactly(Metric("first event", 1, 20, true))
  }

  @Test
  @Throws(Exception::class)
  fun shouldMeasureElapsedTimeForRepeatedEvents() {
    val firstEvent = collector.startEvent("repeatable event")
    fakeClock.delay(20)
    firstEvent.finished()
    val secondEvent = collector.startEvent("repeatable event")
    fakeClock.delay(20)
    secondEvent.finished()
    val thirdEvent = collector.startEvent("repeatable event")
    fakeClock.delay(20)
    thirdEvent.finished()
    val metrics = collector.metrics
    assertThat(metrics).containsExactly(Metric("repeatable event", 3, 60, true))
  }

  @Test
  @Throws(Exception::class)
  fun shouldRunAndMeasureSuccessfulCallable() {
    assertThat(
        collector.measure<String, RuntimeException>("event") {
          fakeClock.delay(10)
          "return value"
        }
      )
      .isEqualTo("return value")
    val metrics = collector.metrics
    assertThat(metrics).containsExactly(Metric("event", 1, 10, true))
  }

  @Test
  @Throws(Exception::class)
  fun shouldRunAndMeasureExceptionThrowingCallable() {
    collector.measure<String, RuntimeException>("event") {
      fakeClock.delay(10)
      "return value"
    }
    try {
      collector.measure<Any, RuntimeException>("event") {
        fakeClock.delay(5)
        throw RuntimeException("fake")
      }
      Assert.fail("should have thrown")
    } catch (e: RuntimeException) {
      assertThat(e.message).isEqualTo("fake")
    }
    val metrics = collector.metrics
    assertThat(metrics).containsAtLeast(Metric("event", 1, 10, true), Metric("event", 1, 5, false))
  }

  @Test
  @Throws(Exception::class)
  fun shouldRunAndMeasureCheckedException() {
    try {
      collector.measure<Any, IOException>("event") {
        fakeClock.delay(5)
        throw IOException("fake")
      }
      Assert.fail("should have thrown")
    } catch (e: IOException) {
      assertThat(e.message).isEqualTo("fake")
    }
    val metrics = collector.metrics
    assertThat(metrics).contains(Metric("event", 1, 5, false))
  }

  @Test
  @Throws(Exception::class)
  fun reset_shouldClearAllMetadataAndMetrics() {
    collector.putMetadata(String::class.java, "metadata")
    collector.startEvent("event").finished()
    collector.reset()
    assertThat(collector.metadata.get(String::class.java)).isNull()
    assertThat(collector.metrics).isEmpty()
  }

  private class FakeClock : Clock {
    private var timeNs = 0
    override fun nanoTime(): Long {
      return timeNs.toLong()
    }

    fun delay(ms: Int) {
      timeNs += ms
    }
  }
}
