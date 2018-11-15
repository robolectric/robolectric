package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.view.ThreadedRenderer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = ThreadedRenderer.class, isInAndroidSdk = false, minSdk = LOLLIPOP, looseSignatures = true)
public class ShadowThreadedRenderer {

  @Implementation(minSdk = O, maxSdk = P)
  protected static Bitmap createHardwareBitmap(Object rendererNode, Object width, Object height) {
    int w = (int) width;
    int h = (int) height;

    Bitmap bitmap = Bitmap.createBitmap(w, h, Config.HARDWARE);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.setMutable(false);
    return bitmap;
  }
}
