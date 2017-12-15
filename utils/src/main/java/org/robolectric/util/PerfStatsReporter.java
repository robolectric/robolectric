package org.robolectric.util;

import java.util.Collection;
import org.robolectric.util.PerfStatsCollector.Metadata;
import org.robolectric.util.PerfStatsCollector.Metric;

public interface PerfStatsReporter {

  /**
   * Report performance stats.
   *
   * @param metadata metadata about this set of metrics.
   * @param metrics the metrics.
   */
  void report(Metadata metadata, Collection<Metric> metrics);

}
