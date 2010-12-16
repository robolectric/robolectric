package com.xtremelabs.robolectric.shadows;

import android.os.Looper;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.Scheduler;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;

/**
 * Shadow for {@code Looper} that enqueues posted {@link Runnable}s to be run (on this thread) later. {@code Runnable}s
 * that are scheduled to run immediately can be triggered by calling {@link #idle()}
 * todo: provide better support for advancing the clock and running queued tasks
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Looper.class)
public class ShadowLooper {
    private static ThreadLocal<Looper> sThreadLocal;
    private static Looper MAIN_LOOPER;

    private Scheduler scheduler = new Scheduler();
    private boolean isPaused = false;


    static {
        resetAll();
    }

    public static void resetAll() {
        sThreadLocal = new ThreadLocal<Looper>() {
            @Override
            protected Looper initialValue() {
                return newInstanceOf(Looper.class);
            }
        };

        MAIN_LOOPER = sThreadLocal.get();
    }

    @Implementation
    public static Looper getMainLooper() {
        return MAIN_LOOPER;
    }

    @Implementation
    public static Looper myLooper() {
        return sThreadLocal.get();
    }

    /**
     * Causes {@link Runnable}s that have been scheduled to run immediately to actually run. Does not advance the
     * scheduler's clock;
     */
    public void idle() {
        scheduler.advanceBy(0);
    }

    /**
     * Causes {@link Runnable}s that have been scheduled to run within the next {@code intervalMillis} milliseconds to
     * run while advancing the scheduler's clock.
     */
    public void idle(long intervalMillis) {
        scheduler.advanceBy(intervalMillis);
    }

    /**
     * Causes all of the {@link Runnable}s that have been scheduled to run while advancing the scheduler's clock to the
     * start time of the last scheduled {@link Runnable}.
     */
    public void runToEndOfTasks() {
        scheduler.advanceToLastPostedRunnable();
    }

    /**
     * Causes the next {@link Runnable}(s) that have been scheduled to run while advancing the scheduler's clock to its
     * start time. If more than one {@link Runnable} is scheduled to run at this time then they will all be run.
     */
    public void runToNextTask() {
        scheduler.advanceToNextPostedRunnable();
    }

    /**
     * Causes only one of the next {@link Runnable}s that have been scheduled to run while advancing the scheduler's
     * clock to its start time. Only one {@link Runnable} will run even if more than one has ben scheduled to run at the
     * same time.
     */
    public void runOneTask() {
        scheduler.runOneTask();
    }

    /**
     * Enqueue a task to be run later.
     *
     * @param runnable the task to be run
     * @param delayMillis how many milliseconds into the (virtual) future to run it
     */
    public void post(Runnable runnable, long delayMillis) {
        if (isPaused || delayMillis > 0) {
            scheduler.postDelayed(runnable, delayMillis);
        } else {
            runnable.run();
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void unPause() {
        isPaused = false;
        scheduler.advanceToLastPostedRunnable();
    }

    /**
     * Causes all enqueued tasks to be discarded
     */
    public void reset() {
        scheduler.reset();
    }

    /**
     * Returns the {@link com.xtremelabs.robolectric.util.Scheduler} that is being used to manage the enqueued tasks.
     *
     * @return the {@link com.xtremelabs.robolectric.util.Scheduler} that is being used to manage the enqueued tasks.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }
}
