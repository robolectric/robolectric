package org.robolectric.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.test.Assertions.assertThat;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import org.assertj.core.api.AbstractAssert;

public class DrawableAssert<T extends Drawable> extends AbstractAssert<DrawableAssert<T>, T> {
  public DrawableAssert(T actual) {
    super(actual, DrawableAssert.class);
  }

  public void isResource(int resourceId) {
    assertThat(actual).isInstanceOf(BitmapDrawable.class);
    BitmapDrawable bitmapDrawable = (BitmapDrawable) actual;
    assertThat(shadowOf(bitmapDrawable.getBitmap()).getCreatedFromResId())
        .isEqualTo(resourceId);
  }
}
