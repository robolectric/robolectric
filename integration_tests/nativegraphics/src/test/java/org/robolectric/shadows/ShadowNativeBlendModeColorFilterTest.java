package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q) // Added in API 29
public class ShadowNativeBlendModeColorFilterTest {
  private static final int TOLERANCE = 5;

  private static final int TEST_WIDTH = 90;
  private static final int TEST_HEIGHT = 90;
  private static final int LEFT_X = TEST_WIDTH / 4;
  private static final int RIGHT_X = TEST_WIDTH * 3 / 4;
  private static final int TOP_Y = TEST_HEIGHT / 4;
  private static final int BOTTOM_Y = TEST_HEIGHT * 3 / 4;

  private static final int FILTER_COLOR = Color.argb(0x80, 0, 0xFF, 0);

  private static final Point[] SAMPLE_POINTS = {
    new Point(LEFT_X, TOP_Y), new Point(LEFT_X, BOTTOM_Y), new Point(RIGHT_X, BOTTOM_Y)
  };

  private void testBlendModeColorFilter(int filterColor, BlendMode mode, int[] colors) {
    // The left side will be red.
    final Bitmap b1 = Bitmap.createBitmap(TEST_WIDTH / 2, TEST_HEIGHT, Bitmap.Config.ARGB_8888);
    b1.eraseColor(Color.RED);
    // The bottom will be blue.
    final Bitmap b2 = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT / 2, Bitmap.Config.ARGB_8888);
    b2.eraseColor(Color.BLUE);

    // This will be the final image, which is the blended combination of the above two bitmaps
    // on an otherwise white bitmap.
    final Bitmap b3 = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Bitmap.Config.ARGB_8888);
    b3.eraseColor(Color.WHITE);

    Canvas canvas = new Canvas(b3);

    canvas.drawColor(Color.WHITE);

    BlendModeColorFilter filter = new BlendModeColorFilter(filterColor, mode);
    Paint p = new Paint();
    canvas.drawBitmap(b1, 0, 0, p);
    p.setColorFilter(filter);
    canvas.drawBitmap(b2, 0, TEST_HEIGHT / 2, p);

    for (int i = 0; i < SAMPLE_POINTS.length; i++) {
      Point point = SAMPLE_POINTS[i];
      assertThat(Integer.toHexString(b3.getPixel(point.x, point.y)))
          .isEqualTo(Integer.toHexString(colors[i]));
    }
  }

  @Test
  public void testBlendModeColorFilter_SRC() {
    testBlendModeColorFilter(
        FILTER_COLOR, BlendMode.SRC, new int[] {Color.RED, 0xFF7F8000, 0xFF7FFF7f});
  }

  @Test
  public void testBlendModeColorFilter_DST() {
    testBlendModeColorFilter(
        FILTER_COLOR, BlendMode.DST, new int[] {Color.RED, Color.BLUE, Color.BLUE});
  }

  @Test
  public void testBlendModeColorFilter_SCREEN() {
    testBlendModeColorFilter(
        Color.GREEN, BlendMode.SCREEN, new int[] {Color.RED, Color.CYAN, Color.CYAN});
  }

  @Test
  public void testBlendModeColorFilterGetMode() {
    BlendModeColorFilter filter = new BlendModeColorFilter(Color.CYAN, BlendMode.SOFT_LIGHT);
    assertEquals(BlendMode.SOFT_LIGHT, filter.getMode());
  }

  @Test
  public void testBlendModeColorFilterGetColor() {
    BlendModeColorFilter filter = new BlendModeColorFilter(Color.MAGENTA, BlendMode.HARD_LIGHT);
    assertEquals(Color.MAGENTA, filter.getColor());
  }
}
