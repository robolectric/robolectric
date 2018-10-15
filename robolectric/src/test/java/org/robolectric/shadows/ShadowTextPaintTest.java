package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.text.TextPaint;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowTextPaintTest {

  @Test
  public void measureText_returnsStringLengthAsMeasurement() throws Exception {
    TextPaint paint = new TextPaint();
    assertThat(paint.measureText("1234")).isEqualTo(4f);
  }
}
