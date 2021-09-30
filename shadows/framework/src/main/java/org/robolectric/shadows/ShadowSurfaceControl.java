package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.SurfaceControl;
import dalvik.system.CloseGuard;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.view.SurfaceControl} */
@Implements(value = SurfaceControl.class, isInAndroidSdk = false, minSdk = JELLY_BEAN_MR2)
public class ShadowSurfaceControl {

  @RealObject private SurfaceControl realSurfaceControl;

  @Implementation
  protected void finalize() throws Throwable {
    // Suppress noisy CloseGuard errors.
    CloseGuard closeGuard =
        reflector(SurfaceControlReflector.class, realSurfaceControl).getCloseGuard();
    if (closeGuard != null) {
      closeGuard.close();
    }
    reflector(SurfaceControlReflector.class, realSurfaceControl).finalize();
  }

  @ForType(SurfaceControl.class)
  interface SurfaceControlReflector {
    @Accessor("mCloseGuard")
    CloseGuard getCloseGuard();

    @Direct
    void finalize();
  }
}
