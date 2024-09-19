package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.app.StatsManager;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link StatsManager}. */
@Implements(value = StatsManager.class, isInAndroidSdk = false, minSdk = P)
public class ShadowStatsManager {

  private static final Map<Long, byte[]> reportDataMap = new HashMap<>();

  @SuppressWarnings("NonFinalStaticField")
  private static byte[] statsMetadata = new byte[] {};

  private static final Map<Long, byte[]> configDataMap = new HashMap<>();

  @Resetter
  public static void reset() {
    reportDataMap.clear();
    statsMetadata = new byte[] {};
    configDataMap.clear();
  }

  /** Adds metrics data that the shadow should return from {@link StatsManager#getReports()}. */
  public static void addReportData(long configKey, byte[] data) {
    reportDataMap.put(configKey, data);
  }

  /**
   * Sets the statsd metadata that the shadow should return from {@link
   * StatsManager#getStatsMetadata()}.
   */
  public static void setStatsMetadata(byte[] metadata) {
    statsMetadata = metadata;
  }

  /**
   * Retrieves the statsd configurations stored in the shadow as a result of {@link
   * StatsManager#addConfig()} and {@link StatsManager#removeConfig()}.
   */
  public static byte[] getConfigData(long configKey) {
    return configDataMap.getOrDefault(configKey, new byte[] {});
  }

  @Implementation
  protected byte[] getReports(long configKey) {
    byte[] data = reportDataMap.getOrDefault(configKey, new byte[] {});
    reportDataMap.remove(configKey);
    return data;
  }

  @Implementation
  protected byte[] getStatsMetadata() {
    return statsMetadata;
  }

  @Implementation
  protected void addConfig(long configKey, byte[] config) {
    configDataMap.put(configKey, config);
  }

  @Implementation
  protected void removeConfig(long configKey) {
    configDataMap.remove(configKey);
  }
}
