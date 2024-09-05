package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.PendingIntent;
import android.app.StatsCursor;
import android.app.StatsManager;
import android.app.StatsManager.StatsQueryException;
import android.content.Context;
import android.content.Intent;
import android.os.IStatsManagerService;
import android.os.OutcomeReceiver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link ShadowStatsManager} */
@Implements(value = StatsManager.class, isInAndroidSdk = false, minSdk = P, looseSignatures = true)
public class ShadowStatsManager {

  private static final Map<Long, byte[]> dataMap = new HashMap<>();
  private static byte[] statsMetadata = new byte[] {};

  // StatsQueryException and StatsCursor only exists on U+
  private static Object /* StatsCursor */ cursor;
  private static Throwable /* StatsQueryException */ queryException;
  private static List<Long> metricIds;

  private PendingIntent pendingIntent;

  @Resetter
  public static void reset() {
    dataMap.clear();
    statsMetadata = new byte[] {};
    if (cursor != null) {
      ((StatsCursor) cursor).close();
      cursor = null;
    }
    queryException = null;
    metricIds = new ArrayList<>();
  }

  public static void addReportData(long configKey, byte[] data) {
    dataMap.put(configKey, data);
  }

  public static void setStatsMetadata(byte[] metadata) {
    statsMetadata = metadata;
  }

  @Implementation
  protected byte[] getReports(long configKey) {
    byte[] data = dataMap.getOrDefault(configKey, new byte[] {});
    dataMap.remove(configKey);
    return data;
  }

  @Implementation
  protected byte[] getStatsMetadata() {
    return statsMetadata;
  }

  /**
   * Sets the result to be returned from {@link StatsManager#query} .
   *
   * @param queryData the data to be returned in the query result.
   * @param columnNames the column names to be returned in the query result.
   * @param columnTypes the column types to be returned in the query result.
   * @param rowCount the number of rows to be returned in the query result.
   */
  public void setQueryResult(
      List<String> queryData, List<String> columnNames, List<Integer> columnTypes, int rowCount) {
    cursor =
        new StatsCursor(
            queryData.toArray(new String[0]),
            columnNames.toArray(new String[0]),
            columnTypes.stream().mapToInt(Integer::intValue).toArray(),
            rowCount);
    queryException = null;
  }

  /**
   * Sets the exception to be thrown when a query is made.
   *
   * @param exception the exception to be thrown when a query is made.
   */
  public void setQueryException(Throwable /*StatsQueryException*/ exception) {
    queryException = exception;
    cursor = null;
  }

  /**
   * Adds metrics to be notified via PendingIntent from {@link
   * StatsManager#setRestrictedMetricsChangedOperation}
   *
   * @param metrics the metricId to be added to existing list of metrics.
   */
  public void addMetricIds(List<Long> metrics, Context context)
      throws PendingIntent.CanceledException {
    metricIds.addAll(metrics);
    if (pendingIntent != null) {
      Intent fillIntent =
          new Intent().putExtra(StatsManager.EXTRA_STATS_RESTRICTED_METRIC_IDS, getMetricIds());
      pendingIntent.send(context, 0, fillIntent);
    }
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void query(
      Object /* long */ configKey,
      Object /* String */ configPackage,
      Object /* StatsQuery */ query,
      Object /* Executor */ executor,
      Object /* OutcomeReceiver<StatsCursor, StatsQueryException>*/ outcomeReceiverObj) {
    // even referencing StatsQueryException inside this method causes ClassNotFoundExceptions
    // when JVM is running with -Xverify:remote (the default setting)
    OutcomeReceiver<Object, Throwable> outcomeReceiver =
        (OutcomeReceiver<Object, Throwable>) outcomeReceiverObj;
    if (cursor != null) {
      outcomeReceiver.onResult(cursor);
    } else if (queryException != null) {
      outcomeReceiver.onError(queryException);
    } else {
      outcomeReceiver.onError(
          reflector(StatsQueryExceptionReflector.class)
              .newStatsQueryException("Stats result was not configured."));
    }
  }

  @ForType(StatsQueryException.class)
  interface StatsQueryExceptionReflector {
    @Constructor
    Throwable newStatsQueryException(String msg);
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  @SuppressWarnings("NonApiType")
  protected long[] setRestrictedMetricsChangedOperation(
      long configKey, String configPackage, PendingIntent pendingIntent) {
    this.pendingIntent = pendingIntent;
    return getMetricIds();
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected IStatsManagerService getIStatsManagerServiceLocked() {
    return ReflectionHelpers.createNullProxy(IStatsManagerService.class);
  }

  private long[] getMetricIds() {
    if (metricIds == null) {
      metricIds = new ArrayList<>();
    }
    return metricIds.stream().mapToLong(Long::longValue).toArray();
  }
}
