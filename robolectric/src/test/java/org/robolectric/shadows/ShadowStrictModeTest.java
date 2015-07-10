package org.robolectric.shadows;

import android.os.StrictMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowStrictModeTest {
  @Test
  public void setVmPolicyTest() {
    StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder().build();
    StrictMode.setVmPolicy(policy); // should not result in an exception
  }
}