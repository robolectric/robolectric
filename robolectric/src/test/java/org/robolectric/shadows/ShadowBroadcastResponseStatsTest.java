package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.usage.BroadcastResponseStats;
import android.os.Build.VERSION_CODES;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.TIRAMISU)
public class ShadowBroadcastResponseStatsTest {
  private static final String TEST_PACKAGE = "test.app";
  private static final int BUCKET_ID = 10;

  @Test
  public void incrementNotificationsCancelledCount() {
    int count = 42;
    BroadcastResponseStats stats = new BroadcastResponseStats(TEST_PACKAGE, BUCKET_ID);

    ShadowBroadcastResponseStats statsShadow = Shadow.extract(stats);
    statsShadow.incrementNotificationsCancelledCount(count);

    assertThat(stats.getNotificationsCancelledCount()).isEqualTo(count);
  }

  @Test
  public void incrementNotificationsPostedCount() {
    int count = 42;
    BroadcastResponseStats stats = new BroadcastResponseStats(TEST_PACKAGE, BUCKET_ID);

    ShadowBroadcastResponseStats statsShadow = Shadow.extract(stats);
    statsShadow.incrementNotificationsPostedCount(count);

    assertThat(stats.getNotificationsPostedCount()).isEqualTo(count);
  }

  @Test
  public void incrementNotificationsUpdatedCount() {
    int count = 42;
    BroadcastResponseStats stats = new BroadcastResponseStats(TEST_PACKAGE, BUCKET_ID);

    ShadowBroadcastResponseStats statsShadow = Shadow.extract(stats);
    statsShadow.incrementNotificationsUpdatedCount(count);

    assertThat(stats.getNotificationsUpdatedCount()).isEqualTo(count);
  }

  @Test
  public void incrementBroadcastsDispatchedCount() {
    int count = 42;
    BroadcastResponseStats stats = new BroadcastResponseStats(TEST_PACKAGE, BUCKET_ID);

    ShadowBroadcastResponseStats statsShadow = Shadow.extract(stats);
    statsShadow.incrementBroadcastsDispatchedCount(count);

    assertThat(stats.getBroadcastsDispatchedCount()).isEqualTo(count);
  }
}
