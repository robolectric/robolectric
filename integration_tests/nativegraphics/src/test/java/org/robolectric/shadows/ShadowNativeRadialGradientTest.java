package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeRadialGradientTest {
  @Test
  public void testZeroScaleMatrix() {
    RadialGradient gradient =
        new RadialGradient(0.5f, 0.5f, 1, Color.RED, Color.BLUE, TileMode.CLAMP);

    Matrix m = new Matrix();
    m.setScale(0, 0);
    gradient.setLocalMatrix(m);

    Bitmap bitmap = Bitmap.createBitmap(3, 1, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.BLACK);
    Canvas canvas = new Canvas(bitmap);

    Paint paint = new Paint();
    paint.setShader(gradient);
    canvas.drawPaint(paint);

    ColorUtils.verifyColor(Color.BLACK, bitmap.getPixel(0, 0), 1);
    ColorUtils.verifyColor(Color.BLACK, bitmap.getPixel(1, 0), 1);
    ColorUtils.verifyColor(Color.BLACK, bitmap.getPixel(2, 0), 1);
  }

  @Test
  @Config(minSdk = Q)
  public void testColorLong() {
    ColorSpace p3 = ColorSpace.get(ColorSpace.Named.DISPLAY_P3);
    long red = Color.pack(1, 0, 0, 1, p3);
    long blue = Color.pack(0, 0, 1, 1, p3);
    RadialGradient gradient = new RadialGradient(50, 50, 25, red, blue, TileMode.CLAMP);

    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGBA_F16);
    bitmap.eraseColor(Color.TRANSPARENT);
    Canvas canvas = new Canvas(bitmap);

    Paint paint = new Paint();
    paint.setShader(gradient);
    canvas.drawPaint(paint);

    final ColorSpace bitmapColorSpace = bitmap.getColorSpace();
    Function<Long, Color> convert =
        (l) -> {
          return Color.valueOf(Color.convert(l, bitmapColorSpace));
        };

    final Color centerColor = bitmap.getColor(50, 50);
    ColorUtils.verifyColor("Center color should be red!", convert.apply(red), centerColor, 0.034f);
    Color blueColor = convert.apply(blue);
    for (Point p :
        new Point[] {
          new Point(0, 0),
          new Point(50, 0),
          new Point(99, 0),
          new Point(0, 50),
          new Point(0, 99),
          new Point(99, 0),
          new Point(99, 50),
          new Point(99, 99)
        }) {
      ColorUtils.verifyColor(
          "Edge point " + p + " should be blue", blueColor, bitmap.getColor(p.x, p.y), .001f);
    }

    final double[] negativeOneAndOne = new double[] {-1, 1};
    Color lastColor = centerColor;
    Point lastPoint = new Point(0, 0);
    // On several different radii, verify that colors trend from red to blue.
    for (double radius = 4; radius < 25; radius += 4) {
      // These correspond to the first point we check at a given radius.
      Color currentColor = null;
      Point currentPoint = null;
      for (double angle = 0; angle <= Math.PI / 2.0; angle += Math.PI / 8.0) {
        double dx = Math.cos(angle) * radius;
        double dy = Math.sin(angle) * radius;
        for (double nx : negativeOneAndOne) {
          for (double ny : negativeOneAndOne) {
            int x = 50 + (int) (nx * dx);
            int y = 50 + (int) (ny * dy);
            Color c = bitmap.getColor(x, y);
            if (currentColor == null) {
              currentColor = c;
              currentPoint = new Point(x, y);
              assertTrue(
                  "Outer "
                      + currentPoint
                      + " ("
                      + currentColor
                      + ") should be less red than inner "
                      + lastPoint
                      + " ("
                      + lastColor
                      + ")",
                  currentColor.red() < lastColor.red());
              assertTrue(
                  "Outer "
                      + currentPoint
                      + " ("
                      + currentColor
                      + ") should be more blue than inner "
                      + lastPoint
                      + " ("
                      + lastColor
                      + ")",
                  currentColor.blue() > lastColor.blue());
            } else {
              ColorUtils.verifyColor(
                  "Point(" + x + ", " + y + ") should match " + currentPoint,
                  currentColor,
                  c,
                  .08f);
            }
          }
        }
      }

      lastColor = currentColor;
      lastPoint = currentPoint;
    }
  }

  @Test
  public void testNullColorInts() {
    int[] colors = null;
    assertThrows(
        NullPointerException.class,
        () -> {
          RadialGradient unused = new RadialGradient(0.5f, 0.5f, 1, colors, null, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testNullColorLongs() {
    long[] colors = null;
    assertThrows(
        NullPointerException.class,
        () -> {
          RadialGradient unused = new RadialGradient(0.5f, 0.5f, 1, colors, null, TileMode.CLAMP);
        });
  }

  @Test
  public void testNoColorInts() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(0.5f, 0.5f, 1, new int[0], null, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testNoColorLongs() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(0.5f, 0.5f, 1, new long[0], null, TileMode.CLAMP);
        });
  }

  @Test
  public void testOneColorInts() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(0.5f, 0.5f, 1, new int[1], null, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testOneColorLongs() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(0.5f, 0.5f, 1, new long[1], null, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testMismatchColorLongs() {
    long[] colors = new long[2];
    colors[0] = Color.pack(Color.BLUE);
    colors[1] = Color.pack(.5f, .5f, .5f, 1.0f, ColorSpace.get(ColorSpace.Named.DISPLAY_P3));
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused = new RadialGradient(0.5f, 0.5f, 1, colors, null, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testMismatchColorLongs2() {
    long color0 = Color.pack(Color.BLUE);
    long color1 = Color.pack(.5f, .5f, .5f, 1.0f, ColorSpace.get(ColorSpace.Named.DISPLAY_P3));
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused = new RadialGradient(0.5f, 0.5f, 1, color0, color1, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testMismatchPositionsInts() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(0.5f, 0.5f, 1, new int[2], new float[3], TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testMismatchPositionsLongs() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(0.5f, 0.5f, 1, new long[2], new float[3], TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testInvalidColorLongs() {
    long[] colors = new long[2];
    colors[0] = -1L;
    colors[0] = -2L;
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused = new RadialGradient(0.5f, 0.5f, 1, colors, null, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testInvalidColorLong() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(0.5f, 0.5f, 1, -1L, Color.pack(Color.RED), TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testInvalidColorLong2() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(0.5f, 0.5f, 1, Color.pack(Color.RED), -1L, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testZeroRadius() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(0.5f, 0.5f, 0, Color.RED, Color.BLUE, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testZeroRadiusArray() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(
                  0.5f, 0.5f, 0, new int[] {Color.RED, Color.BLUE}, null, TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testZeroRadiusLong() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(
                  0.5f, 0.5f, 0, Color.pack(Color.RED), Color.pack(Color.BLUE), TileMode.CLAMP);
        });
  }

  @Test
  @Config(minSdk = Q)
  public void testZeroRadiusLongArray() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RadialGradient unused =
              new RadialGradient(
                  0.5f,
                  0.5f,
                  0,
                  new long[] {Color.pack(Color.RED), Color.pack(Color.BLUE)},
                  null,
                  TileMode.CLAMP);
        });
  }
}
