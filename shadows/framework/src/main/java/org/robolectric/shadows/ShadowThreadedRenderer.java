package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.view.RenderNode;
import android.view.ThreadedRenderer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = ThreadedRenderer.class, isInAndroidSdk = false, minSdk = LOLLIPOP)
public class ShadowThreadedRenderer {

  @Implementation(minSdk = O)
  protected static Bitmap createHardwareBitmap(RenderNode node, int width, int height) {
    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.HARDWARE);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.setMutable(false);
    return bitmap;
  }
}
