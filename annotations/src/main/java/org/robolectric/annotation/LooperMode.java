package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation for controlling Robolectric's
 * {@link android.os.Looper} behavior.
 *
 * <p>Currently Robolectric will default to {@link LooperMode.Mode#PAUSED} behavior, but this can be
 * overridden by applying a @LooperMode(NewMode) annotation to a test package, test class, or test
 * method, or via the 'robolectric.looperMode' system property.
 *
 * @see org.robolectric.plugins.LooperModeConfigurer
 * @see org.robolectric.util.Scheduler
 * @see org.robolectric.shadows.ShadowLooper
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface LooperMode {

  /** Specifies the different supported Looper modes. */
  enum Mode {
    /**
     * Robolectric's default threading model prior to 4.4.
     *
     * <p>Tasks posted to Loopers are managed via a {@link org.robolectric.util.Scheduler}. {@link
     * org.robolectric.util.Scheduler} behavior can be controlled via {@link
     * org.robolectric.util.Scheduler#setIdleState(org.robolectric.util.Scheduler.IdleState)
     * setIdleState(IdleState)}, with a default of {@link
     * org.robolectric.util.Scheduler.IdleState#UNPAUSED UNPAUSED}.
     *
     * <p>There is only a single Looper thread - with tests and all posted Looper tasks executing on
     * that thread.
     *
     * <p>{@link org.robolectric.shadows.ShadowLooper} APIs can also be used to control posted
     * tasks, but most of those APIs just serve as a facade to {@link
     * org.robolectric.util.Scheduler} APIs.
     *
     * <p>There are multiple problems with this mode. Some of the major ones are:
     *
     * <ol>
     *   <li>The default {@link org.robolectric.util.Scheduler.IdleState#UNPAUSED UNPAUSED} state
     *       will execute tasks posted to a {@link android.os.Looper} inline synchronously. This
     *       differs from real Android behaviour, and can cause issues with code that
     *       expects/enforces that posted tasks execute in the correct order, such as RecyclerViews.
     *   <li>The {@link org.robolectric.util.Scheduler} list of Runnables can get out of sync with
     *       the Looper's {@link android.os.MessageQueue}, causing deadlocks or other race
     *       conditions.
     *   <li>Each {@link org.robolectric.util.Scheduler} keeps its own time value, which can get out
     *       of sync.
     *   <li>Background {@link android.os.Looper} tasks execute in the main thread, causing errors
     *       for code that enforces that it runs on a non-main {@link android.os.Looper} thread.
     * </ol>
     *
     * @deprecated use LooperMode.PAUSED
     */
    @Deprecated
    LEGACY,

    /**
     * A mode that more accurately models real Android's {@link android.os.Looper} behavior.
     *
     * <p>Conceptually LooperMode.PAUSED is similar to the LEGACY {@link
     * org.robolectric.util.Scheduler.IdleState#PAUSED} in the following ways:
     *
     * <ul>
     *   <li>Tests run on the main looper thread
     *   <li>Tasks posted to the main {@link android.os.Looper} are not executed automatically, and
     *       must be explicitly executed via {@link org.robolectric.shadows.ShadowLooper} APIs like
     *       {@link org.robolectric.shadows.ShadowLooper#idle()}. This guarantees execution order
     *       correctness
     *   <li>{@link android.os.SystemClock} time is frozen, and can be manually advanced via
     *       Robolectric APIs.
     * </ul>
     *
     * However, it has the following improvements:
     *
     * <ul>
     *   <li>Robolectric will warn users if a test fails with unexecuted tasks in the main Looper
     *       queue
     *   <li>Robolectric test APIs, like {@link
     *       org.robolectric.android.controller.ActivityController#setup()}, will automatically idle
     *       the main {@link android.os.Looper}
     *   <li>Each {@link android.os.Looper} has its own thread. Tasks posted to background loopers
     *       are executed asynchronously in separate threads.
     *   <li>{@link android.os.Looper} use the real {@link android.os.MessageQueue} to store their
     *       queue of pending tasks
     *   <li>There is only a single clock value, managed via {@link
     *       org.robolectric.shadows.ShadowSystemClock}. This can be explictly incremented via
     *       {@link android.os.SystemClock#setCurrentTimeMillis(long)}, or {@link
     *       org.robolectric.shadows.ShadowLooper#idleFor(Duration)}.
     * </ul>
     *
     * A subset of the {@link org.robolectric.util.Scheduler} APIs for the 'foreground' scheduler
     * are currently supported in this mode as well, although it is recommended to switch to use
     * ShadowLooper APIs directly.
     *
     * <p>To use:
     *
     * <ul>
     *   <li>Apply the LooperMode(PAUSED) annotation to your test package/class/method (or remove a
     *       LooperMode(LEGACY) annotation)
     *   <li>Convert any background {@link org.robolectric.util.Scheduler} for controlling {@link
     *       android.os.Looper}s to shadowOf(looper)
     *   <li>Convert any {@link org.robolectric.android.util.concurrent.RoboExecutorService} usages
     *       to {@link org.robolectric.android.util.concurrent.PausedExecutorService} or {@link
     *       org.robolectric.android.util.concurrent.InlineExecutorService}
     *   <li>Run your tests. If you see an test failures like 'Main looper has queued unexecuted
     *       runnables.', you may need to insert shadowOf(getMainLooper()).idle() calls to your test
     *       to drain the main Looper.
     * </ul>
     */
    PAUSED,

    /**
     * Currently not supported.
     *
     * <p>In future, will have free running threads with an automatically increasing clock.
     */
    // RUNNING
  }

  /** Set the Looper mode. */
  Mode value();
}
