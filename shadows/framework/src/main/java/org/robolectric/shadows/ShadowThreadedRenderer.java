package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(
    className = "android.view.ThreadedRenderer",
    isInAndroidSdk = false,
    minSdk = O,
    maxSdk = P)
public class ShadowThreadedRenderer {

  @Implementation
  protected static Bitmap createHardwareBitmap(
      @ClassName("android.view.RenderNode") Object node, int width, int height) {
    return createHardwareBitmap(width, height);
  }

  private static Bitmap createHardwareBitmap(int width, int height) {
    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.HARDWARE);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.setMutable(false);
    return bitmap;
  }

  @Implementation
  protected static long nCreateTextureLayer(long nativeProxy) {
    return ShadowVirtualRefBasePtr.put(nativeProxy);
  }
}
