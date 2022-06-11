package org.robolectric.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.robolectric.AndroidMetadata;
import org.robolectric.pluginapi.perf.Metadata;
import org.robolectric.pluginapi.perf.Metric;
import org.robolectric.pluginapi.perf.PerfStatsReporter;

/** Simple implementation of PerfStatsReporter that writes stats to stdout. */
public class SimplePerfStatsReporter implements PerfStatsReporter {

  private final List<Data> perfStatsData = new ArrayList<>();

  @Override
  public synchronized void report(Metadata metadata, Collection<Metric> metrics) {
    perfStatsData.add(new Data(metadata, metrics));
  }

  public void register() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::finalReport));
  }

  @SuppressWarnings("AndroidJdkLibsChecker)")
  private synchronized void finalReport() {
    Map<MetricKey, MetricValue> mergedMetrics = new TreeMap<>();
    for (Data perfStatsData : perfStatsData) {
      AndroidMetadata metadata = perfStatsData.metadata.get(AndroidMetadata.class);
      Map<String, String> deviceBootProperties = metadata.getDeviceBootProperties();
      int sdkInt = Integer.parseInt(deviceBootProperties.get("ro.build.version.sdk"));
      String resourcesMode = metadata.getResourcesMode();

      for (Metric metric : perfStatsData.metrics) {
        MetricKey key = new MetricKey(metric.getName(), metric.isSuccess(), sdkInt, resourcesMode);
        MetricValue mergedMetric = mergedMetrics.get(key);
        if (mergedMetric == null) {
          mergedMetric = new MetricValue();
          mergedMetrics.put(key, mergedMetric);
        }
        mergedMetric.report(metric);
      }
    }

    System.out.println("Name\tSDK\tResources\tSuccess\tCount\tMin ms\tMax ms\tAvg ms\tTotal ms");
    for (Entry<MetricKey, MetricValue> entry : mergedMetrics.entrySet()) {
      MetricKey key = entry.getKey();
      MetricValue value = entry.getValue();

      System.out.println(
          MessageFormat
              .format("{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}",
                  key.name,
                  key.sdkLevel,
                  key.resourcesMode,
                  key.success,
                  value.count,
                  (int) (value.minNs / 1000000),
                  (int) (value.maxNs / 1000000),
                  (int) (value.elapsedNs / 1000000 / value.count),
                  (int) (value.elapsedNs / 1000000)));
    }
  }

  private static class Data {
    private final Metadata metadata;
    private final Collection<Metric> metrics;

    public Data(Metadata metadata, Collection<Metric> metrics) {
      this.metadata = metadata;
      this.metrics = metrics;
    }
  }

  private static class MetricKey implements Comparable<MetricKey> {
    private final String name;
    private final boolean success;
    private final int sdkLevel;
    private final String resourcesMode;

    public MetricKey(String name, boolean success, int sdkLevel, String resourcesMode) {
      this.name = name;
      this.success = success;
      this.sdkLevel = sdkLevel;
      this.resourcesMode = resourcesMode;
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
      if (name != null ? !name.equals(metricKey.name) : metricKey.name != null) {
        return false;
      }
      if (sdkLevel != metricKey.sdkLevel) {
        return false;
      }
      return resourcesMode != null
          ? resourcesMode.equals(metricKey.resourcesMode)
          : metricKey.resourcesMode == null;
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (success ? 1 : 0);
      result = 31 * result + sdkLevel;
      result = 31 * result + (resourcesMode != null ? resourcesMode.hashCode() : 0);
      return result;
    }

    @Override
    public int compareTo(MetricKey o) {
      int i = name.compareTo(o.name);
      if (i != 0) {
        return i;
      }

      i = resourcesMode.compareTo(o.resourcesMode);
      if (i != 0) {
        return i;
      }

      i = Integer.compare(sdkLevel, o.sdkLevel);
      if (i != 0) {
        return i;
      }

      return Boolean.compare(success, o.success);
    }
  }

  private static class MetricValue {
    private int count;
    private long minNs;
    private long maxNs;
    private long elapsedNs;

    public void report(Metric metric) {
      if (count == 0) {
        count = metric.getCount();
        minNs = metric.getMinNs();
        maxNs = metric.getMaxNs();
        elapsedNs = metric.getElapsedNs();
      } else {
        count += metric.getCount();
        minNs = Math.min(minNs, metric.getMinNs());
        maxNs = Math.max(maxNs, metric.getMaxNs());
        elapsedNs += metric.getElapsedNs();
      }
    }
  }
}
