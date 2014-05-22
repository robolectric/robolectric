package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadows the {@code android.os.Process} class.
 */
@Implements(value = android.os.Process.class, resetStaticState = true)
public class ShadowProcess {
  private static int pid;

  @Implementation
  public static final int myPid() {
    return pid;
  }
  
  public static void setPid(int pid) {
    ShadowProcess.pid = pid;
  }
  
  public static void reset() {
    ShadowProcess.pid = 0;
  }
}