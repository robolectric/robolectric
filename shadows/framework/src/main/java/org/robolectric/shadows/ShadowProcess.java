package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.base.Preconditions.checkArgument;

import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(android.os.Process.class)
public class ShadowProcess {
  private static int pid;
  private static final int UID = getRandomApplicationUid();
  private static Integer uidOverride;
  private static int tid = getRandomApplicationUid();
  private static final Object threadPrioritiesLock = new Object();
  private static final Object killedProcessesLock = new Object();
  // The range of thread priority values is specified by
  // android.os.Process#setThreadPriority(int, int), which is [-20,19].
  private static final int THREAD_PRIORITY_HIGHEST = -20;
  private static final int THREAD_PRIORITY_LOWEST = 19;

  @GuardedBy("threadPrioritiesLock")
  private static final Map<Integer, Integer> threadPriorities = new HashMap<Integer, Integer>();

  @GuardedBy("killedProcessesLock")
  private static final Set<Integer> killedProcesses = new HashSet<>();

  /**
   * Stores requests for killing processes. Processe that were requested to be killed can be
   * retrieved by calling {@link #wasKilled(int)}. Use {@link #clearKilledProcesses()} to clear the
   * list.
   */
  @Implementation
  protected static final void killProcess(int pid) {
    synchronized (killedProcessesLock) {
      killedProcesses.add(pid);
    }
  }

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
    if (uidOverride != null) {
      return uidOverride;
    }
    return UID;
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
   * @param priority The priority to be set for the thread. The range of values accepted is
   *     specified by {@link android.os.Process#setThreadPriority(int, int)}, which is [-20,19].
   */
  @Implementation
  protected static final void setThreadPriority(int tid, int priority) {
    checkArgument(
        priority >= THREAD_PRIORITY_HIGHEST && priority <= THREAD_PRIORITY_LOWEST,
        "priority %s out of range [%s, %s]. It is recommended to use a Process.THREAD_PRIORITY_*"
            + " constant.",
        priority,
        Integer.toString(THREAD_PRIORITY_HIGHEST),
        Integer.toString(THREAD_PRIORITY_LOWEST));

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

  public static void clearKilledProcesses() {
    synchronized (killedProcessesLock) {
      killedProcesses.clear();
    }
  }

  /**
   * Sets the identifier of this process.
   */
  public static void setUid(int uid) {
    ShadowProcess.uidOverride = uid;
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
    ShadowProcess.clearKilledProcesses();
    synchronized (threadPrioritiesLock) {
      threadPriorities.clear();
    }
    // We cannot re-randomize uid, because it would break code that statically depends on
    // android.os.Process.myUid(), which persists between tests.
    ShadowProcess.uidOverride = null;
    ShadowProcess.processName = "";
  }

  static int getRandomApplicationUid() {
    // UIDs are randomly initialized to prevent tests from depending on any given value. Tests
    // should access the current process UID via android.os.Process::myUid().
    return ThreadLocalRandom.current()
        .nextInt(
            android.os.Process.FIRST_APPLICATION_UID, android.os.Process.LAST_APPLICATION_UID + 1);
  }

  /**
   * Gets an indication of whether or not a process was killed (using {@link #killProcess(int)}).
   */
  public static boolean wasKilled(int pid) {
    synchronized (killedProcessesLock) {
      return killedProcesses.contains(pid);
    }
  }

  private static String processName = "";

  /**
   * Returns the name of the process. You can override this value by calling {@link
   * #setProcessName(String)}.
   *
   * @return process name.
   */
  @Implementation(minSdk = TIRAMISU)
  protected static String myProcessName() {
    return processName;
  }

  /**
   * Sets the process name returned by {@link #myProcessName()}.
   *
   * @param processName New process name to set. Cannot be null.
   */
  public static void setProcessName(@NonNull String processName) {
    ShadowProcess.processName = processName;
  }
}
