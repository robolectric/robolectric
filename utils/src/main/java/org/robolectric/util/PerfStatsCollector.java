package org.robolectric.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PerfStatsCollector {

  private final String description;
  private final Clock clock;
  private final Map<String, Metric> metricMap = new HashMap<>();

  public PerfStatsCollector(String description) {
    this(description, System::currentTimeMillis);
  }

  PerfStatsCollector(String description, Clock clock) {
    this.description = description;
    this.clock = clock;
  }

  public synchronized Event startEvent(String eventName) {
    Metric metric = metricMap.computeIfAbsent(eventName, Metric::new);
    return new Event(metric);
  }

  public Collection<Metric> getMetrics() {
    return metricMap.values();
  }

  public String getDescription() {
    return description;
  }

  public class Event {
    private final Metric metric;
    private final long startTimeMs;

    Event(Metric metric) {
      this.metric = metric;
      this.startTimeMs = clock.currentTimeMillis();
    }

    public void finished() {
      this.metric.count++;
      this.metric.elapsedMs += (clock.currentTimeMillis() - startTimeMs);
    }
  }

  public static class Metric {
    private final String name;
    private volatile int count;
    private volatile int elapsedMs;

    Metric(String name, int count, int elapsedMs) {
      this.name = name;
      this.count = count;
      this.elapsedMs = elapsedMs;
    }

    Metric(String name) {
      this(name, 0, 0);
    }

    public String getName() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Metric metric = (Metric) o;

      if (count != metric.count) {
        return false;
      }

      if (elapsedMs != metric.elapsedMs) {
        return false;
      }
      return name != null ? name.equals(metric.name) : metric.name == null;
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + count;
      result = 31 * result + elapsedMs;
      return result;
    }

    @Override
    public String toString() {
      return "Metric{" +
          "name='" + name + '\'' +
          ", count=" + count +
          ", elapsedMs=" + elapsedMs +
          '}';
    }
  }
}
