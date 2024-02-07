package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.drawable.PaintDrawable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ShadowPaintDrawableTest {
  @Test
  public void noCornerRadii_returnsNull() {
    PaintDrawable paintDrawable = new PaintDrawable();
    assertThat(shadowOf(paintDrawable).getCornerRadii()).isEqualTo(null);
  }

  @Test
  public void getCornerRadii_returnsCornerRadii() {
    PaintDrawable paintDrawable = new PaintDrawable();

    float[] radii = {10f, 20f, 30f, 40f, 50f, 60f, 70f, 80f};
    paintDrawable.setCornerRadii(radii);

    assertThat(shadowOf(paintDrawable).getCornerRadii()).isEqualTo(radii);
  }
}
