package com.xtremelabs.robolectric.shadows;

import android.os.Looper;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.util.Scheduler;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow for {@code Looper} that enqueues posted {@link Runnable}s to be run (on this thread) later. {@code Runnable}s
 * that are scheduled to run immediately can be triggered by calling {@link #idle()}
 * todo: provide better support for advancing the clock and running queued tasks
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Looper.class)
public class ShadowLooper {
    private static ThreadLocal<Looper> looperForThread = makeThreadLocalLoopers();
    private Scheduler scheduler = new Scheduler();
    private Thread myThread = Thread.currentThread();

    boolean quit;

    private static synchronized ThreadLocal<Looper> makeThreadLocalLoopers() {
        return new ThreadLocal<Looper>() {
            @Override
            protected Looper initialValue() {
                return Robolectric.Reflection.newInstanceOf(Looper.class);
            }
        };
    }

    public static void resetThreadLoopers() {
        looperForThread = makeThreadLocalLoopers();
    }

    @Implementation
    public static Looper getMainLooper() {
        return Robolectric.getShadowApplication().getMainLooper();
    }

    @Implementation
    public static void loop() {
        final ShadowLooper looper = shadowOf(myLooper());
        if (looper != shadowOf(getMainLooper())) {
            while (!looper.quit) {
                try {
                    synchronized (looper) {
                        looper.wait();
                    }
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Implementation
    public static synchronized Looper myLooper() {
        return looperForThread.get();
    }

    @Implementation
    public void quit() {
        if (this == shadowOf(getMainLooper())) throw new RuntimeException("Main thread not allowed to quit");
        synchronized (this) {
            quit = true;
            scheduler.reset();
            notify();
        }
    }

    @Implementation
    public Thread getThread() {
    	return myThread;
    }
    
    public boolean hasQuit() {
        return quit;
    }

    public static void pauseLooper(Looper looper) {
        shadowOf(looper).pause();
    }

    public static void unPauseLooper(Looper looper) {
        shadowOf(looper).unPause();
    }

    public static void pauseMainLooper() {
        pauseLooper(Looper.getMainLooper());
    }

    public static void unPauseMainLooper() {
        unPauseLooper(Looper.getMainLooper());
    }

    public static void idleMainLooper(long interval) {
        shadowOf(Looper.getMainLooper()).idle(interval);
    }


    public static void idleMainLooperConstantly(boolean shouldIdleConstantly) {
        shadowOf(Looper.getMainLooper()).idleConstantly(shouldIdleConstantly);
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
     *
     * @param intervalMillis milliseconds to advance
     */
    public void idle(long intervalMillis) {
        scheduler.advanceBy(intervalMillis);
    }

    public void idleConstantly(boolean shouldIdleConstantly) {
        scheduler.idleConstantly(shouldIdleConstantly);
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
     * @param runnable    the task to be run
     * @param delayMillis how many milliseconds into the (virtual) future to run it
     */
    public boolean post(Runnable runnable, long delayMillis) {
        if (!quit) {
            scheduler.postDelayed(runnable, delayMillis);
            return true;
        } else {
            return false;
        }
    }

    public boolean postAtFrontOfQueue(Runnable runnable) {
        if (!quit) {
            scheduler.postAtFrontOfQueue(runnable);
            return true;
        } else {
            return false;
        }
    }

    public void pause() {
        scheduler.pause();
    }

    public void unPause() {
        scheduler.unPause();
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
