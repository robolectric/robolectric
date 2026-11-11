package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Loading the native runtime copies its data assets - the fonts, the hyphen data and the ICU data -
 * out of the archives that supply them, and those assets are shared between loads. They do not all
 * come from the same archive: the fonts come from the nativeruntime dist archive whatever the SDK
 * under test, while from {@link VANILLA_ICE_CREAM} the ICU data and the hyphen data come from that
 * SDK's android-all archive, and the ICU data file is named after its ICU version. Sharing all of
 * them through one directory therefore left every load after the first pointing icu.data.path at a
 * file that had never been copied, which fails the library load with U_ILLEGAL_ARGUMENT_ERROR.
 *
 * <p>Running at several SDK levels is what catches this: each gets its own sandbox, and so its own
 * load, within the one JVM.
 */
@RunWith(RobolectricTestRunner.class)
public class NativeRuntimeAssetsTest {

  @Test
  @Config(sdk = {O, UPSIDE_DOWN_CAKE, VANILLA_ICE_CREAM, Config.NEWEST_SDK})
  public void nativeRuntime_drawsText_atEverySdkLevel() {
    Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    canvas.drawColor(Color.WHITE);

    Paint paint = new Paint();
    paint.setColor(Color.BLACK);
    paint.setTextSize(30);
    canvas.drawText("R", 10, 35, paint);

    // Text rendering needs the fonts and the ICU data, so it only produces ink if both were made
    // available at the paths the runtime was pointed at.
    assertThat(hasNonWhitePixel(bitmap)).isTrue();
  }

  private static boolean hasNonWhitePixel(Bitmap bitmap) {
    for (int x = 0; x < bitmap.getWidth(); x++) {
      for (int y = 0; y < bitmap.getHeight(); y++) {
        if (bitmap.getPixel(x, y) != Color.WHITE) {
          return true;
        }
      }
    }
    return false;
  }
}
