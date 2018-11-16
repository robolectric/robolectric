package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.os.Trace;
import android.util.Log;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow implementation for {@link Trace}, which stores the traces locally in arrays (unlike the
 * real implementation) and allows reading them.
 *
 * <p>The shadow doesn't enforce the constrains by default (e.g., null section names, or incorrect
 * {@link ShadowTrace.beginSection(String)} / {@link ShadowTrace.endSection()} sequences), but can
 * be configured to do so by calling {@link ShadowTrace.setCrashOnIncorrectUsage(boolean)}.
 */
@Implements(Trace.class)
public class ShadowTrace {
  private static final String TAG = "ShadowTrace";

  @GuardedBy("lock")
  private static final Deque<String> currentSections = new ArrayDeque<>();

  @GuardedBy("lock")
  private static final Queue<String> previousSections = new ArrayDeque<>();

  private static final boolean CRASH_ON_INCORRECT_USAGE_DEFAULT = true;
  private static boolean crashOnIncorrectUsage = CRASH_ON_INCORRECT_USAGE_DEFAULT;
  private static boolean appTracingAllowed = true;
  private static final Object lock = new Object();

  private static final long TRACE_TAG_APP = 1L << 12;
  private static final int MAX_SECTION_NAME_LEN = 127;

  /** Starts a new trace section with given name. */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static void beginSection(String sectionName) {
    if (Trace.isTagEnabled(TRACE_TAG_APP)) {
      if (crashOnIncorrectUsage) {
        if (sectionName.length() > MAX_SECTION_NAME_LEN) {
          throw new IllegalArgumentException("sectionName is too long");
        }
      } else if (sectionName == null) {
        Log.w(TAG, "Section name cannot be null");
        return;
      } else if (sectionName.length() > MAX_SECTION_NAME_LEN) {
        Log.w(TAG, "Section name is too long");
        return;
      }

      synchronized (lock) {
        currentSections.addFirst(sectionName);
      }
    }
  }

  /**
   * Ends the most recent active trace section.
   *
   * @throws {@link AssertionError} if called without any active trace section.
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static void endSection() {
    if (Trace.isTagEnabled(TRACE_TAG_APP)) {
      synchronized (lock) {
        if (currentSections.isEmpty()) {
          Log.e(TAG, "Trying to end a trace section that was never started");
          return;
        }

        previousSections.offer(currentSections.removeFirst());
      }
    }
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static boolean isTagEnabled(long traceTag) {
    if (traceTag == TRACE_TAG_APP) {
      return appTracingAllowed;
    }

    return directlyOn(Trace.class, "isTagEnabled", ClassParameter.from(long.class, traceTag));
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static void setAppTracingAllowed(boolean appTracingAllowed) {
    ShadowTrace.appTracingAllowed = appTracingAllowed;
  }

  /** Returns a stack of the currently active trace sections. */
  public static Deque<String> getCurrentSections() {
    synchronized (lock) {
      return new ArrayDeque<>(currentSections);
    }
  }

  /** Returns a queue of all the previously active trace sections. */
  public static Queue<String> getPreviousSections() {
    synchronized (lock) {
      return new ArrayDeque<>(previousSections);
    }
  }

  /**
   * Do not use this method unless absolutely necessary. Prefer fixing the tests instead.
   *
   * <p>Sets whether to crash on incorrect usage (e.g., calling {@link #endSection()} before {@link
   * beginSection(String)}. Default value - {@code false}.
   */
  public static void doNotUseSetCrashOnIncorrectUsage(boolean crashOnIncorrectUsage) {
    ShadowTrace.crashOnIncorrectUsage = crashOnIncorrectUsage;
  }

  /** Resets internal lists of active trace sections. */
  @Resetter
  public static void reset() {
    synchronized (lock) {
      currentSections.clear();
      previousSections.clear();
    }
    crashOnIncorrectUsage = CRASH_ON_INCORRECT_USAGE_DEFAULT;
  }
}
