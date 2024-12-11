package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.app.PendingIntent;
import android.app.StatsManager;
import com.google.auto.value.AutoValue;
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
  private static final Map<BroadcastSubscriberKey, PendingIntent> broadcastSubscriberMap =
      new HashMap<>();

  @Resetter
  public static void reset() {
    reportDataMap.clear();
    statsMetadata = new byte[] {};
    configDataMap.clear();
    broadcastSubscriberMap.clear();
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

  /**
   * Retrieves the broadcast subscriber map stored in the shadow as a result of {@link
   * StatsManager#setBroadcastSubscriber()}.
   *
   * @return A map where the keys are {@link BroadcastSubscriberKey} objects, which contain the
   *     configKey and subscriberId, and the values are {@link PendingIntent} objects.
   */
  public static Map<BroadcastSubscriberKey, PendingIntent> getBroadcastSubscriberMap() {
    return broadcastSubscriberMap;
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

  @Implementation
  protected void setBroadcastSubscriber(
      PendingIntent pendingIntent, long configKey, long subscriberId) {
    BroadcastSubscriberKey key = BroadcastSubscriberKey.create(configKey, subscriberId);
    if (pendingIntent != null) {
      broadcastSubscriberMap.put(key, pendingIntent);
    } else {
      broadcastSubscriberMap.remove(key);
    }
  }

  /** A key used to store the configKey and subscriberId in the broadcastSubscriberMap. */
  @AutoValue
  public abstract static class BroadcastSubscriberKey {
    public static BroadcastSubscriberKey create(long key, long id) {
      return new AutoValue_ShadowStatsManager_BroadcastSubscriberKey(key, id);
    }

    public abstract long getKey();

    public abstract long getId();
  }
}
