package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeLightingColorFilterTest {
  private static final int TOLERANCE = 2;

  private void verifyColor(int expected, int actual) {
    ColorUtils.verifyColor(expected, actual, TOLERANCE);
  }

  @Test
  public void testLightingColorFilter() {
    Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    Paint paint = new Paint();

    paint.setColor(Color.MAGENTA);
    paint.setColorFilter(new LightingColorFilter(Color.WHITE, Color.BLACK));
    canvas.drawPaint(paint);
    verifyColor(Color.MAGENTA, bitmap.getPixel(0, 0));

    paint.setColor(Color.MAGENTA);
    paint.setColorFilter(new LightingColorFilter(Color.CYAN, Color.BLACK));
    canvas.drawPaint(paint);
    verifyColor(Color.BLUE, bitmap.getPixel(0, 0));

    paint.setColor(Color.MAGENTA);
    paint.setColorFilter(new LightingColorFilter(Color.BLUE, Color.GREEN));
    canvas.drawPaint(paint);
    verifyColor(Color.CYAN, bitmap.getPixel(0, 0));

    // alpha is ignored
    bitmap.eraseColor(Color.TRANSPARENT);
    paint.setColor(Color.MAGENTA);
    paint.setColorFilter(new LightingColorFilter(Color.TRANSPARENT, Color.argb(0, 0, 0xFF, 0)));
    canvas.drawPaint(paint);
    verifyColor(Color.GREEN, bitmap.getPixel(0, 0));

    // channels get clipped (no overflow into green or alpha)
    paint.setColor(Color.MAGENTA);
    paint.setColorFilter(new LightingColorFilter(Color.WHITE, Color.MAGENTA));
    canvas.drawPaint(paint);
    verifyColor(Color.MAGENTA, bitmap.getPixel(0, 0));

    // multiply before add
    paint.setColor(Color.argb(255, 60, 20, 40));
    paint.setColorFilter(
        new LightingColorFilter(Color.rgb(0x80, 0xFF, 0x80), Color.rgb(0, 10, 10)));
    canvas.drawPaint(paint);
    verifyColor(Color.argb(255, 30, 30, 30), bitmap.getPixel(0, 0));

    // source alpha remains unchanged
    bitmap.eraseColor(Color.TRANSPARENT);
    paint.setColor(Color.argb(0x80, 60, 20, 40));
    paint.setColorFilter(
        new LightingColorFilter(Color.rgb(0x80, 0xFF, 0x80), Color.rgb(0, 10, 10)));
    canvas.drawPaint(paint);
    verifyColor(Color.argb(0x80, 30, 30, 30), bitmap.getPixel(0, 0));
  }

  @Test
  public void testGetColorAdd() {
    LightingColorFilter filter = new LightingColorFilter(Color.WHITE, Color.BLACK);
    ColorUtils.verifyColor(Color.BLACK, filter.getColorAdd());

    filter = new LightingColorFilter(0x87654321, 0x12345678);
    ColorUtils.verifyColor(0x12345678, filter.getColorAdd());
  }

  @Test
  public void testGetColorMultiply() {
    LightingColorFilter filter = new LightingColorFilter(Color.WHITE, Color.BLACK);
    ColorUtils.verifyColor(Color.WHITE, filter.getColorMultiply());

    filter = new LightingColorFilter(0x87654321, 0x12345678);
    ColorUtils.verifyColor(0x87654321, filter.getColorMultiply());
  }
}
