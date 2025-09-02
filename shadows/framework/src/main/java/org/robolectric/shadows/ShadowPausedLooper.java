package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Scheduler;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * The shadow Looper for {@link LooperMode.Mode#PAUSED} and {@link
 * LooperMode.Mode#INSTRUMENTATION_TEST}.
 *
 * <p>This shadow differs from the legacy {@link ShadowLegacyLooper} in the following ways:
 *
 * <ul>
 *   <li>Has no connection to {@link Scheduler}. Its APIs are standalone
 *   <li>The main looper is always paused in PAUSED MODE but can be unpaused in INSTRUMENTATION_TEST
 *       mode. When a looper is paused, posted messages to it are not executed unless {@link
 *       #idle()} is called.
 *   <li>Just like in real Android, each looper has its own thread, and posted tasks get executed in
 *       that thread.
 *   <li>There is only a single {@link SystemClock} value that all loopers read from. Unlike legacy
 *       behavior where each {@link Scheduler} kept their own clock value.
 * </ul>
 *
 * <p>This class should not be used directly; use {@link ShadowLooper} instead.
 */
@Implements(
    value = Looper.class,
    // turn off shadowOf generation.
    isInAndroidSdk = false)
@SuppressWarnings("NewApi")
public final class ShadowPausedLooper extends ShadowLooper {

  // Keep reference to all created Loopers so they can be torn down after test
  private static final Set<Looper> loopingLoopers =
      Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

  private static boolean ignoreUncaughtExceptions = false;

  @RealObject private Looper realLooper;

  private LooperControlService looperControlService;

  @Implementation
  protected void __constructor__(boolean quitAllowed) {
    invokeConstructor(Looper.class, realLooper, from(boolean.class, quitAllowed));

    loopingLoopers.add(realLooper);
    looperControlService = new LooperControlService(realLooper);
  }

  protected static Collection<Looper> getLoopers() {
    List<Looper> loopers = new ArrayList<>(loopingLoopers);
    return Collections.unmodifiableCollection(loopers);
  }

  @Override
  public void quitUnchecked() {
    throw new UnsupportedOperationException(
        "this action is not supported in " + looperMode() + " mode.");
  }

  @Override
  public boolean hasQuit() {
    throw new UnsupportedOperationException(
        "this action is not supported in " + looperMode() + " mode.");
  }

  @Override
  public void idle() {
    looperControlService.executeControlTask(new IdlingRunnable());
  }

  @Override
  public void idleFor(Duration idleForDuration) {
    looperControlService.executeControlTask(new IdleForRunnable(idleForDuration));
  }

  @Override
  public void idleFor(long time, TimeUnit timeUnit) {
    idleFor(Duration.of(time, timeUnit.toChronoUnit()));
  }

  @Override
  public boolean isIdle() {
    if (realLooper.getThread() == Thread.currentThread()) {
      // for accuracy, MessageQueue.isIdle won't be used here, because
      // it will return false if there is only a single sync barrier posted.
      // Which will cause busy-loops when called from idle(), since there is no actual executable
      // messages to be executed
      return !hasExecutableMessages();
    } else if (isPaused()) {
      // we'll take our chances with MessageQueue.isIdle. Calling isIdle from a non Looper thread
      // is going to be racy regardless
      return realLooper.getQueue().isIdle();
    } else {
      // attempt to detect case where a task is currently executing
      return realLooper.getQueue().isIdle() && shadowQueue().isPolling();
    }
  }

  private boolean hasExecutableMessages() {
    try (TestLooperManagerCompat lm = TestLooperManagerCompat.acquire(realLooper)) {
      Long peekWhen = lm.peekWhen();
      return peekWhen != null && peekWhen <= SystemClock.uptimeMillis();
    }
  }

  @Override
  public void unPause() {
    if (realLooper == Looper.getMainLooper()
        && looperMode() != LooperMode.Mode.INSTRUMENTATION_TEST) {
      throw new UnsupportedOperationException("main looper cannot be unpaused");
    }
    looperControlService.unpause();
  }

  @Override
  public void pause() {
    looperControlService.pause();
  }

  @Override
  public boolean isPaused() {
    return looperControlService.isPaused();
  }

  @Override
  public boolean setPaused(boolean shouldPause) {
    if (shouldPause) {
      pause();
    } else {
      unPause();
    }
    return true;
  }

  @Override
  public void resetScheduler() {
    throw new UnsupportedOperationException(
        "this action is not supported in " + looperMode() + " mode.");
  }

  @Override
  public void reset() {
    throw new UnsupportedOperationException(
        "this action is not supported in " + looperMode() + " mode.");
  }

  @Override
  public void idleIfPaused() {
    if (isPaused()) {
      idle();
    }
  }

  @Override
  public void idleConstantly(boolean shouldIdleConstantly) {
    throw new UnsupportedOperationException(
        "this action is not supported in " + looperMode() + " mode.");
  }

  @Override
  public void runToEndOfTasks() {
    idleFor(Duration.ofMillis(getLastScheduledTaskTime().toMillis() - SystemClock.uptimeMillis()));
  }

  @Override
  public void runUntilEmpty() {
    looperControlService.executeControlTask(new RunToEmptyRunnable());
  }

  @Override
  public void runToNextTask() {
    idleFor(Duration.ofMillis(getNextScheduledTaskTime().toMillis() - SystemClock.uptimeMillis()));
  }

  @Override
  public void runOneTask() {
    looperControlService.executeControlTask(new RunOneRunnable());
  }

  @Override
  public boolean post(Runnable runnable, long delayMillis) {
    return new Handler(realLooper).postDelayed(runnable, delayMillis);
  }

  @Override
  public boolean postAtFrontOfQueue(Runnable runnable) {
    return new Handler(realLooper).postAtFrontOfQueue(runnable);
  }

  /**
   * Posts the runnable to the looper and idles until the runnable has been run. Generally clients
   * should prefer to use {@link Instrumentation#runOnMainSync(Runnable)}, which will reraise
   * underlying runtime exceptions to the caller.
   */
  public void postSync(Runnable runnable) {
    looperControlService.executeControlTask(new PostAndIdleToRunnable(runnable));
  }

  /**
   * Posts the runnable as an asynchronous task and wait until it has been run. Ignores all
   * exceptions.
   *
   * <p>This method is similar to postSync, but used in internal cases where you want to make a best
   * effort quick attempt to execute the Runnable, and do not need to idle all the non-async tasks
   * that might be posted to the Looper's queue.
   */
  void postSyncQuiet(Runnable runnable) {
    try {
      looperControlService.executeControlTask(new PostAsyncAndIdleToRunnable(runnable));
    } catch (RuntimeException e) {
      Log.w("ShadowPausedLooper", "ignoring exception on postSyncQuiet", e);
    }
  }

  // this API doesn't make sense in LooperMode.PAUSED, but just retain it for backwards
  // compatibility for now
  @Override
  public void runPaused(Runnable runnable) {
    if (Thread.currentThread() == realLooper.getThread()) {
      // just run
      runnable.run();
    } else {
      throw new UnsupportedOperationException(
          "this method can only be called on " + realLooper.getThread().getName());
    }
  }

  /**
   * Polls the message queue waiting until a message is posted to the head of the queue. This will
   * suspend the thread until a new message becomes available. Returns immediately if the queue is
   * not idle. There's no guarantee that the message queue will not still be idle when returning,
   * but if the message queue becomes not idle it will return immediately.
   *
   * <p>This method is only applicable for the main looper's queue when called on the main thread,
   * as the main looper in Robolectric is processed manually (it doesn't loop)--looper threads are
   * using the native polling of their loopers. Throws an exception if called for another looper's
   * queue. Non-main thread loopers should use {@link #unPause()}.
   *
   * <p>This should be used with care, it can be used to suspend the main (i.e. test) thread while
   * worker threads perform some work, and then resumed by posting to the main looper. Used in a
   * loop to wait on some condition it can process messages on the main looper, simulating the
   * behavior of the real looper, for example:
   *
   * <pre>{@code
   * while (!condition) {
   *   shadowMainLooper.poll(timeout);
   *   shadowMainLooper.idle();
   * }
   * }</pre>
   *
   * <p>Beware though that a message must be posted to the main thread after the condition is
   * satisfied, or the condition satisfied while idling the main thread, otherwise the main thread
   * will continue to be suspended until the timeout.
   *
   * @param timeout Timeout in milliseconds, the maximum time to wait before returning, or 0 to wait
   *     indefinitely,
   */
  public void poll(long timeout) {
    checkState(Looper.myLooper() == Looper.getMainLooper() && Looper.myLooper() == realLooper);
    synchronized (realLooper.getQueue()) {
      if (isIdle()) {
        shadowQueue().poll(timeout);
      }
    }
  }

  @Override
  public Duration getNextScheduledTaskTime() {
    try (TestLooperManagerCompat testLooperManagerCompat =
        TestLooperManagerCompat.acquire(realLooper)) {
      Long nextWhen = testLooperManagerCompat.peekWhen();
      return nextWhen == null
          ? Duration.ZERO
          : Duration.ofMillis(ShadowPausedMessageQueue.convertWhenToScheduledTime(nextWhen));
    }
  }

  @Override
  public Duration getLastScheduledTaskTime() {
    return shadowQueue().getLastScheduledTaskTime();
  }

  @Resetter
  @SuppressWarnings("deprecation") // This is Robolectric library code
  public static synchronized void resetLoopers() {
    // Do not use looperMode() here, because its cached value might already have been reset
    LooperMode.Mode looperMode = ConfigurationRegistry.get(LooperMode.Mode.class);

    if (looperMode == LooperMode.Mode.LEGACY) {
      return;
    }

    createMainThreadAndLooperIfNecessary();
    ShadowPausedChoreographer.resetChoreographers();
    for (Looper looper : getLoopers()) {
      ShadowPausedLooper shadowPausedLooper = Shadow.extract(looper);
      shadowPausedLooper.resetLooperToInitialState();
    }
  }

  private static synchronized void createMainThreadAndLooperIfNecessary() {
    Looper mainLooper = Looper.getMainLooper();

    switch (ConfigurationRegistry.get(LooperMode.Mode.class)) {
      case INSTRUMENTATION_TEST:
        if (mainLooper == null) {
          ConditionVariable mainThreadPrepared = new ConditionVariable();
          Thread mainThread =
              new Thread(String.format("SDK %d Main Thread", RuntimeEnvironment.getApiLevel())) {
                @Override
                public void run() {
                  Looper.prepareMainLooper();
                  mainThreadPrepared.open();
                  // always be Looping.
                  // The shadow loop() method will handle any uncaught exceptions and block
                  // until reset happens
                  while (true) {
                    Looper.loop();
                  }
                }
              };
          mainThread.start();
          mainThreadPrepared.block();
          Thread.currentThread()
              .setName(String.format("SDK %d Test Thread", RuntimeEnvironment.getApiLevel()));
        }
        break;
      case PAUSED:
        if (Looper.myLooper() == null) {
          Looper.prepareMainLooper();
        }
        break;
      default:
        throw new UnsupportedOperationException(
            "Only supports INSTRUMENTATION_TEST and PAUSED LooperMode.");
    }
  }

  @VisibleForTesting
  synchronized void resetLooperToInitialState() {
    // Do not use looperMode() here, because its cached value might already have been reset
    LooperMode.Mode looperMode = ConfigurationRegistry.get(LooperMode.Mode.class);

    looperControlService.reset();
  }

  @Implementation
  protected static void prepareMainLooper() {
    reflector(LooperReflector.class).prepareMainLooper();
    ShadowPausedLooper pausedLooper = Shadow.extract(Looper.getMainLooper());
    pausedLooper.looperControlService.reset();
  }

  @Implementation
  protected void quit() {
    looperControlService.shutdown();
    loopingLoopers.remove(realLooper);
    reflector(LooperReflector.class, realLooper).quit();
  }

  @Implementation
  protected void quitSafely() {
    looperControlService.shutdown();
    loopingLoopers.remove(realLooper);
    reflector(LooperReflector.class, realLooper).quitSafely();
  }

  @Override
  public Scheduler getScheduler() {
    throw new UnsupportedOperationException(
        String.format("this action is not supported in %s mode.", looperMode()));
  }

  private static ShadowPausedMessage shadowMsg(Message msg) {
    return Shadow.extract(msg);
  }

  private ShadowPausedMessageQueue shadowQueue() {
    return Shadow.extract(realLooper.getQueue());
  }

  /** Retrieves the next message or null if the queue is idle. */
  private Message getNextExecutableMessage() {
    checkState(
        Thread.currentThread() == realLooper.getThread(),
        "getNextExecutableMessage is only supported from looper thread");
    try (TestLooperManagerCompat looperManager = TestLooperManagerCompat.acquire(realLooper)) {
      Long when = looperManager.peekWhen();
      if (when != null && when <= SystemClock.uptimeMillis()) {
        return looperManager.poll();
      }
      return null;
    }
  }

  /**
   * By default Robolectric will put Loopers that throw uncaught exceptions in their loop method
   * into an error state, where any future posting to the looper's queue will throw an error.
   *
   * <p>This API allows you to disable this behavior. Note this is a permanent setting - it is not
   * reset between tests.
   *
   * @deprecated this method only exists to accommodate legacy tests with preexisting issues.
   *     Silently discarding exceptions is not recommended, and can lead to deadlocks.
   */
  @Deprecated
  public static void setIgnoreUncaughtExceptions(boolean shouldIgnore) {
    ignoreUncaughtExceptions = shouldIgnore;
  }

  /**
   * Shadow loop to handle uncaught exceptions. Without this logic an uncaught exception on a looper
   * thread will cause idle() to deadlock.
   */
  @Implementation
  protected static void loop() {
    try {
      reflector(LooperReflector.class).loop();
    } catch (Exception e) {
      Looper realLooper = Objects.requireNonNull(Looper.myLooper());
      ShadowPausedMessageQueue shadowQueue = Shadow.extract(realLooper.getQueue());
      if (!ignoreUncaughtExceptions) {
        shadowQueue.setUncaughtException(e);
      }
      ShadowPausedLooper shadowLooper = Shadow.extract(realLooper);
      if (Looper.getMainLooper() == realLooper) {
        // Need to keep using same thread for main Looper, because a lot of code keeps static
        // references to the main Looper that persist across tests
        // Mark Looper as crashed, and block until looper is reset
        shadowLooper.looperControlService.crashed();
      } else {
        shadowLooper.looperControlService.shutdown();
        // TODO: add this statement. Some existing tests are dependent on clearing
        // uncaught exceptions, and should be fixed or at least migrated to ignoreUncaughtExceptions
        // loopingLoopers.remove(realLooper);
        throw e;
      }
    }
  }

  /**
   * If the given {@code lastMessageRead} is not null and the queue is now idle, get the idle
   * handlers and run them. This synchronization mirrors what happens in the real message queue
   * next() method, but does not block after running the idle handlers.
   */
  private void triggerIdleHandlersIfNeeded(Message lastMessageRead) {
    List<IdleHandler> idleHandlers;
    // Mirror the synchronization of MessageQueue.next(). If a message was read on the last call
    // to next() and the queue is now idle, make a copy of the idle handlers and release the lock.
    // Run the idle handlers without holding the lock, removing those that return false from their
    // queueIdle() method.
    synchronized (realLooper.getQueue()) {
      if (lastMessageRead == null || !realLooper.getQueue().isIdle()) {
        return;
      }
      idleHandlers = shadowQueue().getIdleHandlersCopy();
    }
    for (IdleHandler idleHandler : idleHandlers) {
      if (!idleHandler.queueIdle()) {
        // This method already has synchronization internally.
        realLooper.getQueue().removeIdleHandler(idleHandler);
      }
    }
  }

  private class RunToEmptyRunnable implements Runnable {
    private final IdlingRunnable idleRunnable = new IdlingRunnable();

    @Override
    public void run() {
      long nextScheduledTimeMs = getNextScheduledTaskTime().toMillis();
      while (nextScheduledTimeMs != 0) {
        ShadowSystemClock.advanceBy(
            Duration.ofMillis(nextScheduledTimeMs - SystemClock.uptimeMillis()));
        idleRunnable.run();
        nextScheduledTimeMs = getNextScheduledTaskTime().toMillis();
      }
    }
  }

  private class IdleForRunnable implements Runnable {
    private final Duration idleForDuration;
    private final IdlingRunnable idleRunnable = new IdlingRunnable();

    IdleForRunnable(Duration duration) {
      super();
      idleForDuration = duration;
    }

    @Override
    public void run() {
      long endingTimeMs = SystemClock.uptimeMillis() + idleForDuration.toMillis();
      long nextScheduledTimeMs = getNextScheduledTaskTime().toMillis();
      while (nextScheduledTimeMs != 0 && nextScheduledTimeMs <= endingTimeMs) {
        ShadowSystemClock.advanceBy(
            Duration.ofMillis(nextScheduledTimeMs - SystemClock.uptimeMillis()));
        idleRunnable.run();
        nextScheduledTimeMs = getNextScheduledTaskTime().toMillis();
      }
      ShadowSystemClock.advanceBy(Duration.ofMillis(endingTimeMs - SystemClock.uptimeMillis()));
      // the last SystemClock update might have added new tasks to the main looper via Choreographer
      // so idle once more.
      idleRunnable.run();
    }
  }

  private class IdlingRunnable implements Runnable {

    @Override
    public void run() {
      while (true) {
        Message msg = getNextExecutableMessage();
        if (msg == null) {
          break;
        }
        msg.getTarget().dispatchMessage(msg);
        shadowMsg(msg).recycleUnchecked();
        triggerIdleHandlersIfNeeded(msg);
      }
    }
  }

  private class RunOneRunnable implements Runnable {

    @Override
    public void run() {
      Message msg;
      try (TestLooperManagerCompat looperManager = TestLooperManagerCompat.acquire(realLooper)) {
        msg = looperManager.poll();
      }

      if (msg != null) {
        SystemClock.setCurrentTimeMillis(shadowMsg(msg).getWhen());
        msg.getTarget().dispatchMessage(msg);
        triggerIdleHandlersIfNeeded(msg);
      }
    }
  }

  /**
   * Control runnable that posts the provided runnable to the queue and then idles up to and
   * including the posted runnable. Provides essentially similar functionality to {@link
   * Instrumentation#runOnMainSync(Runnable)}.
   */
  private class PostAndIdleToRunnable implements Runnable {
    private final Runnable runnable;

    PostAndIdleToRunnable(Runnable runnable) {
      this.runnable = runnable;
    }

    @Override
    public void run() {
      new Handler(realLooper).post(runnable);
      Message msg;
      do {
        msg = getNextExecutableMessage();
        if (msg == null) {
          throw new IllegalStateException("Runnable is not in the queue");
        }
        msg.getTarget().dispatchMessage(msg);
        triggerIdleHandlersIfNeeded(msg);
      } while (msg.getCallback() != runnable);
    }
  }

  private class PostAsyncAndIdleToRunnable implements Runnable {
    private final Runnable runnable;
    private final Handler handler;

    PostAsyncAndIdleToRunnable(Runnable runnable) {
      this.runnable = runnable;
      this.handler = createAsyncHandler(realLooper);
    }

    @Override
    public void run() {
      handler.postAtFrontOfQueue(runnable);
      Message msg;
      do {
        msg = getNextExecutableMessage();
        if (msg == null) {
          throw new IllegalStateException("Runnable is not in the queue");
        }
        msg.getTarget().dispatchMessage(msg);

      } while (msg.getCallback() != runnable);
    }
  }


  static Handler createAsyncHandler(Looper looper) {
    if (RuntimeEnvironment.getApiLevel() >= 28) {
      // createAsync is only available in API 28+
      return Handler.createAsync(looper);
    } else {
      return new Handler(looper, null, true);
    }
  }

  @ForType(Looper.class)
  interface LooperReflector {

    @Static
    @Direct
    void prepareMainLooper();

    @Direct
    void quit();

    @Direct
    void quitSafely();

    @Direct
    void loop();

    @Accessor("mThread")
    void setThread(Thread thread);
  }
}
