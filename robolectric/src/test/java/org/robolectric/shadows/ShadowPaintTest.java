package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Color;
import android.graphics.Paint;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
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
    shadowPaint.setAntiAlias(false);
    assertFalse(paint.isAntiAlias());
  }

  @Test
  public void testCtor() {
    assertThat(new Paint(Paint.ANTI_ALIAS_FLAG).isAntiAlias()).isTrue();
    assertThat(new Paint(0).isAntiAlias()).isFalse();
  }

  @Test
  public void testCtorWithPaint() {
    Paint paint = new Paint();
    paint.setColor(Color.RED);
    paint.setAlpha(72);
    paint.setFlags(2345);

    Paint other = new Paint(paint);
    assertThat(other.getColor()).isEqualTo(Color.RED);
    assertThat(other.getAlpha()).isEqualTo(72);
    assertThat(other.getFlags()).isEqualTo(2345);
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
    assertThat(paint.measureText("Hello")).isEqualTo(5.0f);
    assertThat(paint.measureText("Hello", 1, 3)).isEqualTo(2.0f);
    assertThat(paint.measureText(new StringBuilder("Hello"), 1, 4)).isEqualTo(3.0f);
  }

  @Test
  public void createPaintFromPaint() throws Exception {
    Paint origPaint = new Paint();
    assertThat(new Paint(origPaint).getTextLocale()).isSameAs(origPaint.getTextLocale());
  }
}
