package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public class ShadowDeviceConfigTest {
  @Test
  public void testReset() {
    ShadowDeviceConfig.reset();
  }
}
