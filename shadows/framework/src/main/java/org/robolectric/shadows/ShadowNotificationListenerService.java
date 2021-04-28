package org.robolectric.shadows;

import static java.util.stream.Collectors.toCollection;

import android.app.Notification;
import android.content.ComponentName;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerService.Ranking;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow implementation of {@link NotificationListenerService}. */
@Implements(value = NotificationListenerService.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowNotificationListenerService extends ShadowService {
  private static final AtomicInteger rebindRequestCount = new AtomicInteger(0);

  private final List<StatusBarNotification> activeNotifications =
      Collections.synchronizedList(new ArrayList<>());
  private final AtomicInteger interruptionFilter =
      new AtomicInteger(NotificationListenerService.INTERRUPTION_FILTER_UNKNOWN);
  private final AtomicInteger hint = new AtomicInteger(0);
  private final AtomicInteger unbindRequestCount = new AtomicInteger(0);
  private final RankingMap emptyRankingMap = createEmptyRankingMap();

  /**
   * Adds the given {@link Notification} to the list of active Notifications. A corresponding {@link
   * StatusBarNotification} will be generated from this Notification, which will be included in the
   * result of {@link NotificationListenerService#getActiveNotifications}.
   *
   * @return the key of the generated {@link StatusBarNotification}
   */
  public String addActiveNotification(String packageName, int id, Notification notification) {
    StatusBarNotification statusBarNotification =
        new StatusBarNotification(
            /* pkg= */ packageName,
            /* opPkg= */ packageName,
            id,
            /* tag= */ null,
            /* uid= */ 0,
            /* initialPid= */ 0,
            /* score= */ 0,
            notification,
            UserHandle.CURRENT,
            notification.when);
    return addActiveNotification(statusBarNotification);
  }

  /**
   * Adds the given {@link StatusBarNotification} to the list of active Notifications. The given
   * {@link StatusBarNotification} will be included in the result of {@link
   * NotificationListenerService#getActiveNotifications}.
   *
   * @return the key of the given {@link StatusBarNotification}
   */
  public String addActiveNotification(StatusBarNotification statusBarNotification) {
    activeNotifications.add(statusBarNotification);
    return statusBarNotification.getKey();
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected static void requestRebind(ComponentName componentName) {
    rebindRequestCount.incrementAndGet();
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void requestUnbind() {
    unbindRequestCount.incrementAndGet();
  }

  @Implementation
  protected final void cancelAllNotifications() {
    activeNotifications.clear();
  }

  @Implementation
  protected final void cancelNotification(String key) {
    synchronized (activeNotifications) {
      Iterator<StatusBarNotification> iterator = activeNotifications.iterator();
      while (iterator.hasNext()) {
        StatusBarNotification statusBarNotification = iterator.next();
        if (statusBarNotification.getKey().equals(key)) {
          iterator.remove();
          break;
        }
      }
    }
  }

  /**
   * Returns zero or more notifications, added by {@link #addActiveNotification}, that match one of
   * the provided keys.
   *
   * @param keys the keys to match
   * @param trim ignored, trimming is not supported
   */
  @Implementation
  protected StatusBarNotification[] getActiveNotifications(String[] keys, int trim) {
    if (keys == null) {
      return activeNotifications.toArray(new StatusBarNotification[0]);
    }

    ImmutableSet<String> keySet = ImmutableSet.copyOf(keys);
    return activeNotifications.stream()
        .filter(notification -> keySet.contains(notification.getKey()))
        .collect(toCollection(ArrayList::new))
        .toArray(new StatusBarNotification[0]);
  }

  @Implementation
  protected void requestInterruptionFilter(int interruptionFilter) {
    this.interruptionFilter.set(interruptionFilter);
  }

  @Implementation
  protected int getCurrentInterruptionFilter() {
    return interruptionFilter.get();
  }

  @Implementation
  protected void requestListenerHints(int hint) {
    this.hint.set(hint);
  }

  @Implementation
  protected int getCurrentListenerHints() {
    return hint.get();
  }

  @Implementation
  protected RankingMap getCurrentRanking() {
    return emptyRankingMap;
  }

  /** Returns the number of times rebind was requested. */
  public static int getRebindRequestCount() {
    return rebindRequestCount.get();
  }

  /** Returns the number of times unbind was requested. */
  public int getUnbindRequestCount() {
    return unbindRequestCount.get();
  }

  /** Resets this shadow instance. */
  @Resetter
  public static void reset() {
    rebindRequestCount.set(0);
  }

  private static RankingMap createEmptyRankingMap() {
    return VERSION.SDK_INT < VERSION_CODES.Q
        ? ReflectionHelpers.callConstructor(RankingMap.class)
        : ReflectionHelpers.callConstructor(
            RankingMap.class, ClassParameter.from(Ranking[].class, new Ranking[] {}));
  }
}
