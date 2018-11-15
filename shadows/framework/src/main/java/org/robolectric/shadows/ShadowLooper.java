package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.robolectric.RuntimeEnvironment.isMainThread;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.os.Looper;
import android.os.MessageQueue;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import org.robolectric.RoboSettings;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Scheduler;

/**
 * Robolectric enqueues posted {@link Runnable}s to be run
 * (on this thread) later. {@code Runnable}s that are scheduled to run immediately can be
 * triggered by calling {@link #idle()}.
 *
 * @see ShadowMessageQueue
 */
@Implements(Looper.class)
@SuppressWarnings("SynchronizeOnNonFinalField")
public class ShadowLooper {

  // Replaced SoftThreadLocal with a WeakHashMap, because ThreadLocal make it impossible to access their contents from other
  // threads, but we need to be able to access the loopers for all threads so that we can shut them down when resetThreadLoopers()
  // is called. This also allows us to implement the useful getLooperForThread() method.
  // Note that the main looper is handled differently and is not put in this hash, because we need to be able to
  // "switch" the thread that the main looper is associated with.
  private static Map<Thread, Looper> loopingLoopers = Collections.synchronizedMap(new WeakHashMap<Thread, Looper>());

  private static Looper mainLooper;

  private @RealObject Looper realObject;

  boolean quit;

  @Resetter
  public static synchronized void resetThreadLoopers() {
    // Blech. We need to keep the main looper because somebody might refer to it in a static
    // field. The other loopers need to be wrapped in WeakReferences so that they are not prevented from
    // being garbage collected.
    if (!isMainThread()) {
      throw new IllegalStateException("you should only be calling this from the main thread!");
    }
    synchronized (loopingLoopers) {
      for (Looper looper : loopingLoopers.values()) {
        synchronized (looper) {
          if (!shadowOf(looper).quit) {
            looper.quit();
          } else {
            // Reset the schedulers of all loopers. This prevents un-run tasks queued up in static
            // background handlers from leaking to subsequent tests.
            shadowOf(looper).getScheduler().reset();
            shadowOf(looper.getQueue()).reset();
          }
        }
      }
    }
    // Because resetStaticState() is called by ParallelUniverse on startup before prepareMainLooper() is
    // called, this might be null on that occasion.
    if (mainLooper != null) {
      shadowOf(mainLooper).reset();
    }
  }

  @Implementation
  protected void __constructor__(boolean quitAllowed) {
    invokeConstructor(Looper.class, realObject, from(boolean.class, quitAllowed));
    if (isMainThread()) {
      mainLooper = realObject;
    } else {
      loopingLoopers.put(Thread.currentThread(), realObject);
    }
    resetScheduler();
  }

  @Implementation
  protected static Looper getMainLooper() {
    return mainLooper;
  }

  @Implementation
  protected static Looper myLooper() {
    return getLooperForThread(Thread.currentThread());
  }

  @Implementation
  protected static void loop() {
    shadowOf(Looper.myLooper()).doLoop();
  }

  private void doLoop() {
    if (realObject != Looper.getMainLooper()) {
      synchronized (realObject) {
        while (!quit) {
          try {
            realObject.wait();
          } catch (InterruptedException ignore) {
          }
        }
      }
    }
  }

