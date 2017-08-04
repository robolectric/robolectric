package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Assertions.assertThat;

import android.os.Debug;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowDebugTest {
  @Test
  public void initNoCrash() {
    assertThat(Debug.getNativeHeapAllocatedSize()).isNotNegative();
  }

  @Test
  @Config(minSdk = M)
  public void getRuntimeStats() {
    assertThat(Debug.getRuntimeStats()).isNotNull();
  }
}
