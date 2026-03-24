package org.robolectric.pluginapi.perf;

import java.util.Collection;

public interface PerfStatsReporter {

  /**
   * Report performance stats.
   *
   * <p>The Robolectric test runner will call this method one or more times with a set of metrics.
   * Implementations should store the data in a way that can be accessed by {@link #finalReport()}.
   *
   * @param metadata metadata about this set of metrics.
   * @param metrics the metrics.
   */
  void report(Metadata metadata, Collection<Metric> metrics);

  /**
   * Called by Robolectric one time at the end of the test session, typically in a JVM shutdown
   * hook. Implementations can use this to report the metrics they have stored from previous calls
   * to {@link #report}.
   */
  default void finalReport() {}
  ;
}
