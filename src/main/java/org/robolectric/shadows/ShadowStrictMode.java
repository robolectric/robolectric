package org.robolectric.shadows;

import android.app.Service;
import android.os.StrictMode;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(StrictMode.class)
public class ShadowStrictMode {
  @Implementation
  public static void setVmPolicy(StrictMode.VmPolicy p) {
    // Just ignore VM policy setting.
    // Results in a NPE otherwise.
  }
}
