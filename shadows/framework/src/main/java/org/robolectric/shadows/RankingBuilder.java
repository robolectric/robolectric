package org.robolectric.shadows;

import android.app.Notification.Action;
import android.app.NotificationChannel;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService.Ranking;
import androidx.annotation.RequiresApi;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.util.ReflectionHelpers;

/** Builder for {@link Ranking}. */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class RankingBuilder {
  private final Ranking ranking = new Ranking();

  private RankingBuilder() {}

  /** Create a new {@link RankingBuilder}. */
  public static RankingBuilder newBuilder() {
    return new RankingBuilder();
  }

  /** Sets the value to be returned by {@link Ranking#canBubble()}. */
  @RequiresApi(VERSION_CODES.Q)
  public RankingBuilder setCanBubble(boolean canBubble) {
    ReflectionHelpers.setField(ranking, "mCanBubble", canBubble);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#canShowBadge()}. */
  @RequiresApi(VERSION_CODES.O)
  public RankingBuilder setCanShowBadge(boolean canShowBadge) {
    ReflectionHelpers.setField(ranking, "mShowBadge", canShowBadge);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getChannel()}. */
  @RequiresApi(VERSION_CODES.O)
  public RankingBuilder setChannel(NotificationChannel notificationChannel) {
    ReflectionHelpers.setField(ranking, "mChannel", notificationChannel);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getImportance()}. */
  @RequiresApi(VERSION_CODES.N)
  public RankingBuilder setImportance(int importance) {
    ReflectionHelpers.setField(ranking, "mImportance", importance);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getImportanceExplanation()}. */
  @RequiresApi(VERSION_CODES.N)
  public RankingBuilder setImportanceExplanation(CharSequence importanceExplanation) {
    ReflectionHelpers.setField(ranking, "mImportanceExplanation", importanceExplanation);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getKey()}. */
  public RankingBuilder setKey(String key) {
    ReflectionHelpers.setField(ranking, "mKey", key);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getLastAudiblyAlertedMillis()}. */
  @RequiresApi(VERSION_CODES.Q)
  public RankingBuilder setLastAudiblyAlertedMillis(long lastAudiblyAlertedMillis) {
    ReflectionHelpers.setField(ranking, "mLastAudiblyAlertedMs", lastAudiblyAlertedMillis);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getOverrideGroupKey()}. */
  @RequiresApi(VERSION_CODES.N)
  public RankingBuilder setOverrideGroupKey(String overrideGroupKey) {
    ReflectionHelpers.setField(ranking, "mOverrideGroupKey", overrideGroupKey);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getRank()}. */
  public RankingBuilder setRank(int rank) {
    ReflectionHelpers.setField(ranking, "mRank", rank);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getSmartActions()}. */
  @RequiresApi(VERSION_CODES.Q)
  public RankingBuilder setSmartActions(List<Action> smartActions) {
    ReflectionHelpers.setField(ranking, "mSmartActions", new ArrayList<>(smartActions));
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getSmartReplies()}. */
  @RequiresApi(VERSION_CODES.Q)
  public RankingBuilder setSmartReplies(List<CharSequence> smartReplies) {
    ReflectionHelpers.setField(ranking, "mSmartReplies", new ArrayList<>(smartReplies));
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getSuppressedVisualEffects()}. */
  @RequiresApi(VERSION_CODES.N)
  public RankingBuilder setSuppressedVisualEffects(int suppressedVisualEffects) {
    ReflectionHelpers.setField(ranking, "mSuppressedVisualEffects", suppressedVisualEffects);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#getUserSentiment()}. */
  @RequiresApi(VERSION_CODES.P)
  public RankingBuilder setUserSentiment(int userSentiment) {
    ReflectionHelpers.setField(ranking, "mUserSentiment", userSentiment);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#isAmbient()}. */
  public RankingBuilder setIsAmbient(boolean isAmbient) {
    ReflectionHelpers.setField(ranking, "mIsAmbient", isAmbient);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#isSuspended()}. */
  @RequiresApi(VERSION_CODES.P)
  public RankingBuilder setIsSuspended(boolean isSuspended) {
    ReflectionHelpers.setField(ranking, "mHidden", isSuspended);
    return this;
  }

  /** Sets the value to be returned by {@link Ranking#matchesInterruptionFilter()}. */
  public RankingBuilder setMatchesInterruptionFilter(boolean matchesInterruptionFilter) {
    ReflectionHelpers.setField(ranking, "mMatchesInterruptionFilter", matchesInterruptionFilter);
    return this;
  }

  public RankingBuilder copyFrom(Ranking ranking) {
    setKey(ranking.getKey());
    setRank(ranking.getRank());
    setIsAmbient(ranking.isAmbient());
    setMatchesInterruptionFilter(ranking.matchesInterruptionFilter());
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      setImportance(ranking.getImportance());
      setImportanceExplanation(ranking.getImportanceExplanation());
      setOverrideGroupKey(ranking.getOverrideGroupKey());
      setSuppressedVisualEffects(ranking.getSuppressedVisualEffects());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      setCanShowBadge(ranking.canShowBadge());
      setChannel(ranking.getChannel());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      setUserSentiment(ranking.getUserSentiment());
      setIsSuspended(ranking.isSuspended());
    }
    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      setCanBubble(ranking.canBubble());
      setLastAudiblyAlertedMillis(ranking.getLastAudiblyAlertedMillis());
      if (ranking.getSmartActions() != null) {
        setSmartActions(ranking.getSmartActions());
      }
      if (ranking.getSmartReplies() != null) {
        setSmartReplies(ranking.getSmartReplies());
      }
    }
    return this;
  }

  public Ranking build() {
    // Don't give out the private Ranking instance; return a copy instead.
    return newBuilder().copyFrom(ranking).ranking;
  }
}
