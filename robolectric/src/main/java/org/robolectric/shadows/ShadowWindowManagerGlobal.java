package org.robolectric.shadows;

import android.os.Looper;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = Robolectric.Anything.class, className = ShadowWindowManagerGlobal.REAL_CLASS_NAME)
public class ShadowWindowManagerGlobal {

  public static final String REAL_CLASS_NAME = "android.view.WindowManagerGlobal";

  @Implementation
  public static Object getWindowSession() {
    return null;
  }

  @Implementation
  public static Object getWindowSession(Looper looper) {
    return null;
  }
}
