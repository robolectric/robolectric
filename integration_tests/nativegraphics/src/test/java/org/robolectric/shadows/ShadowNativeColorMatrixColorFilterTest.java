package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeColorMatrixColorFilterTest {

  @Test
  public void testColorMatrixColorFilter() {
    ColorMatrixColorFilter filter;

    ColorMatrix cm = new ColorMatrix();
    float[] blueToCyan =
        new float[] {
          1f, 0f, 0f, 0f, 0f,
          0f, 1f, 1f, 0f, 0f,
          0f, 0f, 1f, 0f, 0f,
          0f, 0f, 0f, 1f, 0f
        };
    cm.set(blueToCyan);
    filter = new ColorMatrixColorFilter(cm);
    Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    Paint paint = new Paint();
    paint.setColor(Color.BLUE);
    paint.setColorFilter(filter);
    paint.setAntiAlias(false);
    canvas.drawPoint(0, 0, paint);
    ColorUtils.verifyColor(Color.CYAN, bitmap.getPixel(0, 0));

    paint.setColor(Color.GREEN);
    canvas.drawPoint(0, 0, paint);
    ColorUtils.verifyColor(Color.GREEN, bitmap.getPixel(0, 0));

    paint.setColor(Color.RED);
    canvas.drawPoint(0, 0, paint);
    ColorUtils.verifyColor(Color.RED, bitmap.getPixel(0, 0));

    // color components are clipped, not scaled
    paint.setColor(Color.MAGENTA);
    canvas.drawPoint(0, 0, paint);
    ColorUtils.verifyColor(Color.WHITE, bitmap.getPixel(0, 0));

    float[] transparentRedAddBlue =
        new float[] {
          1f, 0f, 0f, 0f, 0f,
          0f, 1f, 0f, 0f, 0f,
          0f, 0f, 1f, 0f, 64f,
          -0.5f, 0f, 0f, 1f, 0f
        };
    filter = new ColorMatrixColorFilter(transparentRedAddBlue);
    paint.setColorFilter(filter);
    paint.setColor(Color.RED);
    bitmap.eraseColor(Color.TRANSPARENT);
    canvas.drawPoint(0, 0, paint);
    // the bitmap stores the result in premul colors and we read out an
    // unpremultiplied result, which causes us to need a bigger tolerance in
    // this case (due to the fact that scaling by 1/255 is not exact).
    ColorUtils.verifyColor(Color.argb(128, 255, 0, 64), bitmap.getPixel(0, 0), 2);
    paint.setColor(Color.CYAN);
    canvas.drawPoint(0, 0, paint);
    // blue gets clipped
    ColorUtils.verifyColor(Color.CYAN, bitmap.getPixel(0, 0));

    // change array to filter out green
    assertEquals(1f, transparentRedAddBlue[6], 0.0f);
    transparentRedAddBlue[6] = 0f;
    // changing the array has no effect
    canvas.drawPoint(0, 0, paint);
    ColorUtils.verifyColor(Color.CYAN, bitmap.getPixel(0, 0));
    // create a new filter with the changed matrix
    paint.setColorFilter(new ColorMatrixColorFilter(transparentRedAddBlue));
    canvas.drawPoint(0, 0, paint);
    ColorUtils.verifyColor(Color.BLUE, bitmap.getPixel(0, 0));
  }

  @Test
  public void testGetColorMatrix() {
    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(new ColorMatrix());
    ColorMatrix getMatrix = new ColorMatrix();

    filter.getColorMatrix(getMatrix);
    assertEquals(new ColorMatrix(), getMatrix);

    ColorMatrix scaleTranslate =
        new ColorMatrix(
            new float[] {
              1, 0, 0, 0, 8,
              0, 2, 0, 0, 7,
              0, 0, 3, 0, 6,
              0, 0, 0, 4, 5
            });

    filter = new ColorMatrixColorFilter(scaleTranslate);
    filter.getColorMatrix(getMatrix);
    assertEquals(scaleTranslate, getMatrix);
    assertArrayEquals(scaleTranslate.getArray(), getMatrix.getArray(), 0);
  }
}
