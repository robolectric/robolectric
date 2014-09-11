package org.robolectric.shadows;

import android.app.Service;
import android.os.StrictMode;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by karlicos on 11.09.14.
 */
@Implements(StrictMode.class)
public class ShadowStrictMode {
  @Implementation
  public static void setVmPolicy(StrictMode.VmPolicy p) {
    // Just ignore VM policy setting.
    // Results in a NPE otherwise.
  }
}
