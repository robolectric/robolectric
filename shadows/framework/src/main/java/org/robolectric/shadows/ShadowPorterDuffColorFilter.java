package org.robolectric.shadows;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(PorterDuffColorFilter.class)
public class ShadowPorterDuffColorFilter {
  private int color;
  private PorterDuff.Mode mode;

  @Implementation
  public void __constructor__(int color, PorterDuff.Mode mode) {
    this.color = color;
    this.mode = mode;
  }

  @Implementation
  public void setColor(int color) {
    this.color = color;
  }

  @Implementation
  public void setMode(PorterDuff.Mode mode) {
    this.mode = mode;
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
