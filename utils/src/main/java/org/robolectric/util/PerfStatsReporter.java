package org.robolectric.util;

public interface PerfStatsReporter {

  /**
   * Report performance stats.
   *
   * @param perfStatsCollector the performance stats collector.
   */
  void report(PerfStatsCollector perfStatsCollector);

}
