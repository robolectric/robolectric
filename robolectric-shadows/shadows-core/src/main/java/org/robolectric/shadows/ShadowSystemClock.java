package org.robolectric.shadows;

import android.os.SystemClock;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Resetter;

/**
 * Shadow for {@link android.os.SystemClock}.
 *
 * <p>The concept of current time is base on the current time of the UI Scheduler for
 * consistency with previous implementations. This is not ideal, since both schedulers
 * (background and foreground), can see different values for the current time.</p>
 */
@Implements(SystemClock.class)
public class ShadowSystemClock {
  private static long bootedAt = 0;
  private static long nanoTime = 0;
  private static long currentTime = 100;
  private static final int MILLIS_PER_NANO = 1000000;

  @Resetter
  public static void reset() {
    bootedAt = 0;
    nanoTime = 0;
    currentTime = 100;
  }

  synchronized public static void advanceToWithNoSideEffects(long millis) {
    if (currentTime < millis) {
      currentTime = millis;
    }
  }

  synchronized static long now() {
    return currentTime;
  }

  /* should this make all schedulers do stuff? */
  @Implementation
  public static void sleep(long millis) {
    if (ShadowApplication.getInstance() == null) {
      return;
    }

    nanoTime = millis * MILLIS_PER_NANO;
    ShadowApplication.getInstance().getForegroundThreadScheduler().advanceBy(millis);
  }

  @Implementation
  public static boolean setCurrentTimeMillis(long millis) {
    if (now() > millis) {
      return false;
    }

    advanceToWithNoSideEffects(millis);

    nanoTime = millis * MILLIS_PER_NANO;
    return true;
  }

  @Implementation
  public static long uptimeMillis() {
    return now() - bootedAt;
  }

  @Implementation
  public static long elapsedRealtime() {
    return uptimeMillis();
  }

  @Implementation
  public static long currentThreadTimeMillis() {
    return uptimeMillis();
  }

  @HiddenApi
  @Implementation
  public static long currentThreadTimeMicro() {
    return uptimeMillis() * 1000;
  }

  @HiddenApi
  @Implementation
  public static long currentTimeMicro() {
    return now() * 1000;
  }

  /**
   * Implements {@link System#currentTimeMillis} through ShadowWrangler.
   *
   * @return Current time in millis.
   */
  @SuppressWarnings("unused")
  public static long currentTimeMillis() {
    return nanoTime / MILLIS_PER_NANO;
  }

  /**
   * Implements {@link System#nanoTime} through ShadowWrangler.
   *
   * @return Current time with nanos.
   */
  @SuppressWarnings("unused")
  public static long nanoTime() {
    return nanoTime;
  }

  public static void setNanoTime(long nanoTime) {
    ShadowSystemClock.nanoTime = nanoTime;
  }
}
