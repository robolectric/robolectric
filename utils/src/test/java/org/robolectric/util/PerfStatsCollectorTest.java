package org.robolectric.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.util.PerfStatsCollector.Event;
import org.robolectric.util.PerfStatsCollector.Metric;

@RunWith(JUnit4.class)
public class PerfStatsCollectorTest {

  private FakeClock fakeClock;
  private PerfStatsCollector collector;

  @Before
  public void setUp() throws Exception {
    fakeClock = new FakeClock();
    collector = new PerfStatsCollector(fakeClock);
  }

  @Test
  public void shouldMeasureElapsedTimeForEvents() throws Exception {
    Event firstEvent = collector.startEvent("first event");
    fakeClock.delay(20);
    firstEvent.finished();

    Collection<Metric> metrics = collector.getMetrics();
    assertThat(metrics).containsExactly(
        new Metric("first event", 1, 20, true)
    );
  }

  @Test
  public void shouldMeasureElapsedTimeForRepeatedEvents() throws Exception {
    Event firstEvent = collector.startEvent("repeatable event");
    fakeClock.delay(20);
    firstEvent.finished();

    Event secondEvent = collector.startEvent("repeatable event");
    fakeClock.delay(20);
    secondEvent.finished();

    Event thirdEvent = collector.startEvent("repeatable event");
    fakeClock.delay(20);
    thirdEvent.finished();

    Collection<Metric> metrics = collector.getMetrics();
    assertThat(metrics).containsExactly(
        new Metric("repeatable event", 3, 60, true)
    );
  }

  @Test
  public void shouldRunAndMeasureSuccessfulCallable() throws Exception {
    assertThat(collector.measure("event", () -> {
      fakeClock.delay(10);
      return "return value";
    })).isEqualTo("return value");

    Collection<Metric> metrics = collector.getMetrics();
    assertThat(metrics).containsExactly(new Metric("event", 1, 10, true));
  }

  @Test
  public void shouldRunAndMeasureExceptionThrowingCallable() throws Exception {
    collector.measure("event", () -> {
      fakeClock.delay(10);
      return "return value";
    });

    try {
      collector.measure("event", () -> {
        fakeClock.delay(5);
        throw new RuntimeException("fake");
      });
      fail("should have thrown");
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("fake");
    }

    Collection<Metric> metrics = collector.getMetrics();
    assertThat(metrics).containsAllOf(
        new Metric("event", 1, 10, true),
        new Metric("event", 1, 5, false));
  }

  @Test
  public void shouldRunAndMeasureCheckedException() throws Exception {
    try {
      collector.measure("event", () -> {
        fakeClock.delay(5);
        throw new IOException("fake");
      });
      fail("should have thrown");
    } catch (IOException e) {
      assertThat(e.getMessage()).isEqualTo("fake");
    }

    Collection<Metric> metrics = collector.getMetrics();
    assertThat(metrics).contains(
        new Metric("event", 1, 5, false));
  }

  @Test
  public void reset_shouldClearAllMetadataAndMetrics() throws Exception {
    collector.putMetadata(String.class, "metadata");
    collector.startEvent("event").finished();
    collector.reset();
    assertThat(collector.getMetadata().get(String.class)).isNull();
    assertThat(collector.getMetrics()).isEmpty();
  }

  private static class FakeClock implements Clock {

    private int timeNs = 0;

    @Override
    public long nanoTime() {
      return timeNs;
    }

    public void delay(int ms) {
      timeNs += ms;
    }
  }
}
