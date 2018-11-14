package org.robolectric.shadows;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ColorMatrixColorFilter.class)
public class ShadowColorMatrixColorFilter {
  private ColorMatrix matrix;

  @Implementation
  protected void __constructor__(ColorMatrix matrix) {
    this.matrix = matrix;
  }

  @Implementation
  protected void __constructor__(float[] array) {
    this.matrix = new ColorMatrix(array);
  }

  ColorMatrix getMatrix() {
    return matrix;
  }
}
