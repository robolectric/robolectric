package org.robolectric.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.util.PerfStatsCollector.Event;
import org.robolectric.util.PerfStatsCollector.Metric;

public class PerfStatsCollectorTest {

  private FakeClock fakeClock;
  private PerfStatsCollector collector;

  @Before
  public void setUp() throws Exception {
    fakeClock = new FakeClock();
    collector = new PerfStatsCollector("description", fakeClock);
  }

  @Test
  public void shouldMeasureElapsedTimeForEvents() throws Exception {
    Event firstEvent = collector.startEvent("first event");
    fakeClock.delay(20);
    firstEvent.finished();

    Collection<Metric> metrics = collector.getMetrics();
    assertThat(metrics).containsExactly(
        new Metric("first event", 1, 20)
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
        new Metric("repeatable event", 3, 60)
    );
  }

  private class FakeClock implements Clock {

    private int timeMs = 0;

    @Override
    public long currentTimeMillis() {
      return timeMs;
    }

    public void delay(int ms) {
      timeMs += ms;
    }
  }
}
