package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeCanvasTest {
  @Test
  public void testDrawColor() {
    Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bm);
    canvas.drawColor(Color.BLUE);
    assertThat(bm.getPixel(0, 0)).isEqualTo(Color.BLUE);
  }

  @Config(minSdk = O, maxSdk = O_MR1)
  @Test
  public void setHighContrastText_preP() {
    Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bm);
    ReflectionHelpers.callInstanceMethod(
        canvas, "setHighContrastText", ClassParameter.from(boolean.class, true));
  }

  @Test
  public void testDrawPaint() {
    Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bm);

    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    p.setColor(Color.BLUE);
    canvas.drawRect(new Rect(0, 0, 100, 100), p);
    assertThat(bm.getPixel(0, 0)).isEqualTo(Color.BLUE);
  }

  @Test
  public void testSetBitmap() {
    Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas();
    canvas.setBitmap(bm);

    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    p.setColor(Color.BLUE);
    canvas.drawRect(new Rect(0, 0, 100, 100), p);
    assertThat(bm.getPixel(0, 0)).isEqualTo(Color.BLUE);
  }

  @Test
  public void testDrawTextRunChars() {
    Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas();
    canvas.setBitmap(bm);
    Paint paint = new Paint();
    paint.setColor(Color.WHITE);
    paint.setStyle(Style.FILL);
    canvas.drawPaint(paint);
    paint.setColor(Color.BLACK);
    canvas.drawTextRun(new char[] {'h', 'e', 'l', 'l', 'o'}, 0, 5, 0, 5, 30, 30, false, paint);
    assertThat(isAllWhite(bm)).isFalse(); // check *something* was drawn
  }

  private boolean isAllWhite(Bitmap bitmap) {
    for (int i = 0; i < bitmap.getWidth(); i++) {
      for (int j = 0; j < bitmap.getHeight(); j++) {
        if (bitmap.getPixel(i, j) != Color.WHITE) {
          return false;
        }
      }
    }
    return true;
  }
}
