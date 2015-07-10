package org.robolectric.shadows;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.RobolectricActivityManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.app.ActivityManagerNative}.
 */
@Implements(value = ActivityManagerNative.class, isInAndroidSdk = false)
public class ShadowActivityManagerNative {
  private static final RobolectricActivityManager activityManager = new RobolectricActivityManager();

  @Implementation
  static public IActivityManager getDefault() {
    return activityManager;
  }
}
