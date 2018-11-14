package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.P;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;


@Implements(PorterDuffColorFilter.class)
public class ShadowPorterDuffColorFilter {
  private int color;
  private PorterDuff.Mode mode;
  @RealObject private PorterDuffColorFilter realPorterDuffColorFilter;


  @Implementation
  protected void __constructor__(int color, PorterDuff.Mode mode) {
    // We need these copies because before Lollipop, PorterDuffColorFilter had no fields, it would
    // just delegate to a native instance. If we remove them, the shadow cannot access the fields
    // on KitKat and earlier.
    this.color = color;
    this.mode = mode;
    Shadow.invokeConstructor(
        PorterDuffColorFilter.class,
        realPorterDuffColorFilter,
        ClassParameter.from(int.class, color),
        ClassParameter.from(PorterDuff.Mode.class, mode));
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = P)
  protected void setColor(int color) {
    this.color = color;
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = P)
  protected void setMode(PorterDuff.Mode mode) {
    this.mode = mode;
  }

  @Override @Implementation
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || !(object instanceof PorterDuffColorFilter)) {
      return false;
    }
    final PorterDuffColorFilter other = (PorterDuffColorFilter) object;
    return (color == other.getColor() && mode.nativeInt == other.getMode().nativeInt);
  }

  @Override @Implementation
  public int hashCode() {
    return 31 * mode.hashCode() + color;
  }

  /**
   * @return Returns the ARGB color used to tint the source pixels when this filter
   * is applied.
   */
  public int getColor() {
    return color;
  }

  /**
   * @return Returns the Porter-Duff mode used to composite this color filter's
   * color with the source pixel when this filter is applied.
   */
  public PorterDuff.Mode getMode() {
    return mode;
  }
}
