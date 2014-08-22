package org.robolectric.shadows;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import org.robolectric.annotation.Implements;

@Implements(PorterDuffColorFilter.class)
public class ShadowPorterDuffColorFilter {
  private int srcColor;
  private PorterDuff.Mode mode;

  public void __constructor__(int srcColor, PorterDuff.Mode mode) {
    this.srcColor = srcColor;
    this.mode = mode;
  }

  public int getSrcColor() {
    return srcColor;
  }

  public PorterDuff.Mode getMode() {
    return mode;
  }
}
