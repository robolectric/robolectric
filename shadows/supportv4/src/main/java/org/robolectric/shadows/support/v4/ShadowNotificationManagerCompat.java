package org.robolectric.shadows.support.v4;

import static org.robolectric.Shadows.shadowOf;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Robolectric shadow class for {@link NotificationManagerCompat}. */
@Implements(NotificationManagerCompat.class)
public class ShadowNotificationManagerCompat {
  @RealObject NotificationManagerCompat notificationManagerCompat;
  private boolean areNotificationsEnabled = true;
  private static ShadowNotificationManagerCompat forcedNotificationManagerCompat;

  /**
   * Sets areNotificationsEnabled value for this ShadowNotificationManagerCompat.
   *
   * <p>This value is only used for SDK versions 19-23. Under 19 the value is always true in the
   * Real Object. 24 and above the value to return is delegated back to the non-compat
   * NotificationManager. Tests that use setNotificationsEnabled() against the
   * ShadowNotificationManager will behave as expected.
   *
   * @param enabled value to return for areNotificationsEnabled
   */
  public void setNotificationsEnabled(boolean enabled) {
    // Set the non-compat version as well to ensure a consistent experience for SDK 24+
    if (Build.VERSION.SDK_INT >= 24) {
      shadowOf(
              (NotificationManager)
                  RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE))
          .setNotificationsEnabled(enabled);
    }
    areNotificationsEnabled = enabled;
  }

  /**
   * This allows a specific shadow to be set to be returned whenever
   * NotificationManagerCompat.from() is called. Since NotificationManagerCompat is not often
   * injected.
   *
   * @param notificationManagerCompat ShadowNotificationManagerCompat to return when
   *     NotificationManagerCompat.from() is called anywhere.
   */
  public static void setFrom(ShadowNotificationManagerCompat notificationManagerCompat) {
    forcedNotificationManagerCompat = notificationManagerCompat;
  }

  @Implementation
  public boolean areNotificationsEnabled() {
    // For SDK 24 the NotificationManagerCompat throws the call back to the non compat version. This
    // does the same.
    if (Build.VERSION.SDK_INT >= 24) {
      NotificationManager notificationManagerNonCompat =
          (NotificationManager)
              RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);
      return notificationManagerNonCompat.areNotificationsEnabled();
    } else if (Build.VERSION.SDK_INT >= 19) {
      return areNotificationsEnabled;
    }

    // SDKs under 19 always return true, thus the shadow does as well.
    return true;
  }

  @Implementation
  public static NotificationManagerCompat from(Context context) {
    if (forcedNotificationManagerCompat != null) {
      return forcedNotificationManagerCompat.notificationManagerCompat;
    }

    return Shadow.directlyOn(
        NotificationManagerCompat.class, "from", ClassParameter.from(Context.class, context));
  }
}
