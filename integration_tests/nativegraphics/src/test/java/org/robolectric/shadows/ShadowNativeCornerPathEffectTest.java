package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeCornerPathEffectTest {
  private static final int BITMAP_WIDTH = 100;
  private static final int BITMAP_HEIGHT = 100;
  private static final int PADDING = 10;
  private static final int RADIUS = 20;
  private static final int TOLERANCE = 5;

  @Test
  public void testCornerPathEffect() {
    Path path = new Path();
    path.moveTo(0, PADDING);
    path.lineTo(BITMAP_WIDTH - PADDING, PADDING);
    path.lineTo(BITMAP_WIDTH - PADDING, BITMAP_HEIGHT);

    PathEffect effect = new CornerPathEffect(RADIUS);

    Paint pathPaint = new Paint();
    pathPaint.setColor(Color.GREEN);
    pathPaint.setStyle(Style.STROKE);
    pathPaint.setStrokeWidth(0);
    pathPaint.setPathEffect(effect);
    pathPaint.setAntiAlias(false);

    Bitmap bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    // draw the path using the corner path effect
    canvas.drawPath(path, pathPaint);

    // create a path that describes the expected shape after the corner is rounded
    Path expectedPath = new Path();
    RectF oval =
        new RectF(
            BITMAP_WIDTH - PADDING - 2 * RADIUS,
            PADDING,
            BITMAP_WIDTH - PADDING,
            PADDING + 2 * RADIUS);
    expectedPath.moveTo(0, PADDING);
    expectedPath.arcTo(oval, 270, 90);
    expectedPath.lineTo(BITMAP_WIDTH - PADDING, BITMAP_HEIGHT);

    // A paint that draws the expected path with a tolerance width into the red channel
    Paint expectedPaint = new Paint();
    expectedPaint.setColor(Color.RED);
    expectedPaint.setStyle(Style.STROKE);
    expectedPaint.setStrokeWidth(TOLERANCE);
    expectedPaint.setXfermode(new PorterDuffXfermode(Mode.SCREEN));

    canvas.drawPath(expectedPath, expectedPaint);

    int numPixels = 0;
    for (int y = 0; y < BITMAP_HEIGHT; y++) {
      for (int x = 0; x < BITMAP_WIDTH; x++) {
        int pixel = bitmap.getPixel(x, y);
        if (Color.green(pixel) > 0) {
          numPixels += 1;
          // green path must overlap with red guide line
          assertEquals(Color.YELLOW, pixel);
        }
      }
    }
    // number of pixels that should be on a straight line
    int straightLines = BITMAP_WIDTH - PADDING - RADIUS + BITMAP_HEIGHT - PADDING - RADIUS;
    // number of pixels forming the corner
    int cornerPixels = numPixels - straightLines;
    // rounded corner must have less pixels than a sharp corner
    assertTrue(cornerPixels < 2 * RADIUS);
    // ... but not as few as a diagonal
    assertTrue(cornerPixels > Math.sqrt(2 * Math.pow(RADIUS, 2)));
  }
}
