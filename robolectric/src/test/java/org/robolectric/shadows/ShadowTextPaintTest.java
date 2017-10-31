package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.text.TextPaint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowTextPaintTest {

  @Test
  public void measureText_returnsStringLengthAsMeasurement() throws Exception {
    TextPaint paint = new TextPaint();
    assertThat(paint.measureText("1234")).isEqualTo(4f);
  }
}
