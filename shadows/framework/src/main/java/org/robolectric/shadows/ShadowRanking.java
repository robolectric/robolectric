package org.robolectric.shadows;

import android.app.NotificationChannel;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService.Ranking;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link android.service.notification.NotificationListenerService.Ranking}. */
@Implements(value = Ranking.class, minSdk = VERSION_CODES.KITKAT_WATCH)
public class ShadowRanking {
  private String key;
  private NotificationChannel notificationChannel;

  @Implementation
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Implementation(minSdk = VERSION_CODES.O)
  public NotificationChannel getChannel() {
    return notificationChannel;
  }

  public void setChannel(NotificationChannel notificationChannel) {
    this.notificationChannel = notificationChannel;
  }
}
