package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;

import android.os.SystemClock;
import java.time.DateTimeException;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * A new version of a shadow SystemClock used when {@link ShadowBaseLooper#useRealisticLooper()} is active.
 *
 * <p>In this variant, there is just one global system time controlled by this class. The current
 * time is fixed in place, and manually advanced by calling {@link
 * SystemClock#setCurrentTimeMillis(long)}
 *
 * <p>{@link SystemClock#uptimeMillis()} and {@link SystemClock#currentThreadTimeMillis()} are
 * identical.
 */
@Implements(
    value = SystemClock.class,
    isInAndroidSdk = false,
    shadowPicker = ShadowBaseSystemClock.Picker.class)
public class ShadowRealisticSystemClock extends ShadowBaseSystemClock {
  private static final long INITIAL_TIME = 100;
  private static final int MILLIS_PER_NANO = 1000000;;
  private static long currentTimeMillis = INITIAL_TIME;
  private static boolean networkTimeAvailable = true;

  /** Advances the current time by given millis, without sleeping the current thread/ */
  @Implementation
  protected static void sleep(long millis) {
    currentTimeMillis += millis;
  }

  /**
   * Sets the current wall time.
   *
   * <p>Currently does not perform any permission checks.
   *
   * @return false if specified time is less than current time.
   */
  @Implementation
  protected static boolean setCurrentTimeMillis(long millis) {
    if (currentTimeMillis > millis) {
      return false;
    }

    currentTimeMillis = millis;
    return true;
  }

  @Implementation
  protected static long uptimeMillis() {
    return currentTimeMillis;
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
    return currentThreadTimeMicro();
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected static long currentNetworkTimeMillis() {
    if (networkTimeAvailable) {
      return currentTimeMillis;
    } else {
      throw new DateTimeException("Network time not available");
    }
  }

  /** Sets whether network time is available. */
  public static void setNetworkTimeAvailable(boolean available) {
    networkTimeAvailable = available;
  }

  public static void advanceBy(long timeValue, TimeUnit timeUnit) {
    currentTimeMillis += timeUnit.toMillis(timeValue);
  }

  @Resetter
  public static void reset() {
    currentTimeMillis = INITIAL_TIME;
    networkTimeAvailable = true;
  }
}
