package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Service.class)
public class ShadowService extends ShadowContextWrapper {
  @RealObject Service realService;

  private int lastForegroundNotificationId;
  private Notification lastForegroundNotification;
  private boolean lastForegroundNotificationAttached = false;
  private boolean selfStopped = false;
  private boolean foregroundStopped;
  private boolean notificationShouldRemoved;
  private int stopSelfId;
  private int stopSelfResultId;

  @Implementation
  protected void onDestroy() {
    if (lastForegroundNotificationAttached) {
      lastForegroundNotificationAttached = false;
      removeForegroundNotification();
    }
  }

  @Implementation
  protected void stopSelf() {
    selfStopped = true;
  }

  @Implementation
  protected void stopSelf(int id) {
    selfStopped = true;
    stopSelfId = id;
  }

  @Implementation
  protected boolean stopSelfResult(int id) {
    selfStopped = true;
    stopSelfResultId = id;
    return true;
  }

  @Implementation
  protected final void startForeground(int id, Notification notification) {
    foregroundStopped = false;
    lastForegroundNotificationId = id;
    lastForegroundNotification = notification;
    lastForegroundNotificationAttached = true;
    notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
    NotificationManager nm =
        (NotificationManager)
            RuntimeEnvironment.getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(id, notification);
  }

  protected void stopForeground(boolean removeNotification) {
    foregroundStopped = true;
    notificationShouldRemoved = removeNotification;
    if (removeNotification) {
      removeForegroundNotification();
    }
  }

  @Implementation(minSdk = N)
  protected void stopForeground(int flags) {
    if ((flags & Service.STOP_FOREGROUND_DETACH) != 0) {
      lastForegroundNotificationAttached = false;
    }
    stopForeground((flags & Service.STOP_FOREGROUND_REMOVE) != 0);
  }

  private void removeForegroundNotification() {
    NotificationManager nm =
        (NotificationManager)
            RuntimeEnvironment.getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(lastForegroundNotificationId);
    lastForegroundNotification = null;
    lastForegroundNotificationAttached = false;
  }

  public int getLastForegroundNotificationId() {
    return lastForegroundNotificationId;
  }

  public Notification getLastForegroundNotification() {
    return lastForegroundNotification;
  }

  /**
   * Returns whether the last foreground notification is still "attached" to the service,
   * meaning it will be removed when the service is destroyed.
   */
  public boolean isLastForegroundNotificationAttached() {
    return lastForegroundNotificationAttached;
  }

  /**
   * @return Is this service stopped by self.
   */
  public boolean isStoppedBySelf() {
    return selfStopped;
  }

  public boolean isForegroundStopped() {
    return foregroundStopped;
  }

  public boolean getNotificationShouldRemoved() {
    return notificationShouldRemoved;
  }

  /**
   * Returns id passed to {@link #stopSelf(int)} method. Make sure to check result of {@link
   * #isStoppedBySelf()} first.
   */
  public int getStopSelfId() {
    return stopSelfId;
  }

  /**
   * Returns id passed to {@link #stopSelfResult(int)} method. Make sure to check result of {@link
   * #isStoppedBySelf()} first.
   */
  public int getStopSelfResultId() {
    return stopSelfResultId;
  }
}
