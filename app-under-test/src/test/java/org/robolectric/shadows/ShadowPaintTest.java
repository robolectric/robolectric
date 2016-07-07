package org.robolectric.shadows;

import android.graphics.Paint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.internal.Shadow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;


@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowPaintTest {

  @Test
  public void shouldGetIsDitherInfo() {
    Paint paint = Shadow.newInstanceOf(Paint.class);
    assertFalse(paint.isAntiAlias());
    ShadowPaint shadowPaint = shadowOf(paint);
    shadowPaint.setAntiAlias(true);
    assertTrue(paint.isAntiAlias());
  }

  @Test
  public void shouldGetIsAntiAlias() {
    Paint paint = Shadow.newInstanceOf(Paint.class);
    assertFalse(paint.isAntiAlias());
    ShadowPaint shadowPaint = shadowOf(paint);
    shadowPaint.setAntiAlias(true);
    assertTrue(paint.isAntiAlias());
  }

  @Test
  public void testCtor() {
    Paint paint = Shadow.newInstanceOf(Paint.class);
    assertFalse(paint.isAntiAlias());
    ShadowPaint shadowPaint = shadowOf(paint);
    shadowPaint.__constructor__( Paint.ANTI_ALIAS_FLAG );
    assertTrue(paint.isAntiAlias());
  }

  @Test
  public void shouldGetAndSetTextAlignment() throws Exception {
    Paint paint = Shadow.newInstanceOf(Paint.class);
    assertThat(paint.getTextAlign()).isEqualTo(Paint.Align.LEFT);
    paint.setTextAlign(Paint.Align.CENTER);
    assertThat(paint.getTextAlign()).isEqualTo(Paint.Align.CENTER);
  }

  @Test
  public void measureTextActuallyMeasuresLength() throws Exception {
    Paint paint = Shadow.newInstanceOf(Paint.class);
    assertThat(paint.measureText("Hello")).isEqualTo(5);
    assertThat(paint.measureText("Hello", 1, 3)).isEqualTo(2);
    assertThat(paint.measureText(new StringBuilder("Hello"), 1, 4)).isEqualTo(3);
  }
}
