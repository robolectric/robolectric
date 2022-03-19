package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(PorterDuffColorFilter.class)
public class ShadowPorterDuffColorFilter {
  private int color;
  private PorterDuff.Mode mode;

  @RealObject private PorterDuffColorFilter realPorterDuffColorFilter;

  @Implementation(maxSdk = KITKAT)
  protected void __constructor__(int color, PorterDuff.Mode mode) {
    // We need these copies because before Lollipop, PorterDuffColorFilter had no fields, it would
    // just delegate to a native instance. If we remove them, the shadow cannot access the fields
    // on KitKat and earlier.
    this.color = color;
    this.mode = mode;
  }

  /**
   * @return Returns the ARGB color used to tint the source pixels when this filter is applied.
   */
  @Implementation(minSdk = LOLLIPOP)
  public int getColor() {
    if (RuntimeEnvironment.getApiLevel() <= KITKAT) {
      return color;
    } else {
      return reflector(PorterDuffColorFilterReflector.class, realPorterDuffColorFilter).getColor();
    }
  }

  /**
   * @return Returns the Porter-Duff mode used to composite this color filter's color with the
   *     source pixel when this filter is applied.
   */
  @Implementation(minSdk = LOLLIPOP)
  public PorterDuff.Mode getMode() {
    if (RuntimeEnvironment.getApiLevel() <= KITKAT) {
      return mode;
    } else {
      return reflector(PorterDuffColorFilterReflector.class, realPorterDuffColorFilter).getMode();
    }
  }

  @ForType(PorterDuffColorFilter.class)
  interface PorterDuffColorFilterReflector {
    @Accessor("mColor")
    int getColor();

    @Accessor("mMode")
    PorterDuff.Mode getMode();
  }
}
