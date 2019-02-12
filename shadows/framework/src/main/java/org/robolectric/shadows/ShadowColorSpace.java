package org.robolectric.shadows;

import android.graphics.ColorSpace;

import static android.os.Build.VERSION_CODES.O;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Container for implementations of ColorSpace
 */
public class ShadowColorSpace {

  @SuppressWarnings({"UnusedDeclaration"})
  @Implements(value = ColorSpace.Rgb.class, minSdk = O)
  public static class ShadowRgb {

  }
}

