package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeComposePathEffectTest {
  private static final int BITMAP_WIDTH = 110;
  private static final int BITMAP_HEIGHT = 20;
  private static final int START_X = 10;
  private static final int END_X = BITMAP_WIDTH - 10;
  private static final int CENTER = BITMAP_HEIGHT / 2;

  @Test
  public void testComposePathEffect() {
    Path path = new Path();
    path.moveTo(START_X, CENTER);
    path.lineTo(END_X, CENTER);

    // ----- ----- ----- -----
    PathEffect innerEffect = new DashPathEffect(new float[] {25, 5}, 0);
    // outer effect is applied to every segment of the inner effect
    // --- --- --- ---
    PathEffect outerEffect = new DashPathEffect(new float[] {15, 5}, 0);
    // --- - --- - --- - --- -
    PathEffect composedEffect = new ComposePathEffect(outerEffect, innerEffect);
    // --- - --- - --- - --- -
    PathEffect expectedEffect = new DashPathEffect(new float[] {15, 5, 5, 5}, 0);

    Bitmap actual = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(actual);
    Paint paint = makePaint();
    paint.setPathEffect(composedEffect);
    canvas.drawPath(path, paint);

    Bitmap expected = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(expected);
    paint = makePaint();
    paint.setPathEffect(expectedEffect);
    canvas.drawPath(path, paint);

    for (int y = 0; y < BITMAP_HEIGHT; y++) {
      for (int x = 0; x < BITMAP_WIDTH; x++) {
        assertEquals(expected.getPixel(x, y), actual.getPixel(x, y));
      }
    }
  }

  private Paint makePaint() {
    Paint paint = new Paint();
    paint.setColor(Color.GREEN);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(0);
    return paint;
  }
}
