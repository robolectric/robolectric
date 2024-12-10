package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;

import android.os.SystemClock;
import java.time.DateTimeException;
import java.time.Duration;
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
  static final int MILLIS_PER_NANO = 1_000_000;
  private static final int MILLIS_PER_MICRO = 1_000;
  private static final long INITIAL_TIME_NS = 100 * MILLIS_PER_NANO;

  @SuppressWarnings("NonFinalStaticField")
  @GuardedBy("ShadowPausedSystemClock.class")
  private static long currentUptimeNs = INITIAL_TIME_NS;

  @SuppressWarnings("NonFinalStaticField")
  @GuardedBy("ShadowPausedSystemClock.class")
  private static long currentRealtimeNs = INITIAL_TIME_NS;

  private static final List<Listener> listeners = new CopyOnWriteArrayList<>();
  // hopefully temporary list of clock listeners that are NOT cleared between tests
  // This is needed to accommodate Loopers which are not reset between tests
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
      currentUptimeNs += (millis * MILLIS_PER_NANO);
      currentRealtimeNs += (millis * MILLIS_PER_NANO);
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
      currentRealtimeNs += (millis * MILLIS_PER_NANO);
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
   * <p>Use of this method is discouraged. It currently has the following inconsistent behavior:
   *
   * <ol>
   *   <li>>It doesn't check permissions. In real android this method is protected by the
   *       signature/privileged SET_TIME permission, thus it is uncallable by most apps
   *   <li>It doesn't actually change the value of System.currentTimeMillis for non-instrumented
   *       code aka nearly all user tests and apps It only allows advancing the current time, not
   *       moving it backwards
   *   <li>It incorrectly changes the value of SystemClock.uptime, elapsedRealtime, and for
   *       instrumented code System.nanoTime. In real android these are all independent clocks
   * </ol>
   *
   * <p>It is recommended to use ShadowSystemClock.advanceBy instead to advance
   * SystemClock.uptimeMillis and SystemClock.elapsedRealTime
   *
   * @return false if specified time is less than current uptime.
   */
  @Implementation
  protected static boolean setCurrentTimeMillis(long millis) {
    long newTimeNs = millis * MILLIS_PER_NANO;
    synchronized (ShadowPausedSystemClock.class) {
      if (currentUptimeNs > newTimeNs) {
        return false;
      } else if (currentUptimeNs == newTimeNs) {
        return true;
      } else {
        currentUptimeNs = newTimeNs;
        currentRealtimeNs = newTimeNs;
      }
    }
    informListeners();
    return true;
  }

  @Implementation
  protected static long uptimeMillis() {
    return uptimeNanos() / MILLIS_PER_NANO;
  }

  @Implementation(minSdk = S)
  protected static synchronized long uptimeNanos() {
    return currentUptimeNs;
  }

  @Implementation
  protected static long elapsedRealtime() {
    return elapsedRealtimeNanos() / MILLIS_PER_NANO;
  }

  @Implementation
  protected static synchronized long elapsedRealtimeNanos() {
    return currentRealtimeNs;
  }

  @Implementation
  protected static long currentThreadTimeMillis() {
    return uptimeMillis();
  }

  @HiddenApi
  @Implementation
  protected static long currentThreadTimeMicro() {
    return uptimeNanos() / MILLIS_PER_MICRO;
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
      return uptimeMillis();
    } else {
      throw new DateTimeException("Network time not available");
    }
  }

  static void internalAdvanceBy(Duration duration) {
    if (duration.toNanos() <= 0) {
      // ignore
      return;
    }
    synchronized (ShadowPausedSystemClock.class) {
      currentUptimeNs += duration.toNanos();
      currentRealtimeNs += duration.toNanos();
    }
    informListeners();
  }

  @Resetter
  public static synchronized void reset() {
    currentUptimeNs = INITIAL_TIME_NS;
    currentRealtimeNs = INITIAL_TIME_NS;
    ShadowSystemClock.reset();
    listeners.clear();
  }
}
