package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import android.graphics.drawable.GradientDrawable;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class GradientDrawableTest {
  @Test
  public void testGetColor_returnsColor() throws Exception {
    GradientDrawable gradientDrawable = new GradientDrawable();
    ShadowGradientDrawable shadowGradientDrawable = shadowOf(gradientDrawable);
    int color = 123;
    gradientDrawable.setColor(color);
    assertThat(shadowGradientDrawable.getColor()).isEqualTo(color);
  }
}
