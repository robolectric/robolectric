package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeBitmapShaderTest {
  private static final int TILE_WIDTH = 20;
  private static final int TILE_HEIGHT = 20;
  private static final int BORDER_WIDTH = 5;
  private static final int BORDER_COLOR = Color.BLUE;
  private static final int CENTER_COLOR = Color.RED;
  private static final int NUM_TILES = 4;

  @Test
  public void testBitmapShader() {
    Bitmap tile = Bitmap.createBitmap(TILE_WIDTH, TILE_HEIGHT, Bitmap.Config.ARGB_8888);
    tile.eraseColor(BORDER_COLOR);
    Canvas c = new Canvas(tile);
    Paint p = new Paint();
    p.setColor(CENTER_COLOR);
    c.drawRect(
        BORDER_WIDTH, BORDER_WIDTH, TILE_WIDTH - BORDER_WIDTH, TILE_HEIGHT - BORDER_WIDTH, p);
    BitmapShader shader = new BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    Paint paint = new Paint();
    paint.setShader(shader);
    // create a bitmap that fits (NUM_TILES - 0.5) tiles in both directions
    Bitmap b =
        Bitmap.createBitmap(
            NUM_TILES * TILE_WIDTH - TILE_WIDTH / 2,
            NUM_TILES * TILE_HEIGHT - TILE_HEIGHT / 2,
            Bitmap.Config.ARGB_8888);
    b.eraseColor(Color.BLACK);
    Canvas canvas = new Canvas(b);
    canvas.drawPaint(paint);

    for (int y = 0; y < NUM_TILES; y++) {
      for (int x = 0; x < NUM_TILES; x++) {
        verifyTile(b, x * TILE_WIDTH, y * TILE_HEIGHT);
      }
    }
  }

  /** Check the colors of the tile at the given coordinates in the given bitmap. */
  private void verifyTile(Bitmap bitmap, int tileX, int tileY) {
    for (int y = 0; y < TILE_HEIGHT; y++) {
      for (int x = 0; x < TILE_WIDTH; x++) {
        if (x < BORDER_WIDTH
            || x >= TILE_WIDTH - BORDER_WIDTH
            || y < BORDER_WIDTH
            || y >= TILE_HEIGHT - BORDER_WIDTH) {
          verifyColor(BORDER_COLOR, bitmap, x + tileX, y + tileY);
        } else {
          verifyColor(CENTER_COLOR, bitmap, x + tileX, y + tileY);
        }
      }
    }
  }

  /**
   * Asserts that the pixel at the given coordinates in the given bitmap matches the given color.
   * Simply returns if the coordinates are outside the bitmap area.
   */
  private void verifyColor(int color, Bitmap bitmap, int x, int y) {
    if (x < bitmap.getWidth() && y < bitmap.getHeight()) {
      assertEquals(color, bitmap.getPixel(x, y));
    }
  }

  @Test
  public void testClamp() {
    Bitmap bitmap = Bitmap.createBitmap(2, 1, Bitmap.Config.ARGB_8888);
    bitmap.setPixel(0, 0, Color.RED);
    bitmap.setPixel(1, 0, Color.BLUE);

    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

    Bitmap dstBitmap = Bitmap.createBitmap(4, 1, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(dstBitmap);
    Paint paint = new Paint();
    paint.setShader(shader);
    canvas.drawRect(0, 0, 4, 1, paint);
    canvas.setBitmap(null);

    int[] pixels = new int[4];
    dstBitmap.getPixels(pixels, 0, 4, 0, 0, 4, 1);
    Assert.assertArrayEquals(new int[] {Color.RED, Color.BLUE, Color.BLUE, Color.BLUE}, pixels);
  }

  @Test
  public void testRepeat() {
    Bitmap bitmap = Bitmap.createBitmap(2, 1, Bitmap.Config.ARGB_8888);
    bitmap.setPixel(0, 0, Color.RED);
    bitmap.setPixel(1, 0, Color.BLUE);

    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

    Bitmap dstBitmap = Bitmap.createBitmap(4, 1, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(dstBitmap);
    Paint paint = new Paint();
    paint.setShader(shader);
    canvas.drawRect(0, 0, 4, 1, paint);
    canvas.setBitmap(null);

    int[] pixels = new int[4];
    dstBitmap.getPixels(pixels, 0, 4, 0, 0, 4, 1);
    Assert.assertArrayEquals(new int[] {Color.RED, Color.BLUE, Color.RED, Color.BLUE}, pixels);
  }

  @Test
  public void testMirror() {
    Bitmap bitmap = Bitmap.createBitmap(2, 1, Bitmap.Config.ARGB_8888);
    bitmap.setPixel(0, 0, Color.RED);
    bitmap.setPixel(1, 0, Color.BLUE);

    BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);

    Bitmap dstBitmap = Bitmap.createBitmap(4, 1, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(dstBitmap);
    Paint paint = new Paint();
    paint.setShader(shader);
    canvas.drawRect(0, 0, 4, 1, paint);
    canvas.setBitmap(null);

    int[] pixels = new int[4];
    dstBitmap.getPixels(pixels, 0, 4, 0, 0, 4, 1);
    Assert.assertArrayEquals(new int[] {Color.RED, Color.BLUE, Color.BLUE, Color.RED}, pixels);
  }

  @Test
  public void testNullBitmap() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new BitmapShader(null, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR));
  }
}
