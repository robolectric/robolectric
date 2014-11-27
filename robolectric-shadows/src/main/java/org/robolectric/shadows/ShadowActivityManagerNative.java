package org.robolectric.shadows;

import android.app.IActivityManager;
import android.app.RobolectricActivityManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(android.app.ActivityManagerNative.class)
public class ShadowActivityManagerNative {
  private static final RobolectricActivityManager activityManager = new RobolectricActivityManager();

  @Implementation
  static public IActivityManager getDefault() {
    return activityManager;
  }
}
