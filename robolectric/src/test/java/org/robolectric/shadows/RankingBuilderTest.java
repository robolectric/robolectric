package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Notification.Action;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService.Ranking;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Test for {@link RankingBuilder}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.LOLLIPOP)
public class RankingBuilderTest {

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setCanBubble() {
    Ranking ranking = RankingBuilder.newBuilder().setCanBubble(true).build();

    assertThat(ranking.canBubble()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void setCanShowBadge() {
    Ranking ranking = RankingBuilder.newBuilder().setCanShowBadge(true).build();

    assertThat(ranking.canShowBadge()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void setChannel() {
    NotificationChannel notificationChannel =
        new NotificationChannel("test_id", "test_name", NotificationManager.IMPORTANCE_DEFAULT);

    Ranking ranking = RankingBuilder.newBuilder().setChannel(notificationChannel).build();

    assertThat(ranking.getChannel()).isEqualTo(notificationChannel);
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void setImportance() {
    Ranking ranking = RankingBuilder.newBuilder().setImportance(42).build();

    assertThat(ranking.getImportance()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void setImportanceExplanation() {
    Ranking ranking = RankingBuilder.newBuilder().setImportanceExplanation("explanation").build();

    assertThat(ranking.getImportanceExplanation().toString()).isEqualTo("explanation");
  }

  @Test
  public void setKey() {
    Ranking ranking = RankingBuilder.newBuilder().setKey("key").build();

    assertThat(ranking.getKey()).isEqualTo("key");
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setLastAudiblyAlertedMillis() {
    Ranking ranking = RankingBuilder.newBuilder().setLastAudiblyAlertedMillis(42).build();

    assertThat(ranking.getLastAudiblyAlertedMillis()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void setOverrideGroupKey() {
    Ranking ranking = RankingBuilder.newBuilder().setOverrideGroupKey("key").build();

    assertThat(ranking.getOverrideGroupKey()).isEqualTo("key");
  }

  @Test
  public void setRank() {
    Ranking ranking = RankingBuilder.newBuilder().setRank(42).build();

    assertThat(ranking.getRank()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setSmartActions() {
    Action action = new Action(0, "", null);

    Ranking ranking = RankingBuilder.newBuilder().setSmartActions(ImmutableList.of(action)).build();

    assertThat(ranking.getSmartActions()).containsExactly(action);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setSmartReplies() {
    Ranking ranking =
        RankingBuilder.newBuilder().setSmartReplies(ImmutableList.of("reply")).build();

    assertThat(ranking.getSmartReplies()).containsExactly("reply");
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void setSuppressedVisualEffects() {
    Ranking ranking = RankingBuilder.newBuilder().setSuppressedVisualEffects(42).build();

    assertThat(ranking.getSuppressedVisualEffects()).isEqualTo(42);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void setUserSentiment() {
    Ranking ranking = RankingBuilder.newBuilder().setUserSentiment(42).build();

    assertThat(ranking.getUserSentiment()).isEqualTo(42);
  }

  @Test
  public void setIsAmbient() {
    Ranking ranking = RankingBuilder.newBuilder().setIsAmbient(true).build();

    assertThat(ranking.isAmbient()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void setIsSuspended() {
    Ranking ranking = RankingBuilder.newBuilder().setIsSuspended(true).build();

    assertThat(ranking.isSuspended()).isTrue();
  }

  @Test
  public void setMatchesInterruptionFilter() {
    Ranking ranking = RankingBuilder.newBuilder().setMatchesInterruptionFilter(true).build();

    assertThat(ranking.matchesInterruptionFilter()).isTrue();
  }

  @Test
  public void copyFrom() {
    RankingBuilder builder = RankingBuilder.newBuilder();
    builder.setKey("key");
    builder.setRank(42);
    builder.setIsAmbient(true);
    builder.setMatchesInterruptionFilter(false);
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      builder.setImportance(4);
      builder.setImportanceExplanation("explanation");
      builder.setOverrideGroupKey("override key");
      builder.setSuppressedVisualEffects(444);
    }
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      builder.setCanShowBadge(true);
      builder.setChannel(
          new NotificationChannel("test_id", "test_name", NotificationManager.IMPORTANCE_DEFAULT));
    }
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      builder.setUserSentiment(15);
      builder.setIsSuspended(true);
    }
    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      builder.setCanBubble(false);
      builder.setLastAudiblyAlertedMillis(1234567);
      builder.setSmartActions(ImmutableList.of(new Action(0, "", null)));
      builder.setSmartReplies(ImmutableList.of("reply"));
    }
    Ranking ranking = builder.build();

    Ranking rankingCopy = RankingBuilder.newBuilder().copyFrom(ranking).build();

    assertThat(rankingCopy).isNotSameInstanceAs(ranking);
    assertThat(rankingCopy.getKey()).isEqualTo(ranking.getKey());
    assertThat(rankingCopy.getRank()).isEqualTo(ranking.getRank());
    assertThat(rankingCopy.isAmbient()).isEqualTo(ranking.isAmbient());
    assertThat(rankingCopy.matchesInterruptionFilter())
        .isEqualTo(ranking.matchesInterruptionFilter());
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      assertThat(rankingCopy.getImportance()).isEqualTo(ranking.getImportance());
      assertThat(rankingCopy.getImportanceExplanation())
          .isEqualTo(ranking.getImportanceExplanation());
      assertThat(rankingCopy.getOverrideGroupKey()).isEqualTo(ranking.getOverrideGroupKey());
      assertThat(rankingCopy.getSuppressedVisualEffects())
          .isEqualTo(ranking.getSuppressedVisualEffects());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      assertThat(rankingCopy.canShowBadge()).isEqualTo(ranking.canShowBadge());
      assertThat(rankingCopy.getChannel()).isEqualTo(ranking.getChannel());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      assertThat(rankingCopy.getUserSentiment()).isEqualTo(ranking.getUserSentiment());
      assertThat(rankingCopy.isSuspended()).isEqualTo(ranking.isSuspended());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      assertThat(rankingCopy.canBubble()).isEqualTo(ranking.canBubble());
      assertThat(rankingCopy.getLastAudiblyAlertedMillis())
          .isEqualTo(ranking.getLastAudiblyAlertedMillis());
      assertThat(rankingCopy.getSmartActions()).isEqualTo(ranking.getSmartActions());
      assertThat(rankingCopy.getSmartReplies()).isEqualTo(ranking.getSmartReplies());
    }
  }

  @Test
  public void build_allowsBuilderToBeReused() {
    RankingBuilder builder = RankingBuilder.newBuilder().setKey("key1");

    Ranking ranking1 = builder.build();
    Ranking ranking2 = builder.setKey("key2").build();

    assertThat(ranking1).isNotSameInstanceAs(ranking2);
    assertThat(ranking1.getKey()).isEqualTo("key1");
    assertThat(ranking2.getKey()).isEqualTo("key2");
  }

  private void printRanking(Ranking ranking) {
    System.err.println("Ranking: " + ranking);
    System.err.println(ranking.getKey());
    System.err.println(ranking.getRank());
    System.err.println(ranking.isAmbient());
    System.err.println(ranking.matchesInterruptionFilter());
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      System.err.println(ranking.getImportance());
      System.err.println(ranking.getImportanceExplanation());
      System.err.println(ranking.getOverrideGroupKey());
      System.err.println(ranking.getSuppressedVisualEffects());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      System.err.println(ranking.canShowBadge());
      System.err.println(ranking.getChannel());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      System.err.println(ranking.getUserSentiment());
      System.err.println(ranking.isSuspended());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      System.err.println(ranking.canBubble());
      System.err.println(ranking.getLastAudiblyAlertedMillis());
      if (ranking.getSmartActions() != null) {
        System.err.println(ranking.getSmartActions());
      }
      if (ranking.getSmartReplies() != null) {
        System.err.println(ranking.getSmartReplies());
      }
    }
  }
}
