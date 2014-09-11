package org.robolectric.shadows;

import android.os.StrictMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

/**
 * Created by karlicos on 11.09.14.
 */
@RunWith(TestRunners.WithDefaults.class)
public class ShadowStrictModeTest {
  @Test
  public void setVmPolicyTest() {
    StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder().build();
    StrictMode.setVmPolicy(policy); // should not result in an exception
  }
}