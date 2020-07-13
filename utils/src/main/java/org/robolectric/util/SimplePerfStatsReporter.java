package org.robolectric.util;

import com.google.common.base.Preconditions;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.robolectric.AndroidMetadata;
import org.robolectric.pluginapi.perf.Metadata;
import org.robolectric.pluginapi.perf.Metric;
import org.robolectric.pluginapi.perf.PerfStatsReporter;

/**
 * Simple implementation of PerfStatsReporter that writes stats to a PrintStream.
 */
public class SimplePerfStatsReporter implements PerfStatsReporter {

  private final List<Data> perfStatsData = new ArrayList<>();
  private final PrintWriter printWriter;

  public SimplePerfStatsReporter() {
    this(System.out);
  }

  public SimplePerfStatsReporter(PrintStream out) {
    printWriter = new PrintWriter(out);
  }

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

    TableText table = new TableText(9);
    table.addRow("Name", "SDK", "Resources", "Success", "Count", "Min ms", "Max ms", "Avg ms",
        "Total ms");
    for (Entry<MetricKey, MetricValue> entry : mergedMetrics.entrySet()) {
      MetricKey key = entry.getKey();
      MetricValue value = entry.getValue();

      table.addRow(
          key.name,
          Integer.toString(key.sdkLevel),
          key.resourcesMode,
          Boolean.toString(key.success),
          Integer.toString(value.count),
          Long.toString(TimeUnit.NANOSECONDS.toMillis(value.minNs)),
          Long.toString(TimeUnit.NANOSECONDS.toMillis(value.maxNs)),
          Long.toString(TimeUnit.NANOSECONDS.toMillis(value.elapsedNs) / value.count),
          Long.toString(TimeUnit.NANOSECONDS.toMillis(value.elapsedNs)));
    }
    table.print(printWriter);
    printWriter.close();
  }

  /**
   * Utility class used to print a formatted ascii text table, with auto-sized column widths.
   */
  private static class TableText {

    private final int[] columnsWidths;
    private final List<String[]> tableData = new ArrayList<>();
    // number of spaces between columns
    private final static int COLUMN_SPACING = 1;

    TableText(int numColumns) {
      columnsWidths = new int[numColumns];
    }

    public void addRow(String... rowValues) {
      Preconditions.checkArgument(rowValues.length == columnsWidths.length);
      // adjust columnwidths
      for (int i = 0; i < rowValues.length; i++) {
        if ((rowValues[i].length() + COLUMN_SPACING) > columnsWidths[i]) {
          columnsWidths[i] = rowValues[i].length() + COLUMN_SPACING;
        }
      }
      tableData.add(rowValues);
    }

    public void print(PrintWriter writer) {
      StringBuilder formatStringBuilder = new StringBuilder();
      for (int i = 0; i < columnsWidths.length; i++) {
        formatStringBuilder.append("%");
        formatStringBuilder.append(columnsWidths[i]);
        formatStringBuilder.append("s");
      }
      formatStringBuilder.append("%n");
      String formatString = formatStringBuilder.toString();
      for (String[] rowData : tableData) {
        writer.printf(formatString, rowData);
      }
      writer.flush();
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
      if (o == null || getClass() != o.getClass()) {
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
