package org.robolectric.shadows;

import android.app.Notification.Action;
import android.app.NotificationChannel;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService.Ranking;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link android.service.notification.NotificationListenerService.Ranking}. */
@Implements(value = Ranking.class, minSdk = VERSION_CODES.KITKAT_WATCH)
public class ShadowRanking {
  @RealObject private Ranking realObject;

  /** Overrides the return value for {@link Ranking#canBubble()}. */
  public void setCanBubble(boolean canBubble) {
    ReflectionHelpers.setField(realObject, "mCanBubble", canBubble);
  }

  /** Overrides the return value for {@link Ranking#canShowBadge()}. */
  public void setCanShowBadge(boolean canShowBadge) {
    ReflectionHelpers.setField(realObject, "mShowBadge", canShowBadge);
  }

  /** Overrides the return value for {@link Ranking#getChannel()}. */
  public void setChannel(NotificationChannel notificationChannel) {
    ReflectionHelpers.setField(realObject, "mChannel", notificationChannel);
  }

  /** Overrides the return value for {@link Ranking#getImportance()}. */
  public void setImportance(int importance) {
    ReflectionHelpers.setField(realObject, "mImportance", importance);
  }

  /** Overrides the return value for {@link Ranking#getImportanceExplanation()}. */
  public void setImportanceExplanation(String importanceExplanation) {
    ReflectionHelpers.setField(realObject, "mImportanceExplanation", importanceExplanation);
  }

  /** Overrides the return value for {@link Ranking#getKey()}. */
  public void setKey(String key) {
    ReflectionHelpers.setField(realObject, "mKey", key);
  }

  /** Overrides the return value for {@link Ranking#getLastAudiblyAlertedMillis()}. */
  public void setLastAudiblyAlertedMillis(long lastAudiblyAlertedMillis) {
    ReflectionHelpers.setField(realObject, "mLastAudiblyAlertedMs", lastAudiblyAlertedMillis);
  }

  /** Overrides the return value for {@link Ranking#getOverrideGroupKey()}. */
  public void setOverrideGroupKey(String overrideGroupKey) {
    ReflectionHelpers.setField(realObject, "mOverrideGroupKey", overrideGroupKey);
  }

  /** Overrides the return value for {@link Ranking#getRank()}. */
  public void setRank(int rank) {
    ReflectionHelpers.setField(realObject, "mRank", rank);
  }

  /** Overrides the return value for {@link Ranking#getSmartActions()}. */
  public void setSmartActions(List<Action> smartActions) {
    ReflectionHelpers.setField(realObject, "mSmartActions", new ArrayList<>(smartActions));
  }

  /** Overrides the return value for {@link Ranking#getSmartReplies()}. */
  public void setSmartReplies(List<CharSequence> smartReplies) {
    ReflectionHelpers.setField(realObject, "mSmartReplies", new ArrayList<>(smartReplies));
  }

  /** Overrides the return value for {@link Ranking#getSuppressedVisualEffects()}. */
  public void setSuppressedVisualEffects(int suppressedVisualEffects) {
    ReflectionHelpers.setField(realObject, "mSuppressedVisualEffects", suppressedVisualEffects);
  }

  /** Overrides the return value for {@link Ranking#getUserSentiment()}. */
  public void setUserSentiment(int userSentiment) {
    ReflectionHelpers.setField(realObject, "mUserSentiment", userSentiment);
  }

  /** Overrides the return value for {@link Ranking#isAmbient()}. */
  public void setIsAmbient(boolean isAmbient) {
    ReflectionHelpers.setField(realObject, "mIsAmbient", isAmbient);
  }

  /** Overrides the return value for {@link Ranking#isSuspended()}. */
  public void setIsSuspended(boolean isSuspended) {
    ReflectionHelpers.setField(realObject, "mHidden", isSuspended);
  }

  /** Overrides the return value for {@link Ranking#matchesInterruptionFilter()}. */
  public void setMatchesInterruptionFilter(boolean matchesInterruptionFilter) {
    ReflectionHelpers.setField(realObject, "mMatchesInterruptionFilter", matchesInterruptionFilter);
  }
}
