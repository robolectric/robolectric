package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PathDashPathEffect;
import android.graphics.RectF;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativePathDashPathEffectTest {
  private static final int SQUARE = 10;
  private static final int ADVANCE = 30;
  private static final int WIDTH = 100;
  private static final int HEIGHT = 100;

  @Test
  public void testPathDashPathEffect() {
    Bitmap b = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
    b.eraseColor(Color.BLACK);
    PathDashPathEffect effect =
        new PathDashPathEffect(shape(), ADVANCE, 0, PathDashPathEffect.Style.TRANSLATE);
    Canvas canvas = new Canvas(b);
    Paint p = new Paint();
    p.setPathEffect(effect);
    p.setColor(Color.RED);
    canvas.drawPath(path(), p);

    Bitmap expected = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
    expected.eraseColor(Color.BLACK);
    canvas = new Canvas(expected);
    p = new Paint();
    p.setColor(Color.RED);
    RectF rect = new RectF(0, HEIGHT / 2 - SQUARE, 0, HEIGHT / 2 + SQUARE);
    for (int i = 0; i <= WIDTH + SQUARE; i += ADVANCE) {
      rect.left = i - SQUARE;
      rect.right = i + SQUARE;
      canvas.drawRect(rect, p);
    }

    int diffCount = 0;
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        if (expected.getPixel(x, y) != b.getPixel(x, y)) {
          diffCount += 1;
        }
      }
    }
    assertEquals(0, diffCount);
  }

  private static Path path() {
    Path p = new Path();
    p.moveTo(0, HEIGHT / 2);
    p.lineTo(WIDTH, HEIGHT / 2);
    return p;
  }

  private static Path shape() {
    Path p = new Path();
    p.addRect(new RectF(-SQUARE, -SQUARE, SQUARE, SQUARE), Direction.CCW);
    return p;
  }
}
