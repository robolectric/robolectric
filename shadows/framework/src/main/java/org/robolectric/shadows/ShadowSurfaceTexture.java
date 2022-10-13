package org.robolectric.shadows;

import android.graphics.SurfaceTexture;
import java.lang.ref.WeakReference;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link android.graphics.SurfaceTexture} */
@Implements(SurfaceTexture.class)
public class ShadowSurfaceTexture {

  @ForType(SurfaceTexture.class)
  interface SurfaceTextureReflector {
    @Static
    void postEventFromNative(WeakReference<SurfaceTexture> weakSelf);

    @Static
    void postEventFromNative(Object weakSelf);
  }
}
