package org.robolectric.shadows;

import android.graphics.SurfaceTexture;
import java.lang.ref.WeakReference;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link android.graphics.SurfaceTexture} */
@Implements(SurfaceTexture.class)
public class ShadowSurfaceTexture {

  @ForType(SurfaceTexture.class)
  interface SurfaceTextureReflector {
    @Static
    @Direct
    void postEventFromNative(WeakReference<SurfaceTexture> weakSelf);

    @Static
    @Direct
    void postEventFromNative(Object weakSelf);
  }
}
