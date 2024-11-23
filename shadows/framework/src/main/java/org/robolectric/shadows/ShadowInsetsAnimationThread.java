package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.view.InsetsAnimationThread;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link InsetsAnimationThread}. */
@Implements(value = InsetsAnimationThread.class, minSdk = R, isInAndroidSdk = false)
public class ShadowInsetsAnimationThread {
  @Resetter
  public static void reset() {
    InsetsAnimationThread.release();
  }
}
