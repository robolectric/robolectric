package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
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
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Scheduler;

/**
 * The shadow Looper for {@link LooperMode.Mode.PAUSED}.
 *
 * <p>This shadow differs from the legacy {@link ShadowLegacyLooper} in the following ways:\ - Has
 * no connection to {@link org.robolectric.util.Scheduler}. Its APIs are standalone - The main
 * looper is always paused. Posted messages are not executed unless {@link #idle()} is called. -
 * Just like in real Android, each looper has its own thread, and posted tasks get executed in that
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

  @RealObject private Looper realLooper;
  private boolean isPaused = false;
  // the Executor that executes looper messages. Must be written to on looper thread
  private Executor looperExecutor;

  @Implementation
  protected void __constructor__(boolean quitAllowed) {
    invokeConstructor(Looper.class, realLooper, from(boolean.class, quitAllowed));

    loopingLoopers.add(realLooper);
    looperExecutor = new HandlerExecutor(new Handler(realLooper));
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
    if (realLooper == Looper.getMainLooper()) {
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
    idle();
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

  // this API doesn't make sense in LooperMode.PAUSED, but just retain it for backwards
  // compatibility for now
  @Override
  public void runPaused(Runnable runnable) {
    if (isPaused && Thread.currentThread() == realLooper.getThread()) {
      // just run
      runnable.run();
    } else {
      throw new UnsupportedOperationException();
    }
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
  public static synchronized void resetLoopers() {
    if (looperMode() != LooperMode.Mode.PAUSED) {
      // ignore if not realistic looper
      return;
    }

    Collection<Looper> loopersCopy = new ArrayList(loopingLoopers);
    for (Looper looper : loopersCopy) {
      ShadowPausedMessageQueue shadowQueue = Shadow.extract(looper.getQueue());
      shadowQueue.reset();
    }
  }

  @Implementation
  protected static void prepareMainLooper() {
    directlyOn(Looper.class, "prepareMainLooper");
    ShadowPausedLooper pausedLooper = Shadow.extract(Looper.getMainLooper());
    pausedLooper.isPaused = true;
  }

  @Implementation
  protected void quit() {
    if (isPaused()) {
      executeOnLooper(new UnPauseRunnable());
    }
    directlyOn(realLooper, Looper.class, "quit");
  }

  @Implementation(minSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
  protected void quitSafely() {
    if (isPaused()) {
      executeOnLooper(new UnPauseRunnable());
    }
    directlyOn(realLooper, Looper.class, "quitSafely");
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

  /** Executes the given runnable on the loopers thread, and waits for it to complete. */
  private void executeOnLooper(ControlRunnable runnable) {
    if (Thread.currentThread() == realLooper.getThread()) {
      runnable.run();
    } else {
      if (realLooper.equals(Looper.getMainLooper())) {
        throw new UnsupportedOperationException(
            "main looper can only be controlled from main thread");
      }
      looperExecutor.execute(runnable);
      runnable.waitTillComplete();
    }
  }

  private void setLooperExecutor(Executor executor) {
    looperExecutor = executor;
  }

  /** A runnable that changes looper state, and that must be run from looper's thread */
  private abstract static class ControlRunnable implements Runnable {

    protected final CountDownLatch runLatch = new CountDownLatch(1);

    public void waitTillComplete() {
      try {
        runLatch.await();
      } catch (InterruptedException e) {
        Log.w("ShadowPausedLooper", "wait till idle interrupted");
      }
    }
  }

  private class IdlingRunnable extends ControlRunnable {

    @Override
    public void run() {
      while (!shadowQueue().isIdle()) {
        Message msg = shadowQueue().getNext();
        msg.getTarget().dispatchMessage(msg);
        shadowMsg(msg).recycleUnchecked();
      }
      runLatch.countDown();
    }
  }

  private class RunOneRunnable extends ControlRunnable {

    @Override
    public void run() {
      Message msg = shadowQueue().poll();
      if (msg != null) {
        SystemClock.setCurrentTimeMillis(shadowMsg(msg).getWhen());
        msg.getTarget().dispatchMessage(msg);
      }
      runLatch.countDown();
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
      executionQueue.add(runnable);
    }

    @Override
    public void run() {
      setLooperExecutor(this);
      isPaused = true;
      runLatch.countDown();
      while (true) {
        try {
          Runnable runnable = executionQueue.take();
          runnable.run();
          if (runnable instanceof UnPauseRunnable) {
            setLooperExecutor(new HandlerExecutor(new Handler(realLooper)));
            return;
          }
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }
  }

  private class UnPauseRunnable extends ControlRunnable {
    @Override
    public void run() {
      isPaused = false;
      runLatch.countDown();
    }
  }

  private static class HandlerExecutor implements Executor {
    private final Handler handler;

    private HandlerExecutor(Handler handler) {
      this.handler = handler;
    }

    @Override
    public void execute(Runnable runnable) {
      if (!handler.post(runnable)) {
        throw new IllegalStateException(
            String.format("post to %s failed. Is handler thread dead?", handler));
      }
    }
  }
}
