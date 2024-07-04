package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Instrumentation;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
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
 * The shadow Looper for {@link LooperMode.Mode.PAUSED and @link
 * LooperMode.Mode.INSTRUMENTATION_TEST}.
 *
 * <p>This shadow differs from the legacy {@link ShadowLegacyLooper} in the following ways:\ - Has
 * no connection to {@link org.robolectric.util.Scheduler}. Its APIs are standalone - The main
 * looper is always paused in PAUSED MODE but can be unpaused in INSTRUMENTATION_TEST mode. When a
 * looper is paused, posted messages to it are not executed unless {@link #idle()} is called. - Just
 * like in real Android, each looper has its own thread, and posted tasks get executed in that
 * thread. - - There is only a single {@link SystemClock} value that all loopers read from. Unlike
 * legacy behavior where each {@link org.robolectric.util.Scheduler} kept their own clock value.
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
  private static Set<Looper> loopingLoopers =
      Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<Looper, Boolean>()));

  private static boolean ignoreUncaughtExceptions = false;

  @RealObject private Looper realLooper;
  private boolean isPaused = false;
  // the Executor that executes looper messages. Must be written to on looper thread
  private Executor looperExecutor;

  @Implementation
  protected void __constructor__(boolean quitAllowed) {
    invokeConstructor(Looper.class, realLooper, from(boolean.class, quitAllowed));

    loopingLoopers.add(realLooper);
    looperExecutor = new HandlerExecutor(realLooper);
  }

  protected static Collection<Looper> getLoopers() {
    List<Looper> loopers = new ArrayList<>(loopingLoopers);
    return Collections.unmodifiableCollection(loopers);
  }

  @Override
  public void quitUnchecked() {
    throw new UnsupportedOperationException(
        "this action is not" + " supported" + " in " + looperMode() + " mode.");
  }

  @Override
  public boolean hasQuit() {
    throw new UnsupportedOperationException(
        "this action is not" + " supported" + " in " + looperMode() + " mode.");
  }

  @Override
  public void idle() {
    executeOnLooper(new IdlingRunnable());
  }

  @Override
  public void idleFor(long time, TimeUnit timeUnit) {
    long endingTimeMs = SystemClock.uptimeMillis() + timeUnit.toMillis(time);
    long nextScheduledTimeMs = getNextScheduledTaskTime().toMillis();
    while (nextScheduledTimeMs != 0 && nextScheduledTimeMs <= endingTimeMs) {
      SystemClock.setCurrentTimeMillis(nextScheduledTimeMs);
      idle();
      nextScheduledTimeMs = getNextScheduledTaskTime().toMillis();
    }
    SystemClock.setCurrentTimeMillis(endingTimeMs);
    // the last SystemClock update might have added new tasks to the main looper via Choreographer
    // so idle once more.
    idle();
  }

  @Override
  public boolean isIdle() {
    if (Thread.currentThread() == realLooper.getThread() || isPaused) {
      return shadowQueue().isIdle();
    } else {
      return shadowQueue().isIdle() && shadowQueue().isPolling();
    }
  }

  @Override
  public void unPause() {
    if (realLooper == Looper.getMainLooper()
        && looperMode() != LooperMode.Mode.INSTRUMENTATION_TEST) {
      throw new UnsupportedOperationException("main looper cannot be unpaused");
    }
    executeOnLooper(new UnPauseRunnable());
  }

  @Override
  public void pause() {
    if (!isPaused()) {
      executeOnLooper(new PausedLooperExecutor());
    }
  }

  @Override
  public boolean isPaused() {
    return isPaused;
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
        "this action is not" + " supported" + " in " + looperMode() + " mode.");
  }

  @Override
  public void reset() {
    throw new UnsupportedOperationException(
        "this action is not" + " supported" + " in " + looperMode() + " mode.");
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
        "this action is not" + " supported" + " in " + looperMode() + " mode.");
  }

  @Override
  public void runToEndOfTasks() {
    idleFor(Duration.ofMillis(getLastScheduledTaskTime().toMillis() - SystemClock.uptimeMillis()));
  }

  @Override
  public void runToNextTask() {
    idleFor(Duration.ofMillis(getNextScheduledTaskTime().toMillis() - SystemClock.uptimeMillis()));
  }

  @Override
  public void runOneTask() {
    executeOnLooper(new RunOneRunnable());
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
    executeOnLooper(new PostAndIdleToRunnable(runnable));
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
      executeOnLooper(new PostAsyncAndIdleToRunnable(runnable));
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
    shadowQueue().poll(timeout);
  }

  @Override
  public Duration getNextScheduledTaskTime() {
    return shadowQueue().getNextScheduledTaskTime();
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

    createMainThreadAndLooperIfNotAlive();
    for (Looper looper : getLoopers()) {
      ShadowPausedLooper shadowPausedLooper = Shadow.extract(looper);
      shadowPausedLooper.resetLooperToInitialState();
    }
    ShadowPausedChoreographer.clearLoopers();
  }

  private static final Object instrumentationTestMainThreadLock = new Object();

  @SuppressWarnings("NonFinalStaticField") // State used in static method for main thread.
  @GuardedBy("instrumentationTestMainThreadLock")
  private static boolean instrumentationTestMainThreadShouldRestart = false;

  private static synchronized void createMainThreadAndLooperIfNotAlive() {
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
                  while (true) {
                    try {
                      Looper.loop();
                    } catch (Throwable e) {
                      // The exception is handled inside of the loop shadow method, so ignore it.
                    }
                    // Wait to restart the looper until the looper is reset.
                    synchronized (instrumentationTestMainThreadLock) {
                      while (!instrumentationTestMainThreadShouldRestart) {
                        try {
                          instrumentationTestMainThreadLock.wait();
                        } catch (InterruptedException ie) {
                          // Shouldn't be interrupted, continue waiting for reset signal.
                        }
                      }
                      instrumentationTestMainThreadShouldRestart = false;
                    }
                  }
                }
              };
          mainThread.start();
          mainThreadPrepared.block();
          Thread.currentThread()
              .setName(String.format("SDK %d Test Thread", RuntimeEnvironment.getApiLevel()));
        } else {
          ShadowPausedMessageQueue shadowQueue = Shadow.extract(mainLooper.getQueue());
          if (shadowQueue.hasUncaughtException()) {
            shadowQueue.reset();
            synchronized (instrumentationTestMainThreadLock) {
              // If the looper died in a previous test it will be waiting to restart, notify it.
              instrumentationTestMainThreadShouldRestart = true;
              instrumentationTestMainThreadLock.notify();
            }
          }
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

    ShadowPausedMessageQueue shadowQueue = Shadow.extract(realLooper.getQueue());
    shadowQueue.reset();

    if (realLooper.getThread().isAlive()
        && !shadowQueue
            .isQuitting()) { // Trying to unpause a quitted background Looper may deadlock.

      if (isPaused()
          && !(realLooper == Looper.getMainLooper() && looperMode != Mode.INSTRUMENTATION_TEST)) {
        unPause();
      }
      ShadowPausedChoreographer.reset(realLooper);
    }
  }

  @Implementation
  protected static void prepareMainLooper() {
    reflector(LooperReflector.class).prepareMainLooper();
    ShadowPausedLooper pausedLooper = Shadow.extract(Looper.getMainLooper());
    pausedLooper.isPaused = looperMode() == Mode.PAUSED;
  }

  @Implementation
  protected void quit() {
    if (isPaused()) {
      executeOnLooper(new UnPauseRunnable());
    }
    synchronized (realLooper.getQueue()) {
      drainQueueSafely(shadowQueue());
      reflector(LooperReflector.class, realLooper).quit();
    }
  }

  @Implementation
  protected void quitSafely() {
    if (isPaused()) {
      executeOnLooper(new UnPauseRunnable());
    }
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

  private void setLooperExecutor(Executor executor) {
    looperExecutor = executor;
  }

  /** Retrieves the next message or null if the queue is idle. */
  private Message getNextExecutableMessage() {
    synchronized (realLooper.getQueue()) {
      // Use null if the queue is idle, otherwise getNext() will block.
      return shadowQueue().isIdle() ? null : shadowQueue().getNext();
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
      Looper realLooper = Preconditions.checkNotNull(Looper.myLooper());
      ShadowPausedMessageQueue shadowQueue = Shadow.extract(realLooper.getQueue());

      if (ignoreUncaughtExceptions) {
        // ignore
      } else {
        synchronized (realLooper.getQueue()) {
          shadowQueue.setUncaughtException(e);
          drainQueueSafely(shadowQueue);
        }
      }
      if (e instanceof ControlException) {
        ((ControlException) e).rethrowCause();
      } else {
        throw e;
      }
    }
  }

  private static void drainQueueSafely(ShadowPausedMessageQueue shadowQueue) {
    // release any ControlRunnables currently in queue to prevent deadlocks
    shadowQueue.drainQueue(
        input -> {
          if (input instanceof ControlRunnable) {
            ((ControlRunnable) input).runLatch.countDown();
            return true;
          }
          return false;
        });
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
      if (lastMessageRead == null || !shadowQueue().isIdle()) {
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

  /**
   * An exception raised by a {@link ControlRunnable} if the runnable was interrupted with an
   * exception. The looper must call {@link #rethrowCause()} after performing cleanup associated
   * with handling the exception.
   */
  private static final class ControlException extends RuntimeException {
    private final ControlRunnable controlRunnable;

    ControlException(ControlRunnable controlRunnable, RuntimeException cause) {
      super(cause);
      this.controlRunnable = controlRunnable;
    }

    void rethrowCause() {
      // Release the control runnable only once the looper has finished draining to avoid any
      // races on the thread that posted the control runnable (otherwise the calling thread may
      // have subsequent interactions with the looper that result in inconsistent state).
      controlRunnable.runLatch.countDown();
      throw (RuntimeException) getCause();
    }
  }

  /** A runnable that changes looper state, and that must be run from looper's thread */
  private abstract static class ControlRunnable implements Runnable {

    protected final CountDownLatch runLatch = new CountDownLatch(1);
    private volatile RuntimeException exception;

    @Override
    public void run() {
      boolean controlExceptionThrown = false;
      try {
        doRun();
      } catch (RuntimeException e) {
        if (!ignoreUncaughtExceptions) {
          exception = e;
        }
        controlExceptionThrown = true;
        throw new ControlException(this, e);
      } finally {
        if (!controlExceptionThrown) {
          runLatch.countDown();
        }
      }
    }

    protected abstract void doRun() throws RuntimeException;

    public void waitTillComplete() throws RuntimeException {
      try {
        runLatch.await();
      } catch (InterruptedException e) {
        Log.w("ShadowPausedLooper", "wait till idle interrupted");
      }
      if (exception != null) {
        throw exception;
      }
    }
  }

  private class IdlingRunnable extends ControlRunnable {

    @Override
    public void doRun() {
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

  private class RunOneRunnable extends ControlRunnable {

    @Override
    public void doRun() {

      Message msg = shadowQueue().getNextIgnoringWhen();
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
  private class PostAndIdleToRunnable extends ControlRunnable {
    private final Runnable runnable;

    PostAndIdleToRunnable(Runnable runnable) {
      this.runnable = runnable;
    }

    @Override
    public void doRun() {
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

  private class PostAsyncAndIdleToRunnable extends ControlRunnable {
    private final Runnable runnable;
    private final Handler handler;

    PostAsyncAndIdleToRunnable(Runnable runnable) {
      this.runnable = runnable;
      this.handler = createAsyncHandler(realLooper);
    }

    @Override
    public void doRun() {
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

  /**
   * Executes the given runnable on the loopers thread, and waits for it to complete.
   *
   * @throws IllegalStateException if Looper is quitting or has stopped due to uncaught exception
   */
  private void executeOnLooper(ControlRunnable runnable) throws IllegalStateException {
    checkState(!shadowQueue().isQuitting(), "Looper is quitting");
    if (Thread.currentThread() == realLooper.getThread()) {
      if (runnable instanceof UnPauseRunnable) {
        // Need to trigger the unpause action in PausedLooperExecutor
        looperExecutor.execute(runnable);
      } else {
        try {
          runnable.run();
        } catch (ControlException e) {
          e.rethrowCause();
        }
      }
    } else {
      if (looperMode() == LooperMode.Mode.PAUSED && realLooper.equals(Looper.getMainLooper())) {
        throw new UnsupportedOperationException(
            "main looper can only be controlled from main thread");
      }
      looperExecutor.execute(runnable);
      runnable.waitTillComplete();
      // throw immediately if looper died while executing tasks
      shadowQueue().checkQueueState();
    }
  }

  /**
   * A runnable that will block normal looper execution of messages aka will 'pause' the looper.
   *
   * <p>Message execution can be triggered by posting messages to this runnable.
   */
  private class PausedLooperExecutor extends ControlRunnable implements Executor {

    private final LinkedBlockingQueue<Runnable> executionQueue = new LinkedBlockingQueue<>();

    @Override
    public void execute(Runnable runnable) {
      shadowQueue().checkQueueState();
      executionQueue.add(runnable);
    }

    @Override
    public void run() {
      setLooperExecutor(this);
      isPaused = true;
      runLatch.countDown();
      while (isPaused) {
        try {
          Runnable runnable = executionQueue.take();
          runnable.run();
        } catch (InterruptedException e) {
          // ignored
        }
      }
    }

    @Override
    protected void doRun() throws RuntimeException {
      throw new UnsupportedOperationException();
    }
  }

  private class UnPauseRunnable extends ControlRunnable {
    @Override
    public void doRun() {
      setLooperExecutor(new HandlerExecutor(realLooper));
      isPaused = false;
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

  private static class HandlerExecutor implements Executor {
    private final Handler handler;

    private HandlerExecutor(Looper looper) {
      // always post async messages so ControlRunnables get processed even if Looper is blocked on a
      // sync barrier
      this.handler = createAsyncHandler(looper);
    }

    @Override
    public void execute(Runnable runnable) {
      if (!handler.post(runnable)) {
        throw new IllegalStateException(
            String.format("post to %s failed. Is handler thread dead?", handler));
      }
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

    @Accessor("sThreadLocal")
    ThreadLocal<Looper> getThreadLocal();
  }
}
