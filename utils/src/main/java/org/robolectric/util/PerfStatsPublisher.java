package org.robolectric.util;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.pluginapi.perf.Metadata;
import org.robolectric.pluginapi.perf.Metric;
import org.robolectric.pluginapi.perf.PerfStatsReporter;

/** Helper class used by the test runner to manage PerfStatsReporters and publish data */
public class PerfStatsPublisher {

  private final ImmutableList<PerfStatsReporter> perfStatsReporters;
  private final AtomicBoolean shutdownRegistered = new AtomicBoolean(false);

  public PerfStatsPublisher(List<PerfStatsReporter> reporters) {
    perfStatsReporters = ImmutableList.copyOf(reporters);
  }

  /**
   * Register a JVM shutdown hook that calls PerfStatsReporter#finalReport on all reporters
   *
   * @throws IllegalStateException if this method has already been called
   */
  public void doFinalReportOnShutdown() {
    doFinalReportOnShutdown(() -> {});
  }

  /**
   * A variant of {@link #doFinalReportOnShutdown} that allows running a task before finalReport is
   * called
   *
   * @throws IllegalStateException if this method has already been called
   */
  public void doFinalReportOnShutdown(Runnable preFinalReportTask) {
    if (shutdownRegistered.getAndSet(true)) {
      throw new IllegalStateException("shutdown already registered");
    }
    if (perfStatsReporters.isEmpty()) {
      // ignore
      return;
    }
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  preFinalReportTask.run();
                  for (PerfStatsReporter perfStatsReporter : perfStatsReporters) {
                    try {
                      perfStatsReporter.finalReport();
                    } catch (RuntimeException e) {
                      e.printStackTrace();
                    }
                  }
                }));
  }

  /**
   * Report the currently stored metrics to the registered PerfStatsReporter.
   *
   * <p>Normally this is called by test runner at end of each test.
   */
  public void report(PerfStatsCollector collector) {
    if (!collector.isEnabled()) {
      return;
    }

    Metadata metadata = collector.getMetadata();
    Collection<Metric> metrics = collector.getMetrics();

    for (PerfStatsReporter perfStatsReporter : perfStatsReporters) {
      try {
        perfStatsReporter.report(metadata, metrics);
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }
  }
}
