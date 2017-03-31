package org.robolectric.shadows;

import android.os.Build;
import com.android.internal.os.BackgroundThread;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * Shadow for {@link com.android.internal.os.BackgroundThread}.
 */
@Implements(value = BackgroundThread.class, isInAndroidSdk = false, inheritImplementationMethods = true, minSdk = KITKAT)
public class ShadowBackgroundThread {

  @Resetter
  public static void reset() {
    BackgroundThread instance = ReflectionHelpers.getStaticField(BackgroundThread.class, "sInstance");
    if (instance != null) {
      instance.quit();
      ReflectionHelpers.setStaticField(BackgroundThread.class, "sInstance", null);
      ReflectionHelpers.setStaticField(BackgroundThread.class, "sHandler", null);
    }
  }
}
