package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification.Action;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService.Ranking;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowRanking}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowRankingTest {
  private Ranking ranking;

  @Before
  public void setUp() {
    ranking = new Ranking();
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setCanBubble() {
    shadowOf(ranking).setCanBubble(true);

    assertThat(ranking.canBubble()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void setCanShowBadge() {
    shadowOf(ranking).setCanShowBadge(true);

    assertThat(ranking.canShowBadge()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void setChannel() {
    NotificationChannel notificationChannel =
        new NotificationChannel("test_id", "test_name", NotificationManager.IMPORTANCE_DEFAULT);

    shadowOf(ranking).setChannel(notificationChannel);

    assertThat(ranking.getChannel()).isEqualTo(notificationChannel);
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void setImportance() {
    shadowOf(ranking).setImportance(42);

    assertThat(ranking.getImportance()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void setImportanceExplanation() {
    shadowOf(ranking).setImportanceExplanation("explanation");

    assertThat(ranking.getImportanceExplanation().toString()).isEqualTo("explanation");
  }

  @Test
  public void setKey() {
    shadowOf(ranking).setKey("key");

    assertThat(ranking.getKey()).isEqualTo("key");
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setLastAudiblyAlertedMillis() {
    shadowOf(ranking).setLastAudiblyAlertedMillis(42);

    assertThat(ranking.getLastAudiblyAlertedMillis()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void setOverrideGroupKey() {
    shadowOf(ranking).setOverrideGroupKey("key");

    assertThat(ranking.getOverrideGroupKey()).isEqualTo("key");
  }

  @Test
  public void setRank() {
    shadowOf(ranking).setRank(42);

    assertThat(ranking.getRank()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setSmartActions() {
    Action action = new Action(0, "", null);

    shadowOf(ranking).setSmartActions(ImmutableList.of(action));

    assertThat(ranking.getSmartActions()).containsExactly(action);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setSmartReplies() {
    shadowOf(ranking).setSmartReplies(ImmutableList.of("reply"));

    assertThat(ranking.getSmartReplies()).containsExactly("reply");
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void setSuppressedVisualEffects() {
    shadowOf(ranking).setSuppressedVisualEffects(42);

    assertThat(ranking.getSuppressedVisualEffects()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void setUserSentiment() {
    shadowOf(ranking).setUserSentiment(42);

    assertThat(ranking.getUserSentiment()).isEqualTo(42);
  }

  @Test
  public void setIsAmbient() {
    shadowOf(ranking).setIsAmbient(true);

    assertThat(ranking.isAmbient()).isEqualTo(true);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void setIsSuspended() {
    shadowOf(ranking).setIsSuspended(true);

    assertThat(ranking.isSuspended()).isEqualTo(true);
  }

  @Test
  public void setMatchesInterruptionFilter() {
    shadowOf(ranking).setMatchesInterruptionFilter(true);

    assertThat(ranking.matchesInterruptionFilter()).isEqualTo(true);
  }
}
