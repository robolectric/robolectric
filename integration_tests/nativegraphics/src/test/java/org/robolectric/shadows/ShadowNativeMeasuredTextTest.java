package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.text.MeasuredText;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(minSdk = Q)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeMeasuredTextTest {

  private static Paint paint;

  @Before
  public void setup() {
    paint = new Paint();
    Context context = RuntimeEnvironment.getApplication();
    AssetManager am = context.getAssets();
    Typeface tf = new Typeface.Builder(am, "fonts/layout/linebreak.ttf").build();
    paint.setTypeface(tf);
    paint.setTextSize(10.0f); // Make 1em = 10px
  }

  @Test
  public void testBuilder() {
    String text = "Hello, World";
    new MeasuredText.Builder(text.toCharArray())
        .appendStyleRun(paint, text.length(), false /* isRtl */)
        .build();
  }

  @Test
  public void testBuilder_fromExistingMeasuredText() {
    String text = "Hello, World";
    final MeasuredText mt =
        new MeasuredText.Builder(text.toCharArray())
            .appendStyleRun(paint, text.length(), false /* isRtl */)
            .build();
    assertNotNull(
        new MeasuredText.Builder(mt)
            .appendStyleRun(paint, text.length(), true /* isRtl */)
            .build());
  }

  @Test
  public void testBuilder_fromExistingMeasuredText_differentLayoutParam() {
    String text = "Hello, World";
    final MeasuredText mt =
        new MeasuredText.Builder(text.toCharArray())
            .setComputeLayout(false)
            .appendStyleRun(paint, text.length(), false /* isRtl */)
            .build();
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MeasuredText.Builder(mt)
                .appendStyleRun(paint, text.length(), true /* isRtl */)
                .build());
  }

  @Test
  public void testBuilder_fromExistingMeasuredText_differentHyphenationParam() {
    String text = "Hello, World";
    final MeasuredText mt =
        new MeasuredText.Builder(text.toCharArray())
            .setComputeHyphenation(false)
            .appendStyleRun(paint, text.length(), false /* isRtl */)
            .build();
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MeasuredText.Builder(mt)
                .setComputeHyphenation(true)
                .appendStyleRun(paint, text.length(), true /* isRtl */)
                .build());
  }

  @Test
  public void testBuilder_nullText() {
    assertThrows(NullPointerException.class, () -> new MeasuredText.Builder((char[]) null));
  }

  @Test
  public void testBuilder_nullMeasuredText() {
    assertThrows(NullPointerException.class, () -> new MeasuredText.Builder((MeasuredText) null));
  }

  @Test
  public void testBuilder_nullPaint() {
    String text = "Hello, World";
    assertThrows(
        NullPointerException.class,
        () ->
            new MeasuredText.Builder(text.toCharArray())
                .appendStyleRun(null, text.length(), false));
  }

  @Test
  public void testGetWidth() {
    String text = "Hello, World";
    MeasuredText mt =
        new MeasuredText.Builder(text.toCharArray())
            .appendStyleRun(paint, text.length(), false /* isRtl */)
            .build();
    assertEquals(0.0f, mt.getWidth(0, 0), 0.0f);
    assertEquals(10.0f, mt.getWidth(0, 1), 0.0f);
    assertEquals(20.0f, mt.getWidth(0, 2), 0.0f);
    assertEquals(10.0f, mt.getWidth(1, 2), 0.0f);
    assertEquals(20.0f, mt.getWidth(1, 3), 0.0f);
  }
}
