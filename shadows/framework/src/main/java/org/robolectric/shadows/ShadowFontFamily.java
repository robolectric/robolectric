package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;

import android.content.res.AssetManager;
import android.graphics.FontFamily;
import android.graphics.fonts.FontVariationAxis;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = FontFamily.class, minSdk = LOLLIPOP, isInAndroidSdk = false)
public class ShadowFontFamily {

  @Implementation(minSdk = O)
  protected static long nInitBuilder(String lang, int variant) {
    return 1;
  }

  @Implementation(minSdk = O)
  protected void abortCreation() {}

  @Implementation(minSdk = O)
  protected boolean addFontFromAssetManager(
      AssetManager mgr,
      String path,
      int cookie,
      boolean isAsset,
      int ttcIndex,
      int weight,
      int isItalic,
      FontVariationAxis[] axes) {
    return true;
  }

  @Implementation(minSdk = O)
  protected boolean freeze() {
    return true;
  }
}
