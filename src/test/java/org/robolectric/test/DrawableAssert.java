package org.robolectric.test;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import org.fest.assertions.api.AbstractAssert;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

public class DrawableAssert<T extends Drawable> extends AbstractAssert<DrawableAssert<T>, T> {
  public DrawableAssert(T actual) {
    super(actual, DrawableAssert.class);
  }

  public void isResource(int resourceId) {
    Assertions.assertThat(actual).isInstanceOf(BitmapDrawable.class);
    BitmapDrawable bitmapDrawable = (BitmapDrawable) actual;
    assertThat(shadowOf(bitmapDrawable.getBitmap()).getCreatedFromResId())
        .isEqualTo(resourceId);
  }
}
