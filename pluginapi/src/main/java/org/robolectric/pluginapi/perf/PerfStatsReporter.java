package org.robolectric.pluginapi.perf;

import java.util.Collection;

public interface PerfStatsReporter {

  /**
   * Report performance stats.
   *
   * @param metadata metadata about this set of metrics.
   * @param metrics the metrics.
   */
  void report(Metadata metadata, Collection<Metric> metrics);

}
