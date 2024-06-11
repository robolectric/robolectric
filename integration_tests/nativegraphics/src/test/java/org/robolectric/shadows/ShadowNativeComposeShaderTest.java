package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeComposeShaderTest {
  private static final int SIZE = 255;
  private static final int TOLERANCE = 5;

  @Test
  public void testPorterDuff() {
    LinearGradient blueGradient =
        new LinearGradient(0, 0, SIZE, 0, Color.GREEN, Color.BLUE, Shader.TileMode.CLAMP);
    LinearGradient redGradient =
        new LinearGradient(0, 0, 0, SIZE, Color.GREEN, Color.RED, Shader.TileMode.CLAMP);
    ComposeShader shader = new ComposeShader(blueGradient, redGradient, PorterDuff.Mode.SCREEN);

    Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setShader(shader);
    canvas.drawPaint(paint);

    for (int y = 0; y < SIZE; y++) {
      for (int x = 0; x < SIZE; x++) {
        float greenX = 1f - (x / 255f);
        float greenY = 1f - (y / 255f);
        int green = (int) ((greenX + greenY - greenX * greenY) * 255);
        int pixel = bitmap.getPixel(x, y);
        try {
          assertEquals(0xFF, Color.alpha(pixel), TOLERANCE);
          assertEquals(y, Color.red(pixel), TOLERANCE);
          assertEquals(green, Color.green(pixel), TOLERANCE);
          assertEquals(x, Color.blue(pixel), TOLERANCE);
        } catch (Error e) {
          Log.w(getClass().getName(), "Failed at (" + x + "," + y + ")");
          throw e;
        }
      }
    }
  }

  @Test
  @Config(minSdk = Q) // This did not exist until Q
  public void testBlendMode() {
    LinearGradient blueGradient =
        new LinearGradient(0, 0, SIZE, 0, Color.GREEN, Color.BLUE, Shader.TileMode.CLAMP);
    LinearGradient redGradient =
        new LinearGradient(0, 0, 0, SIZE, Color.GREEN, Color.RED, Shader.TileMode.CLAMP);
    ComposeShader shader = new ComposeShader(blueGradient, redGradient, BlendMode.SCREEN);

    Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setShader(shader);
    canvas.drawPaint(paint);

    for (int y = 0; y < SIZE; y++) {
      for (int x = 0; x < SIZE; x++) {
        float greenX = 1f - (x / 255f);
        float greenY = 1f - (y / 255f);
        int green = (int) ((greenX + greenY - greenX * greenY) * 255);
        int pixel = bitmap.getPixel(x, y);
        try {
          assertEquals(0xFF, Color.alpha(pixel), TOLERANCE);
          assertEquals(y, Color.red(pixel), TOLERANCE);
          assertEquals(green, Color.green(pixel), TOLERANCE);
          assertEquals(x, Color.blue(pixel), TOLERANCE);
        } catch (Error e) {
          Log.w(getClass().getName(), "Failed at (" + x + "," + y + ")");
          throw e;
        }
      }
    }
  }

  @Test
  public void testXfermode() {
    Bitmap redBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    redBitmap.eraseColor(Color.RED);
    Bitmap cyanBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    cyanBitmap.eraseColor(Color.CYAN);

    BitmapShader redShader = new BitmapShader(redBitmap, TileMode.CLAMP, TileMode.CLAMP);
    BitmapShader cyanShader = new BitmapShader(cyanBitmap, TileMode.CLAMP, TileMode.CLAMP);

    PorterDuffXfermode xferMode = new PorterDuffXfermode(PorterDuff.Mode.ADD);

    ComposeShader shader = new ComposeShader(redShader, cyanShader, xferMode);

    Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setShader(shader);
    canvas.drawPaint(paint);

    // red + cyan = white
    assertEquals(Color.WHITE, bitmap.getPixel(0, 0));
  }

  @Test
  public void testChildLocalMatrix() {
    Matrix translate1x1 = new Matrix();
    translate1x1.setTranslate(1, 1);
    Matrix translate0x1 = new Matrix();
    translate0x1.setTranslate(0, 1);
    Matrix translate1x0 = new Matrix();
    translate1x0.setTranslate(1, 0);

    Bitmap redBitmap = Bitmap.createBitmap(3, 3, Bitmap.Config.ARGB_8888);
    redBitmap.setPixel(1, 1, Color.RED);
    BitmapShader redShader = new BitmapShader(redBitmap, TileMode.CLAMP, TileMode.CLAMP);

    Bitmap cyanBitmap = Bitmap.createBitmap(3, 3, Bitmap.Config.ARGB_8888);
    cyanBitmap.setPixel(1, 1, Color.CYAN);
    BitmapShader cyanShader = new BitmapShader(cyanBitmap, TileMode.CLAMP, TileMode.CLAMP);

    ComposeShader composeShader = new ComposeShader(redShader, cyanShader, PorterDuff.Mode.ADD);

    Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setShader(composeShader);

    // initial state, white pixel from red and cyan overlap
    bitmap.eraseColor(Color.TRANSPARENT);
    canvas.drawPaint(paint);
    assertEquals(Color.WHITE, bitmap.getPixel(1, 1));

    // offset right+down from inner shaders
    redShader.setLocalMatrix(translate1x1);
    cyanShader.setLocalMatrix(translate1x1);
    bitmap.eraseColor(Color.TRANSPARENT);
    canvas.drawPaint(paint);
    assertEquals(Color.WHITE, bitmap.getPixel(2, 2));

    // offset right+down from outer shader
    redShader.setLocalMatrix(null);
    cyanShader.setLocalMatrix(null);
    composeShader.setLocalMatrix(translate1x1);
    bitmap.eraseColor(Color.TRANSPARENT);
    canvas.drawPaint(paint);
    assertEquals(Color.WHITE, bitmap.getPixel(2, 2));

    // combine matrices from both levels
    redShader.setLocalMatrix(translate0x1);
    cyanShader.setLocalMatrix(null);
    composeShader.setLocalMatrix(translate1x0);
    bitmap.eraseColor(Color.TRANSPARENT);
    canvas.drawPaint(paint);
    assertEquals(Color.RED, bitmap.getPixel(2, 2));
    assertEquals(Color.CYAN, bitmap.getPixel(2, 1));
  }
}
