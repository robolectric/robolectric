package org.robolectric.shadows;

import android.os.SystemClock;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Resetter;

import java.util.concurrent.TimeUnit;

/**
 * Shadow for {@link android.os.SystemClock}.
 *
 * <p>In Android, there are three main clocks in use (see {@link SystemClock} for more details).
 * <tt>ShadowSystemClock</tt> emulates these clocks in the following way:</p>
 * <ol><li>The main system clock ({@link SystemClock#uptimeMillis()} and {@link System#nanoTime()}),
 * measures time since boot but not including deep sleep. This clock is used for most
 * interval timing within Android (eg, {@link android.os.Looper}s and {@link SystemClock#sleep(long)}).
 * Robolectric gets this time from the foreground/master scheduler (see
 * {@link RuntimeEnvironment#getMasterScheduler()}.</li>
 * <li>The realtime clock {{@link SystemClock#elapsedRealtime()}}, which measures actual time since
 * boot including any deep sleep. This is used for measuring intervals that must span deep sleep.
 * In Robolectric (at present), this clock is identical the system clock - ie, there is (as yet) no
 * way to emulate deep sleep.</li>
 * <li>The wall clock, which (as the name suggests) is supposed to correspond to the time as
 * reported by the clock on your wall ({@link System#currentTimeMillis()}). In Robolectric, this
 * is emulated within <tt>ShadowSystemClock</tt> by maintaining an offset relative to the system
 * clock - when the wall time is requested, this offset (which may be negative) is added to the
 * system time to produce the wall time. Thus the wall clock will advance at the same rate as you
 * advance the master scheduler, and programmatic changes to the wall clock (using
 * {@link #setCurrentTimeMillis(long) setCurrentTimeMillis()} or
 * {@link #setCurrentWallTime(long, TimeUnit) setCurrentWallTime()}) are emulated by updating this
 * offset.</li>
 * </ol>
 *
 * <p><b>Note</b>: Unfortunately, in versions of Robolectric prior to 3.1 these clocks were not
 * properly distinguished, and in particular the Android method
 * {@link #setCurrentTimeMillis(long) setCurrentTimeMillis()} was effectively implemented to advance
 * the system clock (ie, the master scheduler) whereas it is supposed to set the wall clock. This
 * causes scheduled tasks to be run. This has been fixed as of 3.1, but this change may break
 * implementations that were reliant on the broken behaviour.</p>
 *
 * <p>Note also that unless you set the global scheduling option, the background scheduler and other
 * loopers will have its own concept of current time which may not be consistent with the master
 * scheduler. See {@link org.robolectric.RoboSettings}.</p>
 */
@Implements(SystemClock.class)
public class ShadowSystemClock {
  /** Specifies the offset (in nanoseconds) between the current master clock and the wall clock. */
  private static long wallClockOffsetNanos = 0;

  @Implementation
  public static void sleep(long millis) {
    // TODO: This implementation of sleep() is flawed as it will cause events to execute, which
    // obviously they should not if the thread is sleeping. It will also effectively cause all
    // loopers to sleep in the same way.
    RuntimeEnvironment.getMasterScheduler().advanceBy(millis);
  }

  /**
   * Implements {@link SystemClock#setCurrentTimeMillis(long)}. Equivalent to
   * {@link #setCurrentWallTime(long, TimeUnit) setCurrentWallTime(millis, TimeUnit.MILLISECONDS}}.
   *
   * <b>Notes</b>:<ul><li>Prior to 3.1 this method effectively set the system clock as well as the
   * wall clock which is inconsistent with the behaviour in real Android. This behaviour has been
   * fixed in 3.1, which means if you were relyin on this broken behaviour to set the
   * system/scheduler clock it will no longer work. If you need to set the system/scheduler time,
   * access the master scheduler directly (see {@link RuntimeEnvironment#getMasterScheduler()}).</li>
   * <li>Also, prior to 3.1 this method would not allow you to wind the clock backwards, which
   * (again) is not consistent with Android - this restriction has been removed in 3.1.</li></ul>
   *
   * @param millis the new wall clock time in milliseconds.
   * @return <tt>true</tt> if the clock was successfully set. At present this always succeeds.
   */
  @Implementation
  public static boolean setCurrentTimeMillis(long millis) {
    return setCurrentWallTime(millis, TimeUnit.MILLISECONDS);
  }

