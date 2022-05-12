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

@RunWith(AndroidJUnit4.class)
public class ShadowPaintTest {

  @Test
  public void shouldGetIsDitherInfo() {
    Paint paint = new Paint(0);
    assertFalse(paint.isAntiAlias());
    ShadowPaint shadowPaint = shadowOf(paint);
    shadowPaint.setAntiAlias(true);
    assertTrue(paint.isAntiAlias());
  }

  @Test
  public void shouldGetIsAntiAlias() {
    Paint paint = new Paint(0);
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
  public void shouldGetAndSetTextAlignment() {
    Paint paint = new Paint();
    assertThat(paint.getTextAlign()).isEqualTo(Paint.Align.LEFT);
    paint.setTextAlign(Paint.Align.CENTER);
    assertThat(paint.getTextAlign()).isEqualTo(Paint.Align.CENTER);
  }

  @Test
  public void shouldSetUnderlineText() {
    Paint paint = new Paint();
    paint.setUnderlineText(true);
    assertThat(paint.isUnderlineText()).isTrue();
    paint.setUnderlineText(false);
    assertThat(paint.isUnderlineText()).isFalse();
  }

  @Test
  public void measureTextActuallyMeasuresLength() {
    Paint paint = new Paint();
    assertThat(paint.measureText("Hello")).isEqualTo(5.0f);
    assertThat(paint.measureText("Hello", 1, 3)).isEqualTo(2.0f);
    assertThat(paint.measureText(new StringBuilder("Hello"), 1, 4)).isEqualTo(3.0f);
  }

  @Test
  public void measureTextUsesTextScaleX() {
    Paint paint = new Paint();
    paint.setTextScaleX(1.5f);
    assertThat(paint.measureText("Hello")).isEqualTo(7.5f);
    assertThat(paint.measureText("Hello", 1, 3)).isEqualTo(3.0f);
    assertThat(paint.measureText(new StringBuilder("Hello"), 1, 4)).isEqualTo(4.5f);
  }

  @Test
  public void textWidthWithNegativeScaleXIsZero() {
    Paint paint = new Paint();
    paint.setTextScaleX(-1.5f);
    assertThat(paint.measureText("Hello")).isEqualTo(0f);
    assertThat(paint.measureText("Hello", 1, 3)).isEqualTo(0f);
    assertThat(paint.measureText(new StringBuilder("Hello"), 1, 4)).isEqualTo(0f);
  }

  @Test
  public void createPaintFromPaint() {
    Paint origPaint = new Paint();
    assertThat(new Paint(origPaint).getTextLocale()).isSameInstanceAs(origPaint.getTextLocale());
  }

  @Test
  public void breakTextReturnsNonZeroResult() {
    Paint paint = new Paint();
    assertThat(
            paint.breakText(
                new char[] {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'},
                /*index=*/ 0,
                /*count=*/ 11,
                /*maxWidth=*/ 100,
                /*measuredWidth=*/ null))
        .isGreaterThan(0);
    assertThat(
            paint.breakText(
                "Hello World",
                /*start=*/ 0,
                /*end=*/ 11,
                /*measureForwards=*/ true,
                /*maxWidth=*/ 100,
                /*measuredWidth=*/ null))
        .isGreaterThan(0);
    assertThat(
            paint.breakText(
                "Hello World",
                /*measureForwards=*/ true,
                /*maxWidth=*/ 100,
                /*measuredWidth=*/ null))
        .isGreaterThan(0);
  }

  @Test
  public void defaultTextScaleXIsOne() {
    Paint paint = new Paint();
    assertThat(paint.getTextScaleX()).isEqualTo(1f);
  }
}