  @Implementation
  protected void quit() {
    if (realObject == Looper.getMainLooper()) throw new RuntimeException("Main thread not allowed to quit");
    quitUnchecked();
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected void quitSafely() {
    quit();
  }

  public void quitUnchecked() {
    synchronized (realObject) {
      quit = true;
      realObject.notifyAll();
      getScheduler().reset();
      shadowOf(realObject.getQueue()).reset();
    }
  }

  public boolean hasQuit() {
    synchronized (realObject) {
      return quit;
    }
  }

  /** @deprecated Use `shadowOf({@link Looper#getMainLooper()})` instead. */
  @Deprecated
  public static ShadowLooper getShadowMainLooper() {
    return shadowOf(Looper.getMainLooper());
  }
  
  public static Looper getLooperForThread(Thread thread) {
    return isMainThread(thread) ? mainLooper : loopingLoopers.get(thread);
  }
  
  public static void pauseLooper(Looper looper) {
    shadowOf(looper).pause();
  }

  public static void unPauseLooper(Looper looper) {
    shadowOf(looper).unPause();
  }

  public static void pauseMainLooper() {
    getShadowMainLooper().pause();
  }

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
   * Runs any immediately runnable tasks previously queued on the UI thread,
   * e.g. by {@link android.app.Activity#runOnUiThread(Runnable)} or {@link android.os.AsyncTask#onPostExecute(Object)}.
   *
   * **Note:** calling this method does not pause or un-pause the scheduler.
   *
   * @see #runUiThreadTasksIncludingDelayedTasks
   */
  public static void runUiThreadTasks() {
    getShadowMainLooper().idle();
  }

  /**
   * Runs all runnable tasks (pending and future) that have been queued on the UI thread. Such tasks may be queued by
   * e.g. {@link android.app.Activity#runOnUiThread(Runnable)} or {@link android.os.AsyncTask#onPostExecute(Object)}.
   *
   * **Note:** calling this method does not pause or un-pause the scheduler, however the clock is advanced as
   * future tasks are run.
   * 
   * @see #runUiThreadTasks
   */
  public static void runUiThreadTasksIncludingDelayedTasks() {
    getShadowMainLooper().runToEndOfTasks();
  }

  /**
   * Causes {@link Runnable}s that have been scheduled to run immediately to actually run. Does not advance the
   * scheduler's clock;
   */
  public void idle() {
    idle(0, TimeUnit.MILLISECONDS);
  }

  /**
   * Causes {@link Runnable}s that have been scheduled to run within the next {@code intervalMillis} milliseconds to
   * run while advancing the scheduler's clock.
   *
   * @deprecated Use {@link #idle(long, TimeUnit)}.
   */
  @Deprecated
  public void idle(long intervalMillis) {
    idle(intervalMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Causes {@link Runnable}s that have been scheduled to run within the next specified amount of time to run while
   * advancing the scheduler's clock.
   */
  public void idle(long amount, TimeUnit unit) {
    getScheduler().advanceBy(amount, unit);
  }

  public void idleConstantly(boolean shouldIdleConstantly) {
    getScheduler().idleConstantly(shouldIdleConstantly);
  }

  /**
   * Causes all of the {@link Runnable}s that have been scheduled to run while advancing the scheduler's clock to the
   * start time of the last scheduled {@link Runnable}.
   */
  public void runToEndOfTasks() {
    getScheduler().advanceToLastPostedRunnable();
  }

  /**
   * Causes the next {@link Runnable}(s) that have been scheduled to run while advancing the scheduler's clock to its
   * start time. If more than one {@link Runnable} is scheduled to run at this time then they will all be run.
   */
  public void runToNextTask() {
    getScheduler().advanceToNextPostedRunnable();
  }

  /**
   * Causes only one of the next {@link Runnable}s that have been scheduled to run while advancing the scheduler's
   * clock to its start time. Only one {@link Runnable} will run even if more than one has ben scheduled to run at the
   * same time.
   */
  public void runOneTask() {
    getScheduler().runOneTask();
  }

  /**
   * Enqueue a task to be run later.
   *
   * @param runnable    the task to be run
   * @param delayMillis how many milliseconds into the (virtual) future to run it
   * @return true if the runnable is enqueued
   * @see android.os.Handler#postDelayed(Runnable,long)
   * @deprecated Use a {@link android.os.Handler} instance to post to a looper.
   */
  @Deprecated
  public boolean post(Runnable runnable, long delayMillis) {
    if (!quit) {
      getScheduler().postDelayed(runnable, delayMillis, TimeUnit.MILLISECONDS);
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Enqueue a task to be run ahead of all other delayed tasks.
   *
   * @param runnable    the task to be run
   * @return true if the runnable is enqueued
   * @see android.os.Handler#postAtFrontOfQueue(Runnable)
   * @deprecated Use a {@link android.os.Handler} instance to post to a looper.
   */
  @Deprecated
  public boolean postAtFrontOfQueue(Runnable runnable) {
    if (!quit) {
      getScheduler().postAtFrontOfQueue(runnable);
      return true;
    } else {
      return false;
    }
  }

  public void pause() {
    getScheduler().pause();
  }

  public void unPause() {
    getScheduler().unPause();
  }

  public boolean isPaused() {
    return getScheduler().isPaused();
  }

  public boolean setPaused(boolean shouldPause) {
    boolean wasPaused = isPaused();
    if (shouldPause) {
      pause();
    } else {
      unPause();
    }
    return wasPaused;
  }

  public void resetScheduler() {
    ShadowMessageQueue shadowMessageQueue = shadowOf(realObject.getQueue());
    if (realObject == Looper.getMainLooper() || RoboSettings.isUseGlobalScheduler()) {
      shadowMessageQueue.setScheduler(RuntimeEnvironment.getMasterScheduler());
    } else {
      shadowMessageQueue.setScheduler(new Scheduler());
    }
  }

  /**
   * Causes all enqueued tasks to be discarded, and pause state to be reset
   */
  public void reset() {
    shadowOf(realObject.getQueue()).reset();
    resetScheduler();

    quit = false;
  }

  /**
   * Returns the {@link org.robolectric.util.Scheduler} that is being used to manage the enqueued tasks.
   * This scheduler is managed by the Looper's associated queue.
   *
   * @return the {@link org.robolectric.util.Scheduler} that is being used to manage the enqueued tasks.
   */
  public Scheduler getScheduler() {
    return shadowOf(realObject.getQueue()).getScheduler();
  }

  public void runPaused(Runnable r) {
    boolean wasPaused = setPaused(true);
    try {
      r.run();
    } finally {
      if (!wasPaused) unPause();
    }
  }

  private static ShadowLooper shadowOf(Looper looper) {
    return Shadow.extract(looper);
  }

  private static ShadowMessageQueue shadowOf(MessageQueue mq) {
    return Shadow.extract(mq);
  }
}
