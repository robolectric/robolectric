package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.graphics.animation.NativeInterpolatorFactory;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = R)
public class ShadowNativeNativeInterpolatorFactoryTest {
  @Test
  public void testAllFunctionsLinkedProperly() {
    NativeInterpolatorFactory.createAccelerateDecelerateInterpolator();
    NativeInterpolatorFactory.createAccelerateInterpolator(0);
    NativeInterpolatorFactory.createAnticipateInterpolator(0);
    NativeInterpolatorFactory.createAnticipateOvershootInterpolator(0);
    NativeInterpolatorFactory.createBounceInterpolator();
    NativeInterpolatorFactory.createCycleInterpolator(0);
    NativeInterpolatorFactory.createDecelerateInterpolator(0);
    NativeInterpolatorFactory.createLinearInterpolator();
    NativeInterpolatorFactory.createOvershootInterpolator(0);
    NativeInterpolatorFactory.createPathInterpolator(new float[] {0}, new float[] {0});
    NativeInterpolatorFactory.createLutInterpolator(new float[] {0});
  }
}
