package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.base.MoreObjects;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.bitmapverifiers.ColorVerifier;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeColorFilterTest {
  @Test
  public void testColorMatrix() throws Exception {
    int width = 90;
    int height = 90;
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.WHITE);

    // White, full opacity
    assertThat(Integer.toHexString(bitmap.getPixel(0, 0))).isEqualTo("ffffffff");

    Canvas canvas = new Canvas(bitmap);

    Paint paint = new Paint();
    paint.setTypeface(Typeface.DEFAULT);
    paint.setAntiAlias(true);
    paint.setColorFilter(
        new ColorMatrixColorFilter(
            new ColorMatrix(
                new float[] {
                  -1, 0, 0, 0, 255,
                  0, -1, 0, 0, 255,
                  0, 0, -1, 0, 255,
                  0, 0, 0, 1, 0
                })));

    canvas.drawBitmap(bitmap, 0, 0, paint);

    // Black, full opacity
    assertThat(Integer.toHexString(bitmap.getPixel(0, 0))).isEqualTo("ff000000");

    ColorVerifier colorVerifier = new ColorVerifier(Color.BLACK);

    // Check every pixel is black.
    if (!colorVerifier.verify(bitmap)) {
      Bitmap diff = colorVerifier.getDifferenceBitmap();
      takeScreenshot(diff, "diff.png");
      fail(
          "Bitmap was not the correct color. See diff.png for difference (red shows different"
              + " pixels).");
    }
  }

  private void takeScreenshot(Bitmap bitmap, String name) throws IOException {
    // TEST_UNDECLARED_OUTPUTS_DIR is better in a Bazel environment because the files show up
    // in test artifacts.
    String outputDir =
        MoreObjects.firstNonNull(
            System.getenv("TEST_UNDECLARED_OUTPUTS_DIR"), System.getProperty("java.io.tmpdir"));
    File f = new File(outputDir, "sdk" + VERSION.SDK_INT + "_" + name);
    f.createNewFile();
    f.deleteOnExit();
    bitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(f));
  }
}
