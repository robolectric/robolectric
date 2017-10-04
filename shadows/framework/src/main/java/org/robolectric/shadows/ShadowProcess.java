package org.robolectric.shadows;

import java.util.concurrent.ThreadLocalRandom;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(android.os.Process.class)
public class ShadowProcess {
  private static int pid;
  private static int uid = getRandomApplicationUid();

  @Implementation
  public static final int myPid() {
    return pid;
  }

  /**
   * Returns the identifier of this process's uid. Unlike Android UIDs are randomly initialized to prevent
   * tests from depending on any given value. Tests should access the current process UID via
   * {@link android.os.Process#myUid()}.
   */
  @Implementation
  public static final int myUid() {
    return uid;
  }

  /**
   * Sets the identifier of this process.
   */
  public static void setPid(int pid) {
    ShadowProcess.pid = pid;
  }

  @Resetter
  public static void reset() {
    ShadowProcess.pid = 0;
    // We cannot re-randomize uid, because it would break code that statically depends on
    // android.os.Process.myUid(), which persists between tests.
  }

  private static int getRandomApplicationUid() {
    // UIDs are randomly initialized to prevent tests from depending on any given value. Tests
    // should access the current process UID via android.os.Process::myUid().
    return ThreadLocalRandom.current()
        .nextInt(
            android.os.Process.FIRST_APPLICATION_UID, android.os.Process.LAST_APPLICATION_UID + 1);
  }
}
