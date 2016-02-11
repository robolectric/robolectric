package org.robolectric.shadows;

import android.os.Build;
import android.text.TextPaint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowTextPaintTest {

  @Test
  public void measureText_returnsStringLengthAsMeasurement() throws Exception {
    TextPaint paint = new TextPaint();
    paint.getFontMetrics();
    assertEquals(4.0f, paint.measureText("1234"));
  }

  @Test
  @Config(rendering = true, sdk = Build.VERSION_CODES.LOLLIPOP)
  public void measureText_returnRealMeasurements() throws Exception {
    TextPaint paint = new TextPaint();
    paint.getFontMetrics();
    assertThat(paint.measureText("1234")).isGreaterThan(0);
  }
}
