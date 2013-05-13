package org.robolectric.shadows;


import android.graphics.drawable.ColorDrawable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(ColorDrawable.class)
public class ShadowColorDrawable extends ShadowDrawable {
  @RealObject ColorDrawable realColorDrawable;

  @Override @Implementation
  public boolean equals(Object o) {
    if (!(o instanceof ColorDrawable)) return false;
    ColorDrawable other = (ColorDrawable) o;
    if (realColorDrawable == other) return true;
    if (realColorDrawable.getColor() != other.getColor()) return false;
    if (realColorDrawable.getAlpha() != other.getAlpha()) return false;
    if (realColorDrawable.getOpacity() != other.getOpacity()) return false;
    return super.equals(o);
  }

  @Override @Implementation
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + realColorDrawable.getColor();
    result = 31 * result + realColorDrawable.getAlpha();
    result = 31 * result + realColorDrawable.getOpacity();
    return result;
  }

  @Override @Implementation
  public String toString() {
    return String.format("ColorDrawable{color=#%06x, alpha=0x%02x}",
        realColorDrawable.getColor() & 0xffffff, realColorDrawable.getAlpha());
  }
}
