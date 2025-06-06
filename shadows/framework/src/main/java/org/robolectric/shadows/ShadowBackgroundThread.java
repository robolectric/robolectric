package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Handler;
import com.android.internal.os.BackgroundThread;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = BackgroundThread.class, isInAndroidSdk = false)
public class ShadowBackgroundThread {

  @Resetter
  public static void reset() {
    BackgroundThreadReflector backgroundThreadReflector =
        reflector(BackgroundThreadReflector.class);

    BackgroundThread instance = backgroundThreadReflector.getInstance();
    if (instance != null) {
      instance.quit();
      backgroundThreadReflector.setInstance(null);
      backgroundThreadReflector.setHandler(null);
    }
  }

  /** Accessor interface for {@link BackgroundThread}'s internals. */
  @ForType(BackgroundThread.class)
  interface BackgroundThreadReflector {

    @Static
    @Accessor("sHandler")
    void setHandler(Handler o);

    @Static
    @Accessor("sInstance")
    void setInstance(BackgroundThread o);

    @Static
    @Accessor("sInstance")
    BackgroundThread getInstance();
  }
}
