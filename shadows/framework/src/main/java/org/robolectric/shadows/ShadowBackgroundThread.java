package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;

import com.android.internal.os.BackgroundThread;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = BackgroundThread.class, isInAndroidSdk = false, minSdk = KITKAT)
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
