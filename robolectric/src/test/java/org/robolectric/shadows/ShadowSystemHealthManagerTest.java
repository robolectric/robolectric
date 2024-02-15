package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.Process;
import android.os.health.HealthStats;
import android.os.health.SystemHealthManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = N)
public final class ShadowSystemHealthManagerTest {

  private static final int MY_UID = Process.myUid();
  private static final int OTHER_UID_1 = MY_UID + 1;
  private static final int OTHER_UID_2 = MY_UID + 2;

  private static final HealthStats MY_UID_HEALTH_STATS =
      HealthStatsBuilder.newBuilder().setDataType("my_uid_stats").build();
  private static final HealthStats OTHER_UID_1_HEALTH_STATS =
      HealthStatsBuilder.newBuilder().setDataType("other_uid_1_stats").build();
  private static final HealthStats OTHER_UID_2_HEALTH_STATS =
      HealthStatsBuilder.newBuilder().setDataType("other_uid_2_stats").build();

  private SystemHealthManager systemHealthManager;
  private ShadowSystemHealthManager shadowSystemHealthManager;

  @Before
  public void setUp() {
    systemHealthManager =
        (SystemHealthManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.SYSTEM_HEALTH_SERVICE);
    shadowSystemHealthManager = Shadow.extract(systemHealthManager);

    shadowSystemHealthManager.addHealthStats(MY_UID_HEALTH_STATS);
    shadowSystemHealthManager.addHealthStatsForUid(OTHER_UID_1, OTHER_UID_1_HEALTH_STATS);
    shadowSystemHealthManager.addHealthStatsForUid(OTHER_UID_2, OTHER_UID_2_HEALTH_STATS);
  }

  @Test
  public void snapshotForMyUid_expectedResult() throws Exception {
    HealthStats stats = systemHealthManager.takeMyUidSnapshot();

    assertThat(stats).isEqualTo(MY_UID_HEALTH_STATS);
  }

  @Test
  public void snapshotForOtherUids_expectedResult() throws Exception {
    HealthStats stats1 = systemHealthManager.takeUidSnapshot(OTHER_UID_1);
    HealthStats stats2 = systemHealthManager.takeUidSnapshot(OTHER_UID_2);

    assertThat(stats1).isEqualTo(OTHER_UID_1_HEALTH_STATS);
    assertThat(stats2).isEqualTo(OTHER_UID_2_HEALTH_STATS);
  }

  @Test
  public void snapshotForAllUids_expectedResult() throws Exception {
    int[] uids = {OTHER_UID_1, MY_UID, OTHER_UID_2};

    HealthStats[] stats = systemHealthManager.takeUidSnapshots(uids);

    assertThat(stats[0]).isEqualTo(OTHER_UID_1_HEALTH_STATS);
    assertThat(stats[1]).isEqualTo(MY_UID_HEALTH_STATS);
    assertThat(stats[2]).isEqualTo(OTHER_UID_2_HEALTH_STATS);
  }
}
