package org.robolectric.shadows;

import android.os.StrictMode;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.os.StrictMode}.
 */
@Implements(StrictMode.class)
public class ShadowStrictMode {

  @Implementation
  public static void setVmPolicy(StrictMode.VmPolicy p) {
    // Prevent Robolectric from calling through
  }
}
