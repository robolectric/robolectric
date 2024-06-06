package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.TableMaskFilter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeTableMaskFilterTest {
  private static final int TOLERANCE = 2;

  private void verifyColor(int expected, int actual) {
    ColorUtils.verifyColor(expected, actual, TOLERANCE);
  }

  @Test
  public void testConstructor() {
    Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    Paint paint = new Paint();

    paint.setColor(Color.MAGENTA);
    paint.setMaskFilter(TableMaskFilter.CreateGammaTable(10.0f));
    canvas.drawPaint(paint);
    verifyColor(Color.MAGENTA, bitmap.getPixel(0, 0));

    paint.setColor(Color.MAGENTA);
    paint.setMaskFilter(TableMaskFilter.CreateClipTable(0, 20));
    canvas.drawPaint(paint);
    verifyColor(Color.MAGENTA, bitmap.getPixel(0, 0));

    paint.setColor(Color.MAGENTA);
    paint.setMaskFilter(new TableMaskFilter(new byte[256]));
    canvas.drawPaint(paint);
    verifyColor(Color.MAGENTA, bitmap.getPixel(0, 0));
  }
}
