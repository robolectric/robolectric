package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;

import android.os.SystemClock;
import java.time.DateTimeException;
import java.util.concurrent.TimeUnit;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * A shadow SystemClock for {@link LooperMode.Mode.LEGACY}
 *
 * <p>In LEGACY LooperMode, Robolectric's concept of current time is base on the current time of the
 * UI Scheduler for consistency with previous implementations. This is not ideal, since both
 * schedulers (background and foreground), can see different values for the current time.
 */
@Implements(
    value = SystemClock.class,
    shadowPicker = ShadowSystemClock.Picker.class,
    // turn off shadowOf generation
    isInAndroidSdk = false)
public class ShadowLegacySystemClock extends ShadowSystemClock {
  private static long bootedAt = 0;
  private static long nanoTime = 0;
  private static final int MILLIS_PER_NANO = 1000000;

  static long now() {
    return RuntimeEnvironment.getMasterScheduler().getCurrentTime();
  }

  @Implementation
  protected static void sleep(long millis) {
    nanoTime = millis * MILLIS_PER_NANO;
    RuntimeEnvironment.getMasterScheduler().advanceBy(millis, TimeUnit.MILLISECONDS);
  }

  @Implementation
  protected static boolean setCurrentTimeMillis(long millis) {
    if (now() > millis) {
      return false;
    }
    nanoTime = millis * MILLIS_PER_NANO;
    RuntimeEnvironment.getMasterScheduler().advanceTo(millis);
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
    ShadowLegacySystemClock.nanoTime = nanoTime;
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

  @Resetter
  public static void reset() {
    ShadowSystemClock.reset();
  }
}
