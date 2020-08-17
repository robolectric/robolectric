package org.robolectric.shadows;

import static android.os.Looper.getMainLooper;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.os.Looper;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Scheduler;

/**
 * The base shadow API class for controlling Loopers.
 *
 * <p>It will delegate calls to the appropriate shadow based on the current LooperMode.
 */
@Implements(value = Looper.class, shadowPicker = ShadowLooper.Picker.class)
public abstract class ShadowLooper {

  public static void assertLooperMode(LooperMode.Mode expectedMode) {
    LooperMode.Mode looperMode = ConfigurationRegistry.get(LooperMode.Mode.class);
    if (looperMode != expectedMode) {
      throw new IllegalStateException("this action is not supported in " + looperMode + " mode.");
    }
  }

  private static ShadowLooper shadowLooper(Looper looper) {
    return Shadow.extract(looper);
  }

  /** @deprecated Use {@code shadowOf({@link Looper#getMainLooper()})} instead. */
  @Deprecated
  public static ShadowLooper getShadowMainLooper() {
    return shadowLooper(getMainLooper());
  }

  // TODO: should probably remove this
  public static ShadowLooper shadowMainLooper() {
    return shadowLooper(getMainLooper());
  }

  public static Looper getLooperForThread(Thread thread) {
    if (looperMode() == LEGACY) {
      return ShadowLegacyLooper.getLooperForThread(thread);
    }
    throw new UnsupportedOperationException(
        "this action is not supported in " + looperMode() + " mode.");
  }

  /** Return all created loopers. */
  public static Collection<Looper> getAllLoopers() {
    if (looperMode() == LEGACY) {
      return ShadowLegacyLooper.getLoopers();
    } else {
      return ShadowPausedLooper.getLoopers();
    }
  }

  /** Should not be called directly - Robolectric internal use only. */
  public static void resetThreadLoopers() {
    if (looperMode() == LEGACY) {
      ShadowLegacyLooper.resetThreadLoopers();
      return;
    }
    throw new UnsupportedOperationException(
        "this action is not supported in " + looperMode() + " mode.");
  }

  /** Return the current {@link LooperMode}. */
  public static LooperMode.Mode looperMode() {
    return ConfigurationRegistry.get(LooperMode.Mode.class);
  }

  /**
   * Pauses execution of tasks posted to the ShadowLegacyLooper. This means that during tests, tasks
   * sent to the looper will not execute immediately, but will be queued in a way that is similar to
   * how a real looper works. These queued tasks must be executed explicitly by calling {@link
   * #runToEndOftasks} or a similar method, otherwise they will not run at all before your test
   * ends.
   *
   * @param looper the looper to pause
   */
  public static void pauseLooper(Looper looper) {
    shadowLooper(looper).pause();
  }

  /**
   * Puts the shadow looper in an "unpaused" state (this is the default state). This means that
   * during tests, tasks sent to the looper will execute inline, immediately, on the calling (main)
   * thread instead of being queued, in a way similar to how Guava's "DirectExecutorService" works.
   * This is likely not to be what you want: it will cause code to be potentially executed in a
   * different order than how it would execute on the device, and if you are using certain Android
   * APIs (such as view animations) that are non-reentrant, they may not work at all or do
   * unpredictable things. For more information, see <a
   * href="https://github.com/robolectric/robolectric/issues/3369">this discussion</a>.
   *
   * @param looper the looper to pause
   */
  public static void unPauseLooper(Looper looper) {
    shadowLooper(looper).unPause();
  }

  /**
   * Puts the main ShadowLegacyLooper in an "paused" state.
   *
   * @see #pauseLooper
   */
  public static void pauseMainLooper() {
    getShadowMainLooper().pause();
  }

  /**
   * Puts the main ShadowLegacyLooper in an "unpaused" state.
   *
   * @see #unPauseLooper
   */
  public static void unPauseMainLooper() {
    getShadowMainLooper().unPause();
  }

  public static void idleMainLooper() {
    getShadowMainLooper().idle();
  }

  /** @deprecated Use {@link #idleMainLooper(long, TimeUnit)}. */
  @Deprecated
  public static void idleMainLooper(long interval) {
    idleMainLooper(interval, TimeUnit.MILLISECONDS);
  }

  public static void idleMainLooper(long amount, TimeUnit unit) {
    getShadowMainLooper().idle(amount, unit);
  }

  public static void idleMainLooperConstantly(boolean shouldIdleConstantly) {
    getShadowMainLooper().idleConstantly(shouldIdleConstantly);
  }

  public static void runMainLooperOneTask() {
    getShadowMainLooper().runOneTask();
  }

  public static void runMainLooperToNextTask() {
    getShadowMainLooper().runToNextTask();
  }

  /**
   * Runs any immediately runnable tasks previously queued on the UI thread, e.g. by {@link
   * android.app.Activity#runOnUiThread(Runnable)} or {@link
   * android.os.AsyncTask#onPostExecute(Object)}.
   *
   * <p>**Note:** calling this method does not pause or un-pause the scheduler.
   *
   * @see #runUiThreadTasksIncludingDelayedTasks
   */
  public static void runUiThreadTasks() {
    getShadowMainLooper().idle();
  }

