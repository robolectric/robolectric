package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.HardwareRenderer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(
    value = HardwareRenderer.class,
    isInAndroidSdk = false,
    looseSignatures = true,
    minSdk = Q)
public class ShadowHardwareRenderer {

  private static long nextCreateProxy = 0;

  @Implementation(maxSdk = Q)
  protected static long nCreateProxy(boolean translucent, long rootRenderNode) {
    return ++nextCreateProxy;
  }

  @Implementation(minSdk = R, maxSdk = R)
  protected static long nCreateProxy(
      boolean translucent, boolean isWideGamut, long rootRenderNode) {
    return nCreateProxy(translucent, rootRenderNode);
  }

  // need to use loose signatures here to account for signature changes
  @Implementation(minSdk = S)
  protected static long nCreateProxy(Object translucent, Object rootRenderNode) {
    return nCreateProxy((boolean) translucent, (long) rootRenderNode);
  }

  @Implementation
  protected static Bitmap createHardwareBitmap(
      /*RenderNode*/ Object node, /*int*/ Object width, /*int*/ Object height) {
    return createHardwareBitmap((int) width, (int) height);
  }

  private static Bitmap createHardwareBitmap(int width, int height) {
    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.HARDWARE);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.setMutable(false);
    return bitmap;
  }
}
