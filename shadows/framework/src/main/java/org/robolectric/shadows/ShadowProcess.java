package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(android.os.Process.class)
public class ShadowProcess {
  private static int pid;

  @Implementation
  public static final int myPid() {
    return pid;
  }

  @Implementation
  public static final int myUid() {
    return android.os.Process.FIRST_APPLICATION_UID;
  }

  public static void setPid(int pid) {
    ShadowProcess.pid = pid;
  }

  @Resetter
  public static void reset() {
    ShadowProcess.pid = 0;
  }
}
