package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.usage.BroadcastResponseStats;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow of {@link BroadcastResponseStats} for accessing hidden APIs. */
@Implements(value = BroadcastResponseStats.class, minSdk = TIRAMISU, isInAndroidSdk = false)
public class ShadowBroadcastResponseStats {

  @RealObject private BroadcastResponseStats broadcastResponseStats;

  @Implementation(minSdk = TIRAMISU)
  public void incrementBroadcastsDispatchedCount(int count) {
    reflector(BroadcastResponseStatsReflector.class, broadcastResponseStats)
        .incrementBroadcastsDispatchedCount(count);
  }

  @Implementation(minSdk = TIRAMISU)
  public void incrementNotificationsPostedCount(int count) {
    reflector(BroadcastResponseStatsReflector.class, broadcastResponseStats)
        .incrementNotificationsPostedCount(count);
  }

  @Implementation(minSdk = TIRAMISU)
  public void incrementNotificationsUpdatedCount(int count) {
    reflector(BroadcastResponseStatsReflector.class, broadcastResponseStats)
        .incrementNotificationsUpdatedCount(count);
  }

  @Implementation(minSdk = TIRAMISU)
  public void incrementNotificationsCancelledCount(int count) {
    reflector(BroadcastResponseStatsReflector.class, broadcastResponseStats)
        .incrementNotificationsCancelledCount(count);
  }

  @ForType(BroadcastResponseStats.class)
  interface BroadcastResponseStatsReflector {
    @Direct
    void incrementBroadcastsDispatchedCount(int count);

    @Direct
    void incrementNotificationsPostedCount(int count);

    @Direct
    void incrementNotificationsUpdatedCount(int count);

    @Direct
    void incrementNotificationsCancelledCount(int count);
  }
}
