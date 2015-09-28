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
 * <p>The concept of current time is base on the current time of the master scheduler
 * (see {@link RuntimeEnvironment#getMasterScheduler()}. Note that unless you set the global
 * scheduling option, the background scheduler will have its own concept of current time which may
 * not be consistent with the master scheduler. See {@link org.robolectric.RoboSettings}.
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
   * {@link #setCurrentTime(long, TimeUnit) setCurrentTime(millis, TimeUnit.MILLISECONDS}}.
   * @param millis the new wall clock time in milliseconds.
   * @return <tt>true</tt> if the clock was successfully set.
   */
  @Implementation
  public static boolean setCurrentTimeMillis(long millis) {
    return setCurrentTime(millis, TimeUnit.MILLISECONDS);
  }

  /**
   * Sets the current (wall clock) time with the specified precision.
   *
   * @param newCurrentTime the new wall clock time.
   * @param units the units in which the new time is specified.
   * @return <tt>true</tt> if the clock was successfully set. At present this call always succeeds.
   */
  public static boolean setCurrentTime(long newCurrentTime, TimeUnit units) {
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
   * Retrieves the simulated wall clock time in nanoseconds.
   *
   * @return The simulated wall clock time, in nanoseconds.
   */
  public static long currentTimeNanos() {
    return RuntimeEnvironment.getMasterScheduler().getCurrentTime(TimeUnit.NANOSECONDS) +
        wallClockOffsetNanos;
  }

  @HiddenApi
  @Implementation
  public static long currentTimeMicro() {
    return TimeUnit.NANOSECONDS.toMicros(currentTimeNanos());
  }

  /**
   * Implements {@link System#currentTimeMillis} through ShadowWrangler.
   *
   * @return Current time in millis.
   */
 public static long currentTimeMillis() {
    return TimeUnit.NANOSECONDS.toMillis(currentTimeNanos());
  }

  /**
   * Implements {@link System#nanoTime} through ShadowWrangler.
   *
   * @return Current time with nanos.
   */
  public static long nanoTime() {
    return RuntimeEnvironment.getMasterScheduler().getCurrentTime(TimeUnit.NANOSECONDS);
  }

  @Resetter
  public static void reset() {
    wallClockOffsetNanos = 0;
  }
//
//  public static void setNanoTime(long nanoTime) {
//    ShadowSystemClock.nanoTime = nanoTime;
//  }
}
