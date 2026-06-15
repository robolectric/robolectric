package org.robolectric.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.pluginapi.perf.Metadata;
import org.robolectric.pluginapi.perf.Metric;
import org.robolectric.pluginapi.perf.PerfStatsReporter;

/** Helper class used by the test runner to manage PerfStatsReporters and publish data */
public class PerfStatsPublisher {

  private static final PerfStatsPublisher instance = new PerfStatsPublisher();

  private Set<PerfStatsReporter> perfStatsReporters = new CopyOnWriteArraySet<>();
  private final AtomicBoolean shutdownRegistered = new AtomicBoolean(false);

  private PerfStatsPublisher() {}

  public static PerfStatsPublisher getInstance() {
    return instance;
  }

  public void addReporters(List<PerfStatsReporter> reporters) {
    perfStatsReporters.addAll(reporters);
  }

  /**
   * Register a JVM shutdown hook that calls PerfStatsReporter#finalReport on all reporters
   */
  public void doFinalReportOnShutdown() {
    doFinalReportOnShutdown(() -> {});
  }

  /**
   * A variant of {@link #doFinalReportOnShutdown} that allows running a task before finalReport is
   * called
   */
  public void doFinalReportOnShutdown(Runnable preFinalReportTask) {
    if (shutdownRegistered.getAndSet(true)) {
      return;
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
