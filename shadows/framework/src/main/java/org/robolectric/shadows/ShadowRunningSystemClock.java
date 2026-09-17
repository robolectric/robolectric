package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import android.os.SystemClock;
import java.time.DateTimeException;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.Resetter;

/**
 * A shadow SystemClock used when {@link LooperMode.Mode#RUNNING} is active.
 *
 * <p>In this mode, time is anchored directly to the host's monotonic nanosecond clock ({@link
 * System#nanoTime()}) and advances automatically in real time.
 */
@Implements(
    value = SystemClock.class,
    isInAndroidSdk = false,
    shadowPicker = ShadowSystemClock.Picker.class)
public class ShadowRunningSystemClock extends ShadowSystemClock {

  @Implementation(minSdk = S)
  protected static long uptimeNanos() {
    // uptime HAS to be tied to System.nanoTime(), because Choreographer implementation
    // is dependent on it
    return System.nanoTime();
  }

  @Implementation
  protected static long uptimeMillis() {
    return NANOSECONDS.toMillis(uptimeNanos());
  }

  @Implementation
  protected static long elapsedRealtime() {
    return uptimeMillis();
  }

  @Implementation
  protected static long elapsedRealtimeNanos() {
    return uptimeNanos();
  }

  @Implementation
  protected static long currentThreadTimeMillis() {
    return uptimeMillis();
  }

  @HiddenApi
  @Implementation
  protected static long currentThreadTimeMicro() {
    return NANOSECONDS.toMicros(uptimeNanos());
  }

  @HiddenApi
  @Implementation
  protected static long currentTimeMicro() {
    return currentThreadTimeMicro();
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected static long currentNetworkTimeMillis() {
    if (networkTimeAvailable) {
      return uptimeMillis();
    } else {
      throw new DateTimeException("Network time not available");
    }
  }

  /**
   * Setting the system time is unsupported in RUNNING mode and returns false.
   *
   * <p>Note: This does not match real Android implementation where {@link
   * SystemClock#setCurrentTimeMillis(long)} modifies the kernel RTC wall clock when the caller
   * holds the {@code android.permission.SET_TIME} permission.
   */
  @Implementation
  protected static boolean setCurrentTimeMillis(long millis) {
    return false;
  }

  @Resetter
  public static void reset() {
    ShadowSystemClock.reset();
  }
}
