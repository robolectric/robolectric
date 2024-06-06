package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PathEffect;
import android.graphics.SumPathEffect;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeSumPathEffectTest {
  private static final int WIDTH = 100;
  private static final int HEIGHT = 100;

  @Test
  public void testSumPathEffect() {
    Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.BLACK);
    Bitmap expected = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
    expected.eraseColor(Color.BLACK);

    Path path = new Path();
    path.addRect(10, 10, WIDTH - 10, HEIGHT - 10, Direction.CW);

    PathEffect first = new CornerPathEffect(40);
    Canvas canvas = new Canvas(expected);
    Paint paint = new Paint();
    paint.setColor(Color.GREEN);
    paint.setPathEffect(first);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(0); // 1-pixel hairline
    paint.setAntiAlias(false);
    canvas.drawPath(path, paint);

    PathEffect second = new DashPathEffect(new float[] {10, 5}, 5);
    paint.setPathEffect(second);
    canvas.drawPath(path, paint);

    SumPathEffect sumPathEffect = new SumPathEffect(second, first);
    paint.setPathEffect(sumPathEffect);
    canvas = new Canvas(bitmap);
    canvas.drawPath(path, paint);

    for (int i = 0; i < WIDTH; i++) {
      for (int j = 0; j < HEIGHT; j++) {
        assertEquals(expected.getPixel(i, j), bitmap.getPixel(i, j));
      }
    }
  }
}
