package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Handler;
import com.android.internal.os.BackgroundThread;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = BackgroundThread.class, isInAndroidSdk = false, minSdk = KITKAT)
public class ShadowBackgroundThread {

  @Resetter
  public static void reset() {
    _BackgroundThread_ _backgroundThreadStatic_ = reflector(_BackgroundThread_.class);

    BackgroundThread instance = _backgroundThreadStatic_.getInstance();
    if (instance != null) {
      instance.quit();
      _backgroundThreadStatic_.setInstance(null);
      _backgroundThreadStatic_.setHandler(null);
    }
  }

  /** Accessor interface for {@link BackgroundThread}'s internals. */
  @ForType(BackgroundThread.class)
  interface _BackgroundThread_ {

    @Static @Accessor("sHandler")
    void setHandler(Handler o);

    @Static @Accessor("sInstance")
    void setInstance(BackgroundThread o);

    @Static @Accessor("sInstance")
    BackgroundThread getInstance();
  }
}
