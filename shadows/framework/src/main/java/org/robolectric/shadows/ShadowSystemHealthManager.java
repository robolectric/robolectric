package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.os.Process;
import android.os.health.HealthStats;
import android.os.health.SystemHealthManager;
import java.util.HashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link android.os.health.SystemHealthManager} */
@Implements(value = SystemHealthManager.class, minSdk = N)
public class ShadowSystemHealthManager {

  private static final HealthStats DEFAULT_HEALTH_STATS =
      HealthStatsBuilder.newBuilder().setDataType("default_health_stats").build();

  private final HashMap<Integer, HealthStats> uidToHealthStats = new HashMap<>();

  @Implementation
  protected HealthStats takeMyUidSnapshot() {
    return takeUidSnapshot(Process.myUid());
  }

  @Implementation
  protected HealthStats takeUidSnapshot(int uid) {
    return uidToHealthStats.getOrDefault(uid, DEFAULT_HEALTH_STATS);
  }

  @Implementation
  protected HealthStats[] takeUidSnapshots(int[] uids) {
    HealthStats[] stats = new HealthStats[uids.length];
    for (int i = 0; i < uids.length; i++) {
      stats[i] = takeUidSnapshot(uids[i]);
    }
    return stats;
  }

  /**
   * Add {@link HealthStats} for the given UID. Calling {@link SystemHealthManager#takeUidSnapshot}
   * with the given UID will return this HealthStats object.
   */
  public void addHealthStatsForUid(int uid, HealthStats stats) {
    uidToHealthStats.put(uid, stats);
  }

  /** The same as {@code addHealthStatsForUid(android.os.Process.myUid(), stats)}. */
  public void addHealthStats(HealthStats stats) {
    addHealthStatsForUid(Process.myUid(), stats);
  }
}