  /**
   * Sets the current (wall clock) time with the specified precision.
   *
   * Future enhancements:
   * <ul><li>According to the Android specs, when the wall clock changes there are certain events
   * that are broadcast - this implementation does not yet do that.</li>
   * <li>It will also allow any process to set the clock, whereas in real Android some applications
   * may be restricted from doing so.</li></ul>
   *
   * @param newCurrentTime the new wall clock time.
   * @param units the units in which the new time is specified.
   * @return <tt>true</tt> if the clock was successfully set. At present this call always succeeds.
   */
  public static boolean setCurrentWallTime(long newCurrentTime, TimeUnit units) {
    // TODO: Need to issue system notifications when the wall clock time changes.
    final long nanoTime    = units.toNanos(newCurrentTime);
    final long currentTime = RuntimeEnvironment.getMasterScheduler().getCurrentTime(TimeUnit.NANOSECONDS);
    wallClockOffsetNanos = nanoTime - currentTime;
    return true;
  }

  @Implementation
  public static long uptimeMillis() {
    return RuntimeEnvironment.getMasterScheduler().getCurrentTime();
  }

  @Implementation
  public static long elapsedRealtime() {
    return uptimeMillis();
  }

  @Implementation
  public static long elapsedRealtimeNanos() {
    return nanoTime();
  }

  @Implementation
  public static long currentThreadTimeMillis() {
    return uptimeMillis();
  }

  @HiddenApi
  @Implementation
  public static long currentThreadTimeMicro() {
    return RuntimeEnvironment.getMasterScheduler().getCurrentTime(TimeUnit.MICROSECONDS);
  }

  /**
   * Retrieves the simulated wall clock time in nanoseconds since the epoch.
   *
   * @return The simulated wall clock time, in nanoseconds.
   */
  public static long currentTimeNanos() {
    return RuntimeEnvironment.getMasterScheduler().getCurrentTime(TimeUnit.NANOSECONDS) +
        wallClockOffsetNanos;
  }

  /**
   * Retrieves the simulated wall clock time in microseconds since the epoch.
   *
   * @return The simulated wall clock time, in microseconds.
   */
  @HiddenApi
  @Implementation
  public static long currentTimeMicro() {
    return TimeUnit.NANOSECONDS.toMicros(currentTimeNanos());
  }

  /**
   * Retrieves the simulated wall clock time in milliseconds since the epoch.
   * Implements {@link System#currentTimeMillis} through ShadowWrangler.
   *
   * @return The simulated wall clock time, in milliseconds.
   */
 public static long currentTimeMillis() {
    return TimeUnit.NANOSECONDS.toMillis(currentTimeNanos());
  }

  /**
   * Returns the current system clock in nanoseconds (as stored by the master scheduler). Implements
   * {@link System#nanoTime} through ShadowWrangler.
   *
   * @return The current system time in nanoseconds.
   */
  public static long nanoTime() {
    return RuntimeEnvironment.getMasterScheduler().getCurrentTime(TimeUnit.NANOSECONDS);
  }

  @Resetter
  public static void reset() {
    wallClockOffsetNanos = 0;
  }

  /**
   * Does nothing.
   *
   * @param nanoTime
   * @deprecated As of Robolectric 3.1, the nano time is slaved directly to the master scheduler -
   * see {@link RuntimeEnvironment#getMasterScheduler()} for methods to adjust the time.
   */
  @Deprecated
  public static void setNanoTime(long nanoTime) {
  }
}
