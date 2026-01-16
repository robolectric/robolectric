package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.graphics.BLASTBufferQueue;
import android.graphics.PixelFormat;
import android.media.ImageReader;
import android.view.Display;
import android.view.Surface;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = BLASTBufferQueue.class, isInAndroidSdk = false, minSdk = TIRAMISU)
public class ShadowBlastBufferQueue {

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
      Display display = ShadowDisplay.getDefaultDisplay();

      int width = display.getWidth();
      int height = display.getHeight();

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
