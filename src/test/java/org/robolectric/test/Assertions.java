package org.robolectric.test;

import android.graphics.drawable.Drawable;

public class Assertions {
  public static <T extends Drawable> DrawableAssert<T> assertThat(T actual) {
    return new DrawableAssert<T>(actual);
  }
}
