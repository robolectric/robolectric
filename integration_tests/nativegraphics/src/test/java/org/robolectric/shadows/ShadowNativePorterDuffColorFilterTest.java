package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativePorterDuffColorFilterTest {
  private static final int TOLERANCE = 5;

  @Test
  public void testPorterDuffColorFilter() {
    int width = 100;
    int height = 100;
    Bitmap b1 = Bitmap.createBitmap(width / 2, height, Bitmap.Config.ARGB_8888);
    b1.eraseColor(Color.RED);
    Bitmap b2 = Bitmap.createBitmap(width, height / 2, Bitmap.Config.ARGB_8888);
    b2.eraseColor(Color.BLUE);

    Bitmap target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    target.eraseColor(Color.TRANSPARENT);
    Canvas canvas = new Canvas(target);
    // semi-transparent green
    int filterColor = Color.argb(0x80, 0, 0xFF, 0);
    PorterDuffColorFilter filter = new PorterDuffColorFilter(filterColor, PorterDuff.Mode.SRC);
    Paint p = new Paint();
    canvas.drawBitmap(b1, 0, 0, p);
    p.setColorFilter(filter);
    canvas.drawBitmap(b2, 0, height / 2, p);
    assertEquals(Color.RED, target.getPixel(width / 4, height / 4));
    int lowerLeft = target.getPixel(width / 4, height * 3 / 4);
    assertEquals(0x80, Color.red(lowerLeft), TOLERANCE);
    assertEquals(0x80, Color.green(lowerLeft), TOLERANCE);
    int lowerRight = target.getPixel(width * 3 / 4, height * 3 / 4);
    assertEquals(filterColor, lowerRight);

    target.eraseColor(Color.BLACK);
    filter = new PorterDuffColorFilter(filterColor, PorterDuff.Mode.DST);
    p.setColorFilter(null);
    canvas.drawBitmap(b1, 0, 0, p);
    p.setColorFilter(filter);
    canvas.drawBitmap(b2, 0, height / 2, p);
    assertEquals(Color.RED, target.getPixel(width / 4, height / 4));
    assertEquals(Color.BLUE, target.getPixel(width / 4, height * 3 / 4));
    assertEquals(Color.BLUE, target.getPixel(width * 3 / 4, height * 3 / 4));

    target.eraseColor(Color.BLACK);
    filter = new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SCREEN);
    p.setColorFilter(null);
    canvas.drawBitmap(b1, 0, 0, p);
    p.setColorFilter(filter);
    canvas.drawBitmap(b2, 0, height / 2, p);
    assertEquals(Color.RED, target.getPixel(width / 4, height / 4));
    assertEquals(Color.CYAN, target.getPixel(width / 4, height * 3 / 4));
    assertEquals(Color.CYAN, target.getPixel(width * 3 / 4, height * 3 / 4));
  }

  @Test
  public void legacy_shadow_apis() {
    PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC);
    assertThat(shadowOf(filter).getColor()).isEqualTo(Color.GREEN);
    assertThat(shadowOf(filter).getMode()).isEqualTo(PorterDuff.Mode.SRC);
  }
}
