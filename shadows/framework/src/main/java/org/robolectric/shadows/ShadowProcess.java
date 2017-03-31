package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Shadow for {@link android.os.Process}.
 */
@Implements(android.os.Process.class)
public class ShadowProcess {
  private static int pid;

  @Implementation
  public static final int myPid() {
    return pid;
  }
  
  public static void setPid(int pid) {
    ShadowProcess.pid = pid;
  }
  
  @Resetter
  public static void reset() {
    ShadowProcess.pid = 0;
  }
}