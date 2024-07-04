package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.os.SystemClock;
import java.time.DateTimeException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * A shadow SystemClock used when {@link LooperMode.Mode#PAUSED} is active.
 *
 * <p>In this variant, System times (both elapsed realtime and uptime) are controlled by this class.
 * The current times are fixed in place. You can manually advance both by calling {@link
 * SystemClock#setCurrentTimeMillis(long)} or just advance elapsed realtime only by calling {@link
 * deepSleep(long)}.
 *
 * <p>{@link SystemClock#uptimeMillis()} and {@link SystemClock#currentThreadTimeMillis()} are
 * identical.
 *
 * <p>This class should not be referenced directly. Use ShadowSystemClock instead.
 */
@Implements(
    value = SystemClock.class,
    isInAndroidSdk = false,
    shadowPicker = ShadowSystemClock.Picker.class)
public class ShadowPausedSystemClock extends ShadowSystemClock {
  private static final long INITIAL_TIME = 100;
  private static final int MILLIS_PER_NANO = 1000000;

  @SuppressWarnings("NonFinalStaticField")
  @GuardedBy("ShadowPausedSystemClock.class")
  private static long currentUptimeMillis = INITIAL_TIME;

  @SuppressWarnings("NonFinalStaticField")
  @GuardedBy("ShadowPausedSystemClock.class")
  private static long currentRealtimeMillis = INITIAL_TIME;

  private static final List<Listener> listeners = new CopyOnWriteArrayList<>();
  // hopefully temporary list of clock listeners that are NOT cleared between tests
  // This is needed to accomodate Loopers which are not reset between tests
  private static final List<Listener> staticListeners = new CopyOnWriteArrayList<>();

  /** Callback for clock updates */
  interface Listener {
    void onClockAdvanced();
  }

  static void addListener(Listener listener) {
    listeners.add(listener);
  }

  static void removeListener(Listener listener) {
    listeners.remove(listener);
    staticListeners.remove(listener);
  }

  static void addStaticListener(Listener listener) {
    staticListeners.add(listener);
  }

  /**
   * Advances the current time (both elapsed realtime and uptime) by given millis, without sleeping
   * the current thread.
   */
  @Implementation
  protected static void sleep(long millis) {
    synchronized (ShadowPausedSystemClock.class) {
      currentUptimeMillis += millis;
      currentRealtimeMillis += millis;
    }
    informListeners();
  }

  /**
   * Advances the current time (elapsed realtime only) by given millis, without sleeping the current
   * thread.
   *
   * <p>This is to simulate scenarios like suspend-to-RAM, where only elapsed realtime is
   * incremented when the device is in deep sleep.
   */
  protected static void deepSleep(long millis) {
    synchronized (ShadowPausedSystemClock.class) {
      currentRealtimeMillis += millis;
    }
    informListeners();
  }

  private static void informListeners() {
    for (Listener listener : listeners) {
      listener.onClockAdvanced();
    }
    for (Listener listener : staticListeners) {
      listener.onClockAdvanced();
    }
  }

  /**
   * Sets the current wall time (both elapsed realtime and uptime).
   *
   * <p>This API sets both of the elapsed realtime and uptime to the specified value.
   *
   * <p>Currently does not perform any permission checks.
   *
   * @return false if specified time is less than current uptime.
   */
  @Implementation
  protected static boolean setCurrentTimeMillis(long millis) {
    synchronized (ShadowPausedSystemClock.class) {
      if (currentUptimeMillis > millis) {
        return false;
      } else if (currentUptimeMillis == millis) {
        return true;
      } else {
        currentUptimeMillis = millis;
        currentRealtimeMillis = millis;
      }
    }
    informListeners();
    return true;
  }

  @Implementation
  protected static synchronized long uptimeMillis() {
    return currentUptimeMillis;
  }

  @Implementation
  protected static synchronized long elapsedRealtime() {
    return currentRealtimeMillis;
  }

  @Implementation
  protected static long elapsedRealtimeNanos() {
    return elapsedRealtime() * MILLIS_PER_NANO;
  }

  @Implementation
  protected static long currentThreadTimeMillis() {
    return uptimeMillis();
  }

  @HiddenApi
  @Implementation
  protected static long currentThreadTimeMicro() {
    return uptimeMillis() * 1000;
  }

  @HiddenApi
  @Implementation
  protected static long currentTimeMicro() {
    return currentThreadTimeMicro();
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected static synchronized long currentNetworkTimeMillis() {
    if (networkTimeAvailable) {
      return currentUptimeMillis;
    } else {
      throw new DateTimeException("Network time not available");
    }
  }

  @Resetter
  public static synchronized void reset() {
    currentUptimeMillis = INITIAL_TIME;
    currentRealtimeMillis = INITIAL_TIME;
    ShadowSystemClock.reset();
    listeners.clear();
  }
}
