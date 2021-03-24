package org.robolectric.shadows;

import android.app.NotificationChannel;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService.Ranking;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link android.service.notification.NotificationListenerService.Ranking}. */
@Implements(value = Ranking.class, minSdk = VERSION_CODES.KITKAT_WATCH)
public class ShadowRanking {
  @RealObject private Ranking realObject;

  /** Overrides the return value for {@link Ranking#getChannel()}. */
  public void setChannel(NotificationChannel notificationChannel) {
    ReflectionHelpers.setField(realObject, "mChannel", notificationChannel);
  }

  /** Overrides the return value for {@link Ranking#isSuspended()}. */
  public void setHidden(boolean hidden) {
    ReflectionHelpers.setField(realObject, "mHidden", hidden);
  }
}
