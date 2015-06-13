package org.robolectric.shadows;

import android.os.Looper;
import android.view.ViewRootImpl;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.view.ViewRootImpl}.
 */
@Implements(value = ViewRootImpl.class, isInAndroidSdk = false)
public class ShadowViewRootImpl {

  @Implementation
  public static Object getWindowSession(Looper mainLooper) {
    return null;
  }

  @Implementation
  public void playSoundEffect(int effectId) {
  }
}
