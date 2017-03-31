package org.robolectric.shadows;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/**
 * Shadow for {@link android.app.ActivityManagerNative}.
 */
@Implements(value = ActivityManagerNative.class, isInAndroidSdk = false)
public class ShadowActivityManagerNative {
  private static final IActivityManager activityManager =
      ReflectionHelpers.createNullProxy(IActivityManager.class);

  @Implementation
  static public IActivityManager getDefault() {
    return activityManager;
  }
}
