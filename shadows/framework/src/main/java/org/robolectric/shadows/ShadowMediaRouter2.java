package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.media.MediaRouter2;
import android.os.Build;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow class for {@link android.media.MediaRouter2}. */
@Implements(value = MediaRouter2.class, minSdk = Build.VERSION_CODES.R)
public class ShadowMediaRouter2 {

  @Resetter
  public static void reset() {
    reflector(MediaRouter2Reflector.class).setInstance(null);
  }

  @ForType(MediaRouter2.class)
  interface MediaRouter2Reflector {
    @Static
    @Accessor("sInstance")
    void setInstance(MediaRouter2 instance);
  }
}
