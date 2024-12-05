package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Shader;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeShaderTest {
  @SuppressWarnings("CheckReturnValue")
  @Test
  public void testConstructor() {
    new Shader();
  }

  @Test
  public void testAccessLocalMatrix() {
    int width = 80;
    int height = 120;
    int[] color = new int[width * height];
    Bitmap bitmap = Bitmap.createBitmap(color, width, height, Bitmap.Config.RGB_565);

    Shader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    Matrix m = new Matrix();

    shader.setLocalMatrix(m);
    assertFalse(shader.getLocalMatrix(m));

    shader.setLocalMatrix(null);
    assertFalse(shader.getLocalMatrix(m));
  }

  @Test
  public void testMutateBaseObject() {
    Shader shader = new Shader();
    shader.setLocalMatrix(null);
  }

  @Test
  @Config(minSdk = Q) // Cannot erase color Pre-Q
  public void testGetSetLocalMatrix() {
    Matrix skew10x20 = new Matrix();
    skew10x20.setSkew(10, 20);

    Matrix scale2x3 = new Matrix();
    scale2x3.setScale(2, 3);

    // setup shader
    Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
    bitmap.eraseColor(Color.BLUE);
    Shader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

    // get null
    shader.setLocalMatrix(null);
    Matrix paramMatrix = new Matrix(skew10x20);
    assertFalse("shader should have no matrix set", shader.getLocalMatrix(paramMatrix));
    assertEquals("matrix param not modified when no matrix set", skew10x20, paramMatrix);

    // get nonnull
    shader.setLocalMatrix(scale2x3);
    assertTrue("shader should have matrix set", shader.getLocalMatrix(paramMatrix));
    assertEquals("param matrix should be updated", scale2x3, paramMatrix);
  }

  @Test
  public void testGetWithNullParam() {
    Shader shader = new Shader();
    Matrix matrix = new Matrix();
    matrix.setScale(10, 10);
    shader.setLocalMatrix(matrix);

    assertThrows(NullPointerException.class, () -> shader.getLocalMatrix(null));
  }
}
