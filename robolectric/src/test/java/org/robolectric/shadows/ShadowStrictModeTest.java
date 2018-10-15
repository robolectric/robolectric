package org.robolectric.shadows;

import android.os.StrictMode;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowStrictModeTest {
  @Test
  public void setVmPolicyTest() {
    StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder().build();
    StrictMode.setVmPolicy(policy); // should not result in an exception
  }
}