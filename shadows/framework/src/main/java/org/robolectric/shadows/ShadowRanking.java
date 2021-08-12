package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.NotificationChannel;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService.Ranking;
import java.util.ArrayList;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.service.notification.NotificationListenerService.Ranking}. */
@Implements(value = Ranking.class, minSdk = VERSION_CODES.KITKAT_WATCH)
public class ShadowRanking {
  @RealObject private Ranking realObject;

  /** Overrides the return value for {@link Ranking#getChannel()}. */
  public void setChannel(NotificationChannel notificationChannel) {
    reflector(RankingReflector.class, realObject).setChannel(notificationChannel);
  }

  /** Overrides the return value for {@link Ranking#getSmartReplies()}. */
  public void setSmartReplies(ArrayList<CharSequence> smartReplies) {
    reflector(RankingReflector.class, realObject).setSmartReplies(smartReplies);
  }

  /** Accessor interface for {@link Ranking}'s internals. */
  @ForType(Ranking.class)
  interface RankingReflector {

    @Accessor("mChannel")
    void setChannel(NotificationChannel notificationChannel);

    @Accessor("mSmartReplies")
    void setSmartReplies(ArrayList<CharSequence> smartReplies);
  }
}
