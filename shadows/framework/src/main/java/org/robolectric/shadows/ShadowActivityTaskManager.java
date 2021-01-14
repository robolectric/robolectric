package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = ActivityTaskManager.class, minSdk = Q, isInAndroidSdk = false)
public class ShadowActivityTaskManager {
  @Implementation
  protected static IActivityTaskManager getService() {
    return ReflectionHelpers.createDeepProxy(IActivityTaskManager.class);
  }
}
