package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;

import android.os.SystemClock;
import java.time.DateTimeException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.Resetter;

/**
 * A shadow SystemClock used when {@link LooperMode.Mode#PAUSED} is active.
 *
 * <p>In this variant, there is just one global system time controlled by this class. The current
 * time is fixed in place, and manually advanced by calling {@link
 * SystemClock#setCurrentTimeMillis(long)}
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

  @GuardedBy("ShadowPausedSystemClock.class")
  private static long currentTimeMillis = INITIAL_TIME;

  private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

  /**
   * Callback for clock updates
   */
  interface Listener {
    void onClockAdvanced();
  }

  static void addListener(Listener listener) {
    listeners.add(listener);
  }

  static void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  /** Advances the current time by given millis, without sleeping the current thread/ */
  @Implementation
  protected static void sleep(long millis) {
    synchronized (ShadowPausedSystemClock.class) {
      currentTimeMillis += millis;
    }
    for (Listener listener : listeners) {
      listener.onClockAdvanced();
    }
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
    synchronized (ShadowPausedSystemClock.class) {
      if (currentTimeMillis > millis) {
        return false;
      } else if (currentTimeMillis == millis) {
        return true;
      } else {
        currentTimeMillis = millis;
      }
    }
    for (Listener listener : listeners) {
      listener.onClockAdvanced();
    }
    return true;
  }

  @Implementation
  protected static synchronized long uptimeMillis() {
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
      return currentTimeMillis;
    } else {
      throw new DateTimeException("Network time not available");
    }
  }

  @Resetter
  public static synchronized void reset() {
    currentTimeMillis = INITIAL_TIME;
    ShadowSystemClock.reset();
    listeners.clear();
  }
}
