package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeDashPathEffectTest {
  private static final int BITMAP_WIDTH = 200;
  private static final int BITMAP_HEIGHT = 20;
  private static final int START_X = 10;
  private static final int END_X = BITMAP_WIDTH - START_X;
  private static final int COORD_Y = BITMAP_HEIGHT / 2;
  private static final float[] PATTERN = new float[] {15, 5, 10, 5};
  private static final int OFFSET = 5;
  private static final int BACKGROUND = Color.TRANSPARENT;
  private static final int FOREGROUND = Color.GREEN;

  @Test
  public void testDashPathEffect() {
    PathEffect effect = new DashPathEffect(PATTERN, OFFSET);
    Bitmap bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(BACKGROUND);

    Path path = new Path();
    path.moveTo(START_X, COORD_Y);
    path.lineTo(END_X, COORD_Y);

    Paint paint = new Paint();
    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(0);
    paint.setColor(FOREGROUND);
    paint.setPathEffect(effect);
    paint.setAntiAlias(false);

    Canvas canvas = new Canvas(bitmap);
    canvas.drawPath(path, paint);

    PatternIterator iterator = new PatternIterator(PATTERN, OFFSET);
    for (int y = 0; y < BITMAP_HEIGHT; y++) {
      for (int x = 0; x < BITMAP_WIDTH; x++) {
        try {
          if (y == COORD_Y && x >= START_X && x < END_X) {
            if (iterator.next()) {
              assertEquals(FOREGROUND, bitmap.getPixel(x, y));
            } else {
              assertEquals(BACKGROUND, bitmap.getPixel(x, y));
            }
          } else {
            assertEquals(BACKGROUND, bitmap.getPixel(x, y));
          }
        } catch (Error e) {
          Log.w(getClass().getName(), "Failed at (" + x + "," + y + ")");
          throw e;
        }
      }
    }
  }

  private static class PatternIterator {
    private int mPatternOffset;
    private int mLength;
    private final float[] mPattern;

    /** Create an instance that iterates through the given pattern starting at the given offset. */
    PatternIterator(final float[] pattern, int offset) {
      mPattern = pattern;
      while (offset-- > 0) {
        next();
      }
    }

    /** Determine whether to draw the current pixel and move on to the next. */
    @CanIgnoreReturnValue
    boolean next() {
      int oldPatternOffset = mPatternOffset;
      mLength += 1;
      if (mLength == mPattern[mPatternOffset]) {
        mLength = 0;
        mPatternOffset += 1;
        mPatternOffset %= mPattern.length;
      }
      // even offsets are 'on'
      return (oldPatternOffset & 1) == 0;
    }
  }
}
