package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(android.os.Process.class)
public class ShadowProcess {
  private static int pid;
  private static int uid = getRandomApplicationUid();
  private static int tid = getRandomApplicationUid();
  private static final Object threadPrioritiesLock = new Object();

  @GuardedBy("threadPrioritiesLock")
  private static final Map<Integer, Integer> threadPriorities = new HashMap<Integer, Integer>();

  @Implementation
  protected static final int myPid() {
    return pid;
  }

  /**
   * Returns the identifier of this process's uid. Unlike Android UIDs are randomly initialized to
   * prevent tests from depending on any given value. Tests should access the current process UID
   * via {@link android.os.Process#myUid()}. You can override this value by calling {@link
   * #setUid(int)}.
   */
  @Implementation
  protected static final int myUid() {
    return uid;
  }

  /**
   * Returns the identifier ({@link java.lang.Thread#getId()}) of the current thread ({@link
   * java.lang.Thread#currentThread()}).
   */
  @Implementation
  protected static final int myTid() {
    return (int) Thread.currentThread().getId();
  }

  /**
   * Stores priority for the current thread, but doesn't actually change it to not mess up with test
   * runner. Unlike real implementation does not throw any exceptions.
   */
  @Implementation
  protected static final void setThreadPriority(int priority) {
    synchronized (threadPrioritiesLock) {
      threadPriorities.put(ShadowProcess.myTid(), priority);
    }
  }

  /**
   * Stores priority for the given thread, but doesn't actually change it to not mess up with test
   * runner. Unlike real implementation does not throw any exceptions.
   *
   * @param tid The identifier of the thread. If equals zero, the identifier of the calling thread
   *     will be used.
   */
  @Implementation
  protected static final void setThreadPriority(int tid, int priority) {
    checkArgument(
        priority >= android.os.Process.THREAD_PRIORITY_URGENT_AUDIO
            && priority <= android.os.Process.THREAD_PRIORITY_LOWEST,
        "priority %s out of range. Use a Process.THREAD_PRIORITY_* constant.",
        priority);

    if (tid == 0) {
      tid = ShadowProcess.myTid();
    }
    synchronized (threadPrioritiesLock) {
      threadPriorities.put(tid, priority);
    }
  }

  /**
   * Returns priority stored for the given thread.
   *
   * @param tid The identifier of the thread. If equals zero, the identifier of the calling thread
   *     will be used.
   */
  @Implementation
  protected static final int getThreadPriority(int tid) {
    if (tid == 0) {
      tid = ShadowProcess.myTid();
    }
    synchronized (threadPrioritiesLock) {
      return threadPriorities.getOrDefault(tid, 0);
    }
  }

  /**
   * Sets the identifier of this process.
   */
  public static void setUid(int uid) {
    ShadowProcess.uid = uid;
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
    synchronized (threadPrioritiesLock) {
      threadPriorities.clear();
    }
    // We cannot re-randomize uid, because it would break code that statically depends on
    // android.os.Process.myUid(), which persists between tests.
  }

  static int getRandomApplicationUid() {
    // UIDs are randomly initialized to prevent tests from depending on any given value. Tests
    // should access the current process UID via android.os.Process::myUid().
    return ThreadLocalRandom.current()
        .nextInt(
            android.os.Process.FIRST_APPLICATION_UID, android.os.Process.LAST_APPLICATION_UID + 1);
  }
}
