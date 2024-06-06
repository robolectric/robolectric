package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.HardwareRendererObserver;
import android.graphics.HardwareRendererObserver.OnFrameMetricsAvailableListener;
import android.os.Handler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Config(sdk = R)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeHardwareRendererObserverTest {

  @Test
  public void test_hardwareRenderer() {
    OnFrameMetricsAvailableListener listener = i -> {};
    if (RuntimeEnvironment.getApiLevel() >= S) {
      HardwareRendererObserver unused =
          new HardwareRendererObserver(listener, new long[0], new Handler(), false);
    } else {
      HardwareRendererObserver unused =
          ReflectionHelpers.callConstructor(
              HardwareRendererObserver.class,
              ClassParameter.from(OnFrameMetricsAvailableListener.class, listener),
              ClassParameter.from(long[].class, new long[0]),
              ClassParameter.from(Handler.class, new Handler()));
    }
  }
}
