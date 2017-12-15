package org.robolectric.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects performance statistics for later reporting via {@link PerfStatsReporter}.
 *
 * @since 3.6
 */
public class PerfStatsCollector {

  private static final PerfStatsCollector INSTANCE = new PerfStatsCollector();

  private final Clock clock;
  private final Map<Class<?>, Object> metadata = new HashMap<>();
  private final Map<MetricKey, Metric> metricMap = new HashMap<>();
  private boolean enabled = true;

  public PerfStatsCollector() {
    this(System::nanoTime);
  }

  PerfStatsCollector(Clock clock) {
    this.clock = clock;
  }

  public static PerfStatsCollector getInstance() {
    return INSTANCE;
  }

  /**
   * If not enabled, don't bother retaining perf stats, saving some memory and CPU cycles.
   */
  public void setEnabled(boolean isEnabled) {
    this.enabled = isEnabled;
  }

  public Event startEvent(String eventName) {
    return new Event(eventName);
  }

  public <T, E extends Exception> T measure(String eventName, ThrowingSupplier<T, E> supplier)
      throws E {
    boolean success = true;
    Event event = startEvent(eventName);
    try {
      return supplier.get();
    } catch (Exception e) {
      success = false;
      throw e;
    } finally {
      event.finished(success);
    }
  }

  /**
   * Supplier that throws an exception.
   */
  // @FunctionalInterface -- not available on Android yet...
  public interface ThrowingSupplier<T, F extends Exception> {
    T get() throws F;
  }

  public <E extends Exception> void measure(String eventName, ThrowingRunnable<E> runnable)
      throws E {
    boolean success = true;
    Event event = startEvent(eventName);
    try {
      runnable.run();
    } catch (Exception e) {
      success = false;
      throw e;
    } finally {
      event.finished(success);
    }
  }

  /**
   * Runnable that throws an exception.
   */
  // @FunctionalInterface -- not available on Android yet...
  public interface ThrowingRunnable<F extends Exception> {
    void run() throws F;
  }

  public synchronized Collection<Metric> getMetrics() {
    return new ArrayList<>(metricMap.values());
  }

  public synchronized <T> void putMetadata(Class<T> metadataClass, T metadata) {
    if (!enabled) {
      return;
    }

    this.metadata.put(metadataClass, metadata);
  }

  public synchronized Metadata getMetadata() {
    return new Metadata(metadata);
  }

  public void reset() {
    metadata.clear();
    metricMap.clear();
  }

  /**
   * Event for perf stats collection.
   */
  public class Event {
    private final String name;
    private final long startTimeNs;

    Event(String name) {
      this.name = name;
      this.startTimeNs = clock.nanoTime();
    }

    public void finished() {
      finished(true);
    }

    public void finished(boolean success) {
      if (!enabled) {
        return;
      }

      synchronized (PerfStatsCollector.this) {
        MetricKey key = new MetricKey(name, success);
        Metric metric = metricMap.get(key);
        if (metric == null) {
          metricMap.put(key, metric = new Metric(key.name, key.success));
        }
        metric.count++;
        metric.elapsedNs += clock.nanoTime() - startTimeNs;
      }
    }
  }

  /**
   * Metric for perf stats collection.
   */
  public static class Metric {
    private final String name;
    private int count;
    private long elapsedNs;
    private final boolean success;

    public Metric(String name, int count, int elapsedNs, boolean success) {
      this.name = name;
      this.count = count;
      this.elapsedNs = elapsedNs;
      this.success = success;
    }

    public Metric(String name, boolean success) {
      this(name, 0, 0, success);
    }

    public String getName() {
      return name;
    }

    public int getCount() {
      return count;
    }

    public long getElapsedNs() {
      return elapsedNs;
    }

    public boolean isSuccess() {
      return success;
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
      if (elapsedNs != metric.elapsedNs) {
        return false;
      }
      if (success != metric.success) {
        return false;
      }
      return name != null ? name.equals(metric.name) : metric.name == null;
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + count;
      result = 31 * result + (int) (elapsedNs ^ (elapsedNs >>> 32));
      result = 31 * result + (success ? 1 : 0);
      return result;
    }

    @Override
    public String toString() {
      return "Metric{"
          + "name='" + name + '\''
          + ", count=" + count
          + ", elapsedNs=" + elapsedNs
          + ", success=" + success
          + '}';
    }
  }

  /**
   * Metric key for perf stats collection.
   */
  private static class MetricKey {
    private final String name;
    private final boolean success;

    MetricKey(String name, boolean success) {
      this.name = name;
      this.success = success;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      MetricKey metricKey = (MetricKey) o;

      if (success != metricKey.success) {
        return false;
      }
      return name != null ? name.equals(metricKey.name) : metricKey.name == null;
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (success ? 1 : 0);
      return result;
    }
  }

  /**
   * Metadata for perf stats collection.
   */
  public static class Metadata {
    private final Map<Class<?>, Object> metadata;

    Metadata(Map<Class<?>, Object> metadata) {
      this.metadata = new HashMap<>(metadata);
    }

    public <T> T get(Class<T> metadataClass) {
      return metadataClass.cast(metadata.get(metadataClass));
    }
  }
}