  /**
   * Runs all runnable tasks (pending and future) that have been queued on the UI thread. Such tasks
   * may be queued by e.g. {@link android.app.Activity#runOnUiThread(Runnable)} or {@link
   * android.os.AsyncTask#onPostExecute(Object)}.
   *
   * <p>**Note:** calling this method does not pause or un-pause the scheduler, however the clock is
   * advanced as future tasks are run.
   *
   * @see #runUiThreadTasks
   */
  public static void runUiThreadTasksIncludingDelayedTasks() {
    getShadowMainLooper().runToEndOfTasks();
  }

  public abstract void quitUnchecked();

  public abstract boolean hasQuit();

  /** Executes all posted tasks scheduled before or at the current time. */
  public abstract void idle();

  /**
   * Advances the system clock by the given time, then executes all posted tasks scheduled before or
   * at the given time.
   */
  public abstract void idleFor(long time, TimeUnit timeUnit);

  /** A variant of {@link #idleFor(long, TimeUnit)} that accepts a Duration. */
  @SuppressWarnings("AndroidJdkLibsChecker")
  public void idleFor(Duration duration) {
    idleFor(duration.toMillis(), TimeUnit.MILLISECONDS);
  }

  /** Returns true if there are no pending tasks scheduled to be executed before current time. */
  public abstract boolean isIdle();

  /** Not supported for the main Looper in {@link LooperMode.Mode.PAUSED}. */
  public abstract void unPause();

  public abstract boolean isPaused();

  /**
   * Control the paused state of the Looper.
   *
   * <p>Not supported for the main Looper in {@link LooperMode.Mode.PAUSED}.
   */
  public abstract boolean setPaused(boolean shouldPause);

  /** Only supported for {@link LooperMode.Mode.LEGACY}. */
  public abstract void resetScheduler();

  /** Causes all enqueued tasks to be discarded, and pause state to be reset */
  public abstract void reset();

  /**
   * Returns the {@link org.robolectric.util.Scheduler} that is being used to manage the enqueued
   * tasks. This scheduler is managed by the Looper's associated queue.
   *
   * <p>Only supported for {@link LooperMode.Mode.LEGACY}.
   *
   * @return the {@link org.robolectric.util.Scheduler} that is being used to manage the enqueued
   *     tasks.
   */
  public abstract Scheduler getScheduler();

  /**
   * Runs the current task with the looper paused.
   *
   * <p>When LooperMode is PAUSED, this will execute all pending tasks scheduled before the current
   * time.
   */
  public abstract void runPaused(Runnable run);

  /**
   * Helper method to selectively call idle() only if LooperMode is PAUSED.
   *
   * <p>Intended for backwards compatibility, to avoid changing behavior for tests still using
   * LEGACY LooperMode.
   */
  public abstract void idleIfPaused();

  /**
   * Causes {@link Runnable}s that have been scheduled to run within the next {@code intervalMillis}
   * milliseconds to run while advancing the scheduler's clock.
   *
   * @deprecated Use {@link #idle(long, TimeUnit)}.
   */
  @Deprecated
  public void idle(long intervalMillis) {
    idleFor(Duration.ofMillis(intervalMillis));
  }

  /**
   * Causes {@link Runnable}s that have been scheduled to run within the next specified amount of
   * time to run while advancing the clock.
   *
   * @deprecated use {@link idleFor(amount, unit)}
   */
  @Deprecated
  public void idle(long amount, TimeUnit unit) {
    idleFor(amount, unit);
  }

  public abstract void idleConstantly(boolean shouldIdleConstantly);

  /**
   * Causes all of the {@link Runnable}s that have been scheduled to run while advancing the clock
   * to the start time of the last scheduled {@link Runnable}.
   */
  public abstract void runToEndOfTasks();

  /**
   * Causes the next {@link Runnable}(s) that have been scheduled to run while advancing the clock
   * to its start time. If more than one {@link Runnable} is scheduled to run at this time then they
   * will all be run.
   */
  public abstract void runToNextTask();

  /**
   * Causes only one of the next {@link Runnable}s that have been scheduled to run while advancing
   * the clock to its start time. Only one {@link Runnable} will run even if more than one has been
   * scheduled to run at the same time.
   */
  public abstract void runOneTask();

  /**
   * Enqueue a task to be run later.
   *
   * @param runnable the task to be run
   * @param delayMillis how many milliseconds into the (virtual) future to run it
   * @return true if the runnable is enqueued
   * @see android.os.Handler#postDelayed(Runnable,long)
   * @deprecated Use a {@link android.os.Handler} instance to post to a looper.
   */
  @Deprecated
  public abstract boolean post(Runnable runnable, long delayMillis);

  /**
   * Enqueue a task to be run ahead of all other delayed tasks.
   *
   * @param runnable the task to be run
   * @return true if the runnable is enqueued
   * @see android.os.Handler#postAtFrontOfQueue(Runnable)
   * @deprecated Use a {@link android.os.Handler} instance to post to a looper.
   */
  @Deprecated
  public abstract boolean postAtFrontOfQueue(Runnable runnable);

  /**
   * Pause the looper.
   *
   * <p>Has no practical effect for realistic looper, since it is always paused.
   */
  public abstract void pause();

  /**
   * @return the scheduled time of the next posted task; Duration.ZERO if there is no currently
   *     scheduled task.
   */
  public abstract Duration getNextScheduledTaskTime();

  /**
   * @return the scheduled time of the last posted task; Duration.ZERO 0 if there is no currently
   *     scheduled task.
   */
  public abstract Duration getLastScheduledTaskTime();

  public static class Picker extends LooperShadowPicker<ShadowLooper> {

    public Picker() {
      super(ShadowLegacyLooper.class, ShadowPausedLooper.class);
    }
  }
}
