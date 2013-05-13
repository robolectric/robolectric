package org.robolectric.shadows;

import android.os.Looper;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = Robolectric.Anything.class, className = "android.view.ViewRootImpl")
public class ShadowViewRootImpl {
  @Implementation
  public static Object getWindowSession(Looper mainLooper) {
    return null;
  }

  @Implementation
  public void playSoundEffect(int effectId) {
  }
}
