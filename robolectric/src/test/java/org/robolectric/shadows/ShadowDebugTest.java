package org.robolectric.shadows;

import android.os.Debug;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowDebugTest {
  @Test
  public void initNoCrash() {
    assertThat(Debug.getNativeHeapAllocatedSize()).isNotNegative();
  }
}
