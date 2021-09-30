package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import dalvik.system.CloseGuard;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.view.Surface} */
@Implements(Surface.class)
public class ShadowSurface {
  private SurfaceTexture surfaceTexture;
  @RealObject private Surface realSurface;

  @Implementation
  protected void __constructor__(SurfaceTexture surfaceTexture) {
    this.surfaceTexture = surfaceTexture;
    Shadow.invokeConstructor(
        Surface.class, realSurface, ClassParameter.from(SurfaceTexture.class, surfaceTexture));
  }

  public SurfaceTexture getSurfaceTexture() {
    return surfaceTexture;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected void finalize() throws Throwable {
    // Suppress noisy CloseGuard errors that may exist in SDK 17+.
    CloseGuard closeGuard = reflector(SurfaceReflector.class, realSurface).getCloseGuard();
    if (closeGuard != null) {
      closeGuard.close();
    }
    reflector(SurfaceReflector.class, realSurface).finalize();
  }

  @ForType(Surface.class)
  interface SurfaceReflector {
    @Accessor("mCloseGuard")
    CloseGuard getCloseGuard();

    @Direct
    void finalize();
  }
}
