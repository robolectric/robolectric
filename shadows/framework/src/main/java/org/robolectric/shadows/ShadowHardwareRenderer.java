package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.HardwareRenderer;
import android.graphics.RenderNode;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;

/** No-op shadow for {@link HardwareRenderer}. */
@Implements(value = HardwareRenderer.class, isInAndroidSdk = false, minSdk = Q)
public class ShadowHardwareRenderer {

  private static long nextCreateProxy = 0;

  @Implementation(maxSdk = Q)
  protected static long nCreateProxy(boolean translucent, long rootRenderNode) {
    return ++nextCreateProxy;
  }

  @Implementation
  protected static long nCreateTextureLayer(long nativeProxy) {
    return ShadowVirtualRefBasePtr.put(nativeProxy);
  }

  @Implementation(minSdk = R, maxSdk = R)
  protected static long nCreateProxy(
      boolean translucent, boolean isWideGamut, long rootRenderNode) {
    return nCreateProxy(translucent, rootRenderNode);
  }

  // `nCreateProxy` function signature changed in R, have to create two functions with different
  // function name for pre-R and post-R.
  @Implementation(minSdk = S, methodName = "nCreateProxy")
  protected static long nCreateProxyFromS(boolean translucent, long rootRenderNode) {
    return nCreateProxy(translucent, rootRenderNode);
  }

  @Implementation
  protected static Bitmap createHardwareBitmap(
      @ClassName("android.graphics.RenderNode") RenderNode node, int width, int height) {
    return createHardwareBitmap(width, height);
  }

  private static Bitmap createHardwareBitmap(int width, int height) {
    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.HARDWARE);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.setMutable(false);
    return bitmap;
  }

  @Resetter
  public static void reset() {
    nextCreateProxy = 0;
  }
}
