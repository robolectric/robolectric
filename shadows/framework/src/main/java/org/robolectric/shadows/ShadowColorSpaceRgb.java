package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;

import android.graphics.ColorSpace;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link ColorSpace.Rgb}. */
@Implements(value = ColorSpace.Rgb.class, minSdk = O)
public class ShadowColorSpaceRgb {
  @Implementation(minSdk = Q)
  protected long getNativeInstance() {
    return 1;
  }
}
