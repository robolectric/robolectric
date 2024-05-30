package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.reflector.Reflector.reflector;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.util.reflector.WithType;

/**
 * Shadow for ThreadedRenderer.ProcessInitializer. This sets up some services for real Android which
 * are not used in Robolectric.
 */
@Implements(
    className = "android.view.ThreadedRenderer$ProcessInitializer",
    isInAndroidSdk = false,
    minSdk = O,
    maxSdk = P)
public class ShadowProcessInitializer {
  @Resetter
  public static void reset() {
    reflector(ProcessInitializerReflector.class)
        .setSInstance(reflector(ProcessInitializerReflector.class).newSInstance());
  }

  @ForType(className = "android.view.ThreadedRenderer$ProcessInitializer")
  interface ProcessInitializerReflector {
    @Constructor
    Object newSInstance();

    @Accessor("sInstance")
    @Static
    void setSInstance(
        @WithType("android.view.ThreadedRenderer$ProcessInitializer;") Object instance);
  }
}
