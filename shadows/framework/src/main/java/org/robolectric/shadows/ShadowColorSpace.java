package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.ColorSpace;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Container for implementations of ColorSpace
 */
public class ShadowColorSpace {

  @SuppressWarnings({"UnusedDeclaration"})
  @Implements(value = ColorSpace.Rgb.class, minSdk = O)
  public static class ShadowRgb {

    @Implementation(minSdk = android.os.Build.VERSION_CODES.Q)
    protected long getNativeInstance() {
      return 1;
    }
  }
}

