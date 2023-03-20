package org.robolectric.integrationtests.nativegraphics;

import static android.os.Build.VERSION_CODES.Q;

import android.graphics.HardwareRenderer;
import android.view.Choreographer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(minSdk = Q)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeHardwareRendererTest {

  @Test
  public void test_hardwareRenderer() {
    HardwareRenderer unused = new HardwareRenderer();
  }

  @Test
  public void choreographer_firstCalled() {
    // In some SDK levels, the Choreographer constructor ends up calling
    // HardwareRenderer.nHackySetRTAnimationsEnabled. Ensure that RNG is loaded if this happens.
    var unused = Choreographer.getInstance();
  }
}
