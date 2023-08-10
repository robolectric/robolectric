package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.app.StatsManager;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link ShadowStatsManager} */
@Implements(value = StatsManager.class, isInAndroidSdk = false, minSdk = P)
public class ShadowStatsManager {

  private static final Map<Long, byte[]> dataMap = new HashMap<>();
  private static byte[] statsMetadata = new byte[] {};

  @Resetter
  public static void reset() {
    dataMap.clear();
    statsMetadata = new byte[] {};
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
}
