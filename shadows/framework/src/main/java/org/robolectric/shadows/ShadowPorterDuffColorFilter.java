package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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

  /**
   * @return Returns the ARGB color used to tint the source pixels when this filter is applied.
   */
  @Implementation
  public int getColor() {

    return reflector(PorterDuffColorFilterReflector.class, realPorterDuffColorFilter).getColor();
  }

  /**
   * @return Returns the Porter-Duff mode used to composite this color filter's color with the
   *     source pixel when this filter is applied.
   */
  @Implementation
  public PorterDuff.Mode getMode() {

    return reflector(PorterDuffColorFilterReflector.class, realPorterDuffColorFilter).getMode();
  }

  @ForType(PorterDuffColorFilter.class)
  interface PorterDuffColorFilterReflector {
    @Accessor("mColor")
    int getColor();

    @Accessor("mMode")
    PorterDuff.Mode getMode();
  }
}
