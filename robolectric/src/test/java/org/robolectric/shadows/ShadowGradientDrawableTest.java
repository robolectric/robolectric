package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.res.ColorStateList;
import android.graphics.Color;
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

    assertThat(shadowGradientDrawable.getLastSetColor()).isEqualTo(0);

    int color = 123;
    gradientDrawable.setColor(color);

    assertThat(shadowGradientDrawable.getLastSetColor()).isEqualTo(color);
  }

  @Test
  public void testGetShape_returnsShape() {
    GradientDrawable gradientDrawable = new GradientDrawable();
    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);

    assertThat(shadowGradientDrawable.getShape()).isEqualTo(0);

    int shape = GradientDrawable.OVAL;
    gradientDrawable.setShape(shape);

    assertThat(shadowGradientDrawable.getShape()).isEqualTo(shape);
  }

  @Test
  public void testGetStrokeProperties_defaultValues() {
    GradientDrawable gradientDrawable = new GradientDrawable();

    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);

    assertStrokeProperties(shadowGradientDrawable, 0, 0, null, 0f, 0f);
  }

  @Test
  public void testGetStrokeProperties_color() {
    int width = 2;
    int color = Color.RED;
    ColorStateList colorStateList = ColorStateList.valueOf(color);

    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setStroke(width, color);

    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);

    assertStrokeProperties(shadowGradientDrawable, width, color, colorStateList, 0f, 0f);
  }

  @Test
  public void testGetStrokeProperties_color_dash() {
    int width = 2;
    int color = Color.RED;
    ColorStateList colorStateList = ColorStateList.valueOf(color);
    float dashWidth = 3.5f;
    float dashGap = 5f;

    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setStroke(width, color, dashWidth, dashGap);

    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);

    assertStrokeProperties(
        shadowGradientDrawable, width, color, colorStateList, dashWidth, dashGap);
  }

  @Test
  public void testGetStrokeProperties_colorStateList() {
    int width = 2;
    int color = Color.RED;
    ColorStateList colorStateList = ColorStateList.valueOf(color);

    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setStroke(width, colorStateList);

    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);

    assertStrokeProperties(shadowGradientDrawable, width, color, colorStateList, 0f, 0f);
  }

  @Test
  public void testGetStrokeProperties_colorStateList_dash() {
    int width = 2;
    int color = Color.RED;
    ColorStateList colorStateList = ColorStateList.valueOf(color);
    float dashWidth = 3.5f;
    float dashGap = 5f;

    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setStroke(width, colorStateList, dashWidth, dashGap);

    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);

    assertStrokeProperties(
        shadowGradientDrawable, width, color, colorStateList, dashWidth, dashGap);
  }

  private void assertStrokeProperties(
      ShadowGradientDrawable shadow,
      int expectedWidth,
      int expectedColor,
      ColorStateList expectedColorStateList,
      float expectedDashWidth,
      float expectedDashGap) {
    assertThat(shadow.getStrokeWidth()).isEqualTo(expectedWidth);
    assertThat(shadow.getStrokeColor()).isEqualTo(expectedColor);
    assertThat(shadow.getStrokeColorStateList()).isEqualTo(expectedColorStateList);
    assertThat(shadow.getStrokeDashWidth()).isEqualTo(expectedDashWidth);
    assertThat(shadow.getStrokeDashGap()).isEqualTo(expectedDashGap);
  }
}
