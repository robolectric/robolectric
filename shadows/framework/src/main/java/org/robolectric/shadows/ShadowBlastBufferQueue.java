package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.base.Preconditions.checkState;

import android.graphics.BLASTBufferQueue;
import android.graphics.PixelFormat;
import android.media.ImageReader;
import android.view.Surface;
import android.view.SurfaceControl;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = BLASTBufferQueue.class, isInAndroidSdk = false, minSdk = TIRAMISU)
public class ShadowBlastBufferQueue {

  private int width;
  private int height;

  @Implementation
  protected void update(SurfaceControl sc, int width, int height, int format) {
    this.width = width;
    this.height = height;
  }

  /**
   * The real Android implementation is backed by native code which is currently unsupported on host
   * OSes.
   *
   * <p>This implementation uses ImageReader to ensure a Surface where isValid() is true is
   * returned. Returning a valid Surface is vital to performing the real Draw logic.
   */
  @Implementation
  protected Surface createSurfaceWithHandle() {
    if (ShadowView.useRealDrawTraversals()) {
      checkState(width > 0);
      checkState(height > 0);
      ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
      Surface surface = imageReader.getSurface();

      ShadowNativeSurface shadowSurface = Shadow.extract(surface);
      shadowSurface.setContainerImageReader(imageReader);
      return surface;
    } else {
      return null;
    }
  }

  @Implementation
  protected Surface createSurface() {
    return createSurfaceWithHandle();
  }
}
