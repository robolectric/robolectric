package org.robolectric.shadows;

import android.os.SystemClock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.HiddenApi;

@Implements(SystemClock.class)
public class ShadowSystemClock {
  private static long bootedAt = 0;
  private static long nanoTime = 0;

  static long now() {
    return Robolectric.getUiThreadScheduler().getCurrentTime();
  }

  @Implementation
  public static void sleep(long ms) {
    Robolectric.getUiThreadScheduler().advanceBy(ms);
  }

  /**
   * The concept of current time is base on the current time
   * of the UI Scheduler for consistency with previous implementations.
   * This is not ideal, since both schedulers (background and foreground),
   * can see different values for the current time.
   */
  @Implementation
  public static boolean setCurrentTimeMillis(long millis) {
    if (now() > millis) {
      return false;
    } 
    Robolectric.getUiThreadScheduler().advanceTo(millis);
    return true;
  }
  
  public static void setNanoTime(long nanoTime) {
    ShadowSystemClock.nanoTime = nanoTime;
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

  @HiddenApi @Implementation
  public static long currentThreadTimeMicro() {
    return uptimeMillis() * 1000;
  }

  @HiddenApi @Implementation
  public static long currentTimeMicro() {
    return now() * 1000;
  }
  
  /**
   * Implements {@link System#currentTimeMillis} through ShadowWrangler.
   */
  @SuppressWarnings("UnusedDeclaration")
  public static long currentTimeMillis() {
    long currTimeMillis = nanoTime / 1000000;
    nanoTime += 1000000;
    return currTimeMillis;
  }

  /**
   * Implements {@link System#nanoTime} through ShadowWrangler.
   */
  @SuppressWarnings("UnusedDeclaration")
  public static long nanoTime() {
    return nanoTime++;
  }
}
