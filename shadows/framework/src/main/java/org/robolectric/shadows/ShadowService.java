package org.robolectric.shadows;

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
  private boolean selfStopped = false;
  private boolean foregroundStopped;
  private boolean notificationShouldRemoved;

  @Implementation
  public void onDestroy() {
    removeForegroundNotification();
  }

  @Implementation
  public void stopSelf() {
    selfStopped = true;
  }

  @Implementation
  public void stopSelf(int id) {
    selfStopped = true;
  }

  @Implementation
  public final void startForeground(int id, Notification notification) {
    foregroundStopped = false;
	  lastForegroundNotificationId = id;
    lastForegroundNotification = notification;
    notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
    NotificationManager nm = (NotificationManager)RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(id, notification);
  }

  @Implementation
  public void stopForeground(boolean removeNotification) {
    foregroundStopped = true;
    notificationShouldRemoved = removeNotification;
    if (removeNotification) {
      removeForegroundNotification();
    }
  }

  private void removeForegroundNotification() {
    NotificationManager nm = (NotificationManager)RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(lastForegroundNotificationId);    
    lastForegroundNotification = null;
  }
  
  public int getLastForegroundNotificationId() {
    return lastForegroundNotificationId;
  }
  
  public Notification getLastForegroundNotification() {
    return lastForegroundNotification;
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
}
