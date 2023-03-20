package org.robolectric.integrationtests.nativegraphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public final class ShadowNativeAllocationRegistryTest {

  @Test
  public void nativeAllocationRegistryStressTest() {
    for (int i = 0; i < 10_000; i++) {
      Bitmap bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
      bitmap.eraseColor(Color.BLUE);
      if (i % 100 == 0) {
        System.gc();
      }
    }
  }
}
