package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.graphics.Bitmap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(minSdk = O)
@RunWith(RobolectricTestRunner.class)
public class BitmapLoadingTest {

  /**
   * There is a potential race that happens if Bitmap classes are loaded by multiple threads. The
   * race is due to the lazy initialization of ColorSpace class members in hwui's jni/Graphics.cpp.
   */
  @Test
  public void bitmapLoading_backgroundThreads_doesNotRace() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    for (int i = 0; i < 10; i++) {
      executorService.execute(
          () -> {
            Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            // createScaledBitmap triggers lazy ColorSpace member initialization in hwui
            // Graphics.cpp.
            Bitmap.createScaledBitmap(bitmap, 200, 200, false);
          });
    }
    executorService.shutdown();
    executorService.awaitTermination(10, SECONDS);
  }
}
