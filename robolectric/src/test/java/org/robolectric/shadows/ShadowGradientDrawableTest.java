package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.drawable.GradientDrawable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowGradientDrawableTest {
  @Test
  public void testGetLastSetColor_returnsColor() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);
    int color = 123;
    gradientDrawable.setColor(color);
    assertThat(shadowGradientDrawable.getLastSetColor()).isEqualTo(color);
  }

  @Test
  public void testGetStrokeWidth_returnsStrokeWidth() {
    int strokeWidth = 123;
    GradientDrawable gradientDrawable = new GradientDrawable();

    gradientDrawable.setStroke(strokeWidth, /* color= */ 456);

    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);
    assertThat(shadowGradientDrawable.getStrokeWidth()).isEqualTo(strokeWidth);
  }

  @Test
  public void testGetStrokeColor_returnsStrokeColor() {
    int stokeColor = 123;
    GradientDrawable gradientDrawable = new GradientDrawable();

    gradientDrawable.setStroke(/* width= */ 456, stokeColor);

    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);
    assertThat(shadowGradientDrawable.getStrokeColor()).isEqualTo(stokeColor);
  }
}
