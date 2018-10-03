package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;

import android.os.SystemClock;
import java.time.DateTimeException;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Robolectric's concept of current time is base on the current time of the UI Scheduler for
 * consistency with previous implementations. This is not ideal, since both schedulers
 * (background and foreground), can see different values for the current time.
 */
@Implements(SystemClock.class)
public class ShadowSystemClock {
  private static long bootedAt = 0;
  private static long nanoTime = 0;
  private static final int MILLIS_PER_NANO = 1000000;
  private static boolean networkTimeAvailable = true;

  static long now() {
    if (ShadowApplication.getInstance() == null) {
      return 0;
    }
    return ShadowApplication.getInstance().getForegroundThreadScheduler().getCurrentTime();
  }

  @Implementation
  protected static void sleep(long millis) {
    if (ShadowApplication.getInstance() == null) {
      return;
    }

    nanoTime = millis * MILLIS_PER_NANO;
    ShadowApplication.getInstance().getForegroundThreadScheduler().advanceBy(millis);
  }

  @Implementation
  protected static boolean setCurrentTimeMillis(long millis) {
    if (ShadowApplication.getInstance() == null) {
      return false;
    }

    if (now() > millis) {
      return false;
    }
    nanoTime = millis * MILLIS_PER_NANO;
    ShadowApplication.getInstance().getForegroundThreadScheduler().advanceTo(millis);
    return true;
  }

  @Implementation
  protected static long uptimeMillis() {
    return now() - bootedAt;
  }

  @Implementation
  protected static long elapsedRealtime() {
    return uptimeMillis();
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected static long elapsedRealtimeNanos() {
    return elapsedRealtime() * MILLIS_PER_NANO;
  }

  @Implementation
  protected static long currentThreadTimeMillis() {
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

  @Implementation(minSdk = P)
  @HiddenApi
  protected static long currentNetworkTimeMillis() {
    if (networkTimeAvailable) {
      return currentTimeMillis();
    } else {
      throw new DateTimeException("Network time not available");
    }
  }

  /** Sets whether network time is available. */
  public static void setNetworkTimeAvailable(boolean available) {
    networkTimeAvailable = available;
  }

  @Resetter
  public static void reset() {
    networkTimeAvailable = true;
  }
}
