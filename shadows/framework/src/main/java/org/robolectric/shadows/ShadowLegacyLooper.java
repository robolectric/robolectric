package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.robolectric.RuntimeEnvironment.isMainThread;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.os.Looper;
import android.os.MessageQueue;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import org.robolectric.RoboSettings;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Scheduler;

/**
 * The shadow Looper implementation for {@link LooperMode.Mode.LEGACY}.
 *
 * <p>Robolectric enqueues posted {@link Runnable}s to be run (on this thread) later. {@code
 * Runnable}s that are scheduled to run immediately can be triggered by calling {@link #idle()}.
 *
 * @see ShadowMessageQueue
 */
@Implements(value = Looper.class, isInAndroidSdk = false)
@SuppressWarnings("SynchronizeOnNonFinalField")
public class ShadowLegacyLooper extends ShadowLooper {

  // Replaced SoftThreadLocal with a WeakHashMap, because ThreadLocal make it impossible to access
  // their contents from other threads, but we need to be able to access the loopers for all
  // threads so that we can shut them down when resetThreadLoopers()
  // is called. This also allows us to implement the useful getLooperForThread() method.
  // Note that the main looper is handled differently and is not put in this hash, because we need
  // to be able to "switch" the thread that the main looper is associated with.
  private static Map<Thread, Looper> loopingLoopers =
      Collections.synchronizedMap(new WeakHashMap<Thread, Looper>());

  private static Looper mainLooper;

  private static Scheduler backgroundScheduler;

  private @RealObject Looper realObject;

  boolean quit;

  @Resetter
  public static synchronized void resetThreadLoopers() {
    // do not use looperMode() here, because its cached value might already have been reset
    if (ConfigurationRegistry.get(LooperMode.Mode.class) == LooperMode.Mode.PAUSED) {
      // ignore if realistic looper
      return;
    }
    // Blech. We need to keep the main looper because somebody might refer to it in a static
    // field. The other loopers need to be wrapped in WeakReferences so that they are not prevented
    // from being garbage collected.
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
    // Because resetStaticState() is called by AndroidTestEnvironment on startup before
    // prepareMainLooper() is called, this might be null on that occasion.
    if (mainLooper != null) {
      shadowOf(mainLooper).reset();
    }
  }

  static synchronized Scheduler getBackgroundThreadScheduler() {
    return backgroundScheduler;
  }

  /** Internal API to initialize background thread scheduler from AndroidTestEnvironment. */
  public static void internalInitializeBackgroundThreadScheduler() {
    backgroundScheduler =
        RoboSettings.isUseGlobalScheduler()
            ? RuntimeEnvironment.getMasterScheduler()
            : new Scheduler();
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
    if (realObject == Looper.getMainLooper()) {
      throw new RuntimeException("Main thread not allowed to quit");
    }
    quitUnchecked();
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected void quitSafely() {
    quit();
  }

  @Override
  public void quitUnchecked() {
    synchronized (realObject) {
      quit = true;
      realObject.notifyAll();
      getScheduler().reset();
      shadowOf(realObject.getQueue()).reset();
    }
  }

  @Override
  public boolean hasQuit() {
    synchronized (realObject) {
      return quit;
    }
  }

  public static Looper getLooperForThread(Thread thread) {
    return isMainThread(thread) ? mainLooper : loopingLoopers.get(thread);
  }

  /** Return loopers for all threads including main thread. */
  protected static Collection<Looper> getLoopers() {
    List<Looper> loopers = new ArrayList<>(loopingLoopers.values());
    loopers.add(mainLooper);
    return Collections.unmodifiableCollection(loopers);
  }

  @Override
  public void idle() {
    idle(0, TimeUnit.MILLISECONDS);
  }

  @Override
  public void idleFor(long time, TimeUnit timeUnit) {
    getScheduler().advanceBy(time, timeUnit);
  }

  @Override
  public boolean isIdle() {
    return !getScheduler().areAnyRunnable();
  }

  @Override
  public void idleIfPaused() {
    // ignore
  }

  @Override
  public void idleConstantly(boolean shouldIdleConstantly) {
    getScheduler().idleConstantly(shouldIdleConstantly);
  }

  @Override
  public void runToEndOfTasks() {
    getScheduler().advanceToLastPostedRunnable();
  }

  @Override
  public void runToNextTask() {
    getScheduler().advanceToNextPostedRunnable();
  }

  @Override
  public void runOneTask() {
    getScheduler().runOneTask();
  }

  /**
   * Enqueue a task to be run later.
   *
   * @param runnable the task to be run
   * @param delayMillis how many milliseconds into the (virtual) future to run it
   * @return true if the runnable is enqueued
   * @see android.os.Handler#postDelayed(Runnable,long)
   * @deprecated Use a {@link android.os.Handler} instance to post to a looper.
   */
  @Override
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
   * @param runnable the task to be run
   * @return true if the runnable is enqueued
   * @see android.os.Handler#postAtFrontOfQueue(Runnable)
   * @deprecated Use a {@link android.os.Handler} instance to post to a looper.
   */
  @Override
  @Deprecated
  public boolean postAtFrontOfQueue(Runnable runnable) {
    if (!quit) {
      getScheduler().postAtFrontOfQueue(runnable);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void pause() {
    getScheduler().pause();
  }

  @Override
  public Duration getNextScheduledTaskTime() {
    return getScheduler().getNextScheduledTaskTime();
  }

  @Override
  public Duration getLastScheduledTaskTime() {
    return getScheduler().getLastScheduledTaskTime();
  }

  @Override
  public void unPause() {
    getScheduler().unPause();
  }

  @Override
  public boolean isPaused() {
    return getScheduler().isPaused();
  }

  @Override
  public boolean setPaused(boolean shouldPause) {
    boolean wasPaused = isPaused();
    if (shouldPause) {
      pause();
    } else {
      unPause();
    }
    return wasPaused;
  }

  @Override
  public void resetScheduler() {
    ShadowMessageQueue shadowMessageQueue = shadowOf(realObject.getQueue());
    if (realObject == Looper.getMainLooper() || RoboSettings.isUseGlobalScheduler()) {
      shadowMessageQueue.setScheduler(RuntimeEnvironment.getMasterScheduler());
    } else {
      shadowMessageQueue.setScheduler(new Scheduler());
    }
  }

  /** Causes all enqueued tasks to be discarded, and pause state to be reset */
  @Override
  public void reset() {
    shadowOf(realObject.getQueue()).reset();
    resetScheduler();

    quit = false;
  }

  /**
   * Returns the {@link org.robolectric.util.Scheduler} that is being used to manage the enqueued
   * tasks. This scheduler is managed by the Looper's associated queue.
   *
   * @return the {@link org.robolectric.util.Scheduler} that is being used to manage the enqueued
   *     tasks.
   */
  @Override
  public Scheduler getScheduler() {
    return shadowOf(realObject.getQueue()).getScheduler();
  }

  @Override
  public void runPaused(Runnable r) {
    boolean wasPaused = setPaused(true);
    try {
      r.run();
    } finally {
      if (!wasPaused) unPause();
    }
  }

  private static ShadowLegacyLooper shadowOf(Looper looper) {
    return Shadow.extract(looper);
  }

  private static ShadowMessageQueue shadowOf(MessageQueue mq) {
    return Shadow.extract(mq);
  }
}
