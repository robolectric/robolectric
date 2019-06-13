package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService.Ranking;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowRanking}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.KITKAT_WATCH)
public class ShadowRankingTest {
  private Ranking ranking;

  @Before
  public void setUp() {
    ranking = new Ranking();
  }

  @Test
  public void setGetKey() {
    String key = "testKey";

    shadowOf(ranking).setKey(key);

    assertThat(shadowOf(ranking).getKey()).isEqualTo(key);
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void setGetChannel() {
    NotificationChannel channel =
        new NotificationChannel("id", "name", NotificationManager.IMPORTANCE_DEFAULT);

    shadowOf(ranking).setChannel(channel);

    assertThat(shadowOf(ranking).getChannel()).isEqualTo(channel);
  }
}
