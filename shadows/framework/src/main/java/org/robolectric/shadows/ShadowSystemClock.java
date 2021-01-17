package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static java.time.ZoneOffset.UTC;
import static org.robolectric.shadows.ShadowLooper.assertLooperMode;

import android.os.SimpleClock;
import android.os.SystemClock;
import java.time.DateTimeException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;

/**
 * The shadow API for {@link SystemClock}.
 *
 * The behavior of SystemClock in Robolectric will differ based on the current {@link
 * LooperMode}. See {@link ShadowLegacySystemClock} and {@link ShadowPausedSystemClock} for more
 * details.
 */
@Implements(value = SystemClock.class, shadowPicker = ShadowSystemClock.Picker.class,
    looseSignatures = true)
public abstract class ShadowSystemClock {
  protected static boolean networkTimeAvailable = true;
  private static boolean gnssTimeAvailable = true;

  /**
   * Implements {@link System#currentTimeMillis} through ShadowWrangler.
   *
   * @return Current time in millis.
   */
  @SuppressWarnings("unused")
  public static long currentTimeMillis() {
    return ShadowLegacySystemClock.currentTimeMillis();
  }

  /**
   * Implements {@link System#nanoTime}.
   *
   * @return Current time with nanos.
   * @deprecated Don't call this method directly; instead, use {@link System#nanoTime()}.
   */
  @SuppressWarnings("unused")
  @Deprecated
  public static long nanoTime() {
    return ShadowSystem.nanoTime();
  }

  /**
   * Sets the value for {@link System#nanoTime()}.
   *
   * <p>May only be used for {@link LooperMode.Mode#LEGACY}. For {@link LooperMode.Mode#PAUSED},
   * {@param nanoTime} is calculated based on {@link SystemClock#uptimeMillis()} and can't be set
   * explicitly.
   */
  public static void setNanoTime(long nanoTime) {
    assertLooperMode(Mode.LEGACY);
    ShadowLegacySystemClock.setNanoTime(nanoTime);
  }

  /** Sets whether network time is available. */
  public static void setNetworkTimeAvailable(boolean available) {
    networkTimeAvailable = available;
  }

  /**
   * An alternate to {@link #advanceBy(Duration)} for older Android code bases where Duration is not
   * available.
   */
  public static void advanceBy(long time, TimeUnit unit) {
    SystemClock.setCurrentTimeMillis(SystemClock.uptimeMillis() + unit.toMillis(time));
  }

  /**
   * A convenience method for advancing the clock via {@link SystemClock#setCurrentTimeMillis(long)}
   *
   * @param duration The interval by which to advance.
   */
  public static void advanceBy(Duration duration) {
    SystemClock.setCurrentTimeMillis(SystemClock.uptimeMillis() + duration.toMillis());
  }

  @Implementation(minSdk = Q)
  protected static Object currentGnssTimeClock() {
    if (gnssTimeAvailable) {
      return new SimpleClock(UTC) {
        @Override
        public long millis() {
          return SystemClock.uptimeMillis();
        }
      };
    } else {
      throw new DateTimeException("Gnss based time is not available.");
    }
  }

  /** Sets whether gnss location based time is available. */
  public static void setGnssTimeAvailable(boolean available) {
    gnssTimeAvailable = available;
  }

  public static void reset() {
    networkTimeAvailable = true;
    gnssTimeAvailable = true;
  }

  public static class Picker extends LooperShadowPicker<ShadowSystemClock> {

    public Picker() {
      super(ShadowLegacySystemClock.class, ShadowPausedSystemClock.class);
    }
  }
}
