package org.robolectric.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.robolectric.pluginapi.perf.Metadata;
import org.robolectric.pluginapi.perf.Metric;
import org.robolectric.pluginapi.perf.PerfStatsReporter;

/**
 * Collects performance statistics for later reporting via {@link PerfStatsReporter}.
 *
 * @since 3.6
 */
public class PerfStatsCollector {

  private static final PerfStatsCollector INSTANCE = new PerfStatsCollector();

  private final Clock clock;
  private final AtomicReference<Metadata> metadata = new AtomicReference<>(null);
  private final Map<MetricKey, Metric> metricMap = new ConcurrentHashMap<>();
  private final AtomicBoolean enabled = new AtomicBoolean(true);

  public PerfStatsCollector() {
    this(System::nanoTime);
  }

  PerfStatsCollector(Clock clock) {
    this.clock = clock;
  }

  public static PerfStatsCollector getInstance() {
    return INSTANCE;
  }

  /** If not enabled, don't bother retaining perf stats, saving some memory and CPU cycles. */
  public void setEnabled(boolean isEnabled) {
    this.enabled.set(isEnabled);
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

  public void incrementCount(String eventName) {
    if (!enabled.get()) {
      return;
    }

    MetricKey key = new MetricKey(eventName, true);
    Metric metric = metricMap.get(key);
    if (metric == null) {
      metric = metricMap.computeIfAbsent(key, k -> new Metric(k.name, k.success));
    }
    metric.incrementCount();
  }

  /** Supplier that throws an exception. */
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

  /** Runnable that throws an exception. */
  // @FunctionalInterface -- not available on Android yet...
  public interface ThrowingRunnable<F extends Exception> {
    void run() throws F;
  }

  public Collection<Metric> getMetrics() {
    return new ArrayList<>(metricMap.values());
  }

  public void putMetadata(Metadata metadata) {
    if (!enabled.get()) {
      return;
    }

    this.metadata.set(metadata);
  }

  public Metadata getMetadata() {
    return metadata.get();
  }

  public void reset() {
    metadata.set(null);
    metricMap.clear();
  }

  /** Event for perf stats collection. */
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
      if (!enabled.get()) {
        return;
      }

      MetricKey key = new MetricKey(name, success);
      Metric metric = metricMap.get(key);
      if (metric == null) {
        metric = metricMap.computeIfAbsent(key, k -> new Metric(k.name, k.success));
      }
      metric.record(clock.nanoTime() - startTimeNs);
    }
  }

  /** Metric key for perf stats collection. */
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
      if (!(o instanceof MetricKey)) {
        return false;
      }

      MetricKey metricKey = (MetricKey) o;

      if (success != metricKey.success) {
        return false;
      }
      return Objects.equals(name, metricKey.name);
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (success ? 1 : 0);
      return result;
    }
  }
}
