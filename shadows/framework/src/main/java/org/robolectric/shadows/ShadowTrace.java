package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

import android.os.Trace;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Shadow implementation for {@link Trace}, which stores the traces locally in arrays (unlike the
 * real implementation) and allows reading them.
 */
@Implements(Trace.class)
public class ShadowTrace {

  private static final Deque<String> currentSections = new ArrayDeque<>();
  private static final Queue<String> previousSections = new ArrayDeque<>();

  /** Starts a new trace section with given name. */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static void beginSection(String sectionName) {
    currentSections.addFirst(sectionName);
  }

  /**
   * Ends the most recent active trace section.
   *
   * @throws {@link AssertionError} if called without any active trace section.
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static void endSection() {
    if (currentSections.isEmpty()) {
      throw new IllegalStateException("Trying to end a trace section that was never started");
    }

    previousSections.offer(currentSections.removeFirst());
  }

  /** Returns a stack of the currently active trace sections. */
  public static Deque<String> getCurrentSections() {
    return new ArrayDeque<>(currentSections);
  }

  /** Returns a queue of all the previously active trace sections. */
  public static Queue<String> getPreviousSections() {
    return new ArrayDeque<>(previousSections);
  }

  /** Resets internal lists of active trace sections. */
  @Resetter
  public static void reset() {
    currentSections.clear();
    previousSections.clear();
  }
}
