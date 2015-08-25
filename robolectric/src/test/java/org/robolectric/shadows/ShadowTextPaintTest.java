package org.robolectric.shadows;

import android.text.TextPaint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowTextPaintTest {

  @Test
  public void measureText_returnsStringLengthAsMeasurement() throws Exception {
    TextPaint paint = new TextPaint();
    paint.getFontMetrics();
    assertEquals(4.0f, paint.measureText("1234"));
  }
}
