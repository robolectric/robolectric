package org.robolectric.shadows;

import android.os.Looper;
import android.view.WindowManagerGlobal;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

/**
 * Shadow for {@link android.view.WindowManagerGlobal}.
 */
@Implements(value = WindowManagerGlobal.class, isInAndroidSdk = false, minSdk = JELLY_BEAN_MR1)
public class ShadowWindowManagerGlobal {

  @Implementation
  public static Object getWindowSession() {
    return null;
  }

  @Implementation
  public static Object getWindowSession(Looper looper) {
    return null;
  }

  @Implementation
  public static Object getWindowManagerService() {
    return null;
  }

}