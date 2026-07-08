package org.robolectric.shadows;

import static android.os.Looper.getMainLooper;
import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.annotation.LooperMode.Mode.INSTRUMENTATION_TEST;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;
import static org.robolectric.shadows.ShadowLooper.looperMode;

import android.os.Handler;
import android.os.Looper;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.shadow.api.Shadow;

final class LooperControlService {

  private final Executor looperExecutor;
  private final Looper looper;

  // Lock to control access to state change operations
  private final Object lock = new Object();

  enum State {
    // Looper is blocked and will not execute messages unless explicitly done via control operations
    PAUSED,
    // Looper is free-running
    UNPAUSED,
    // Looper has been shutdown. No further interaction will be allowed
    SHUTDOWN,
    // Looper has crashed due to an uncaught exception, and is blocked waiting for the next test to
    // start and reinitialize the Looper. This state is currently only applicable on the main
    // Looper when in LooperMode.INSTRUMENTATION_TEST
    CRASHED
  }

  @GuardedBy("lock")
  private State state = State.UNPAUSED;

  // The queue of tasks that control the Looper.
  // In PAUSED mode, the Looper thread will be blocked waiting for tasks to be posted to this queue.
  // Adding to this queue should synchronize on lock and check the state.
  // This is needed to ensure that shutdown or init operations cancels those operations
  private final LinkedBlockingQueue<CancelableRunnableFuture> controlQueue =
      new LinkedBlockingQueue<>();

  LooperControlService(Looper looper) {
    this.looperExecutor = new HandlerExecutor(looper);
    this.looper = looper;
  }

  /** Reset the Looper to its initial state. */
  public void reset() {
    List<CancelableRunnableFuture> pendingTasks = new ArrayList<>();
    ShadowPausedMessageQueue messageQueue = Shadow.extract(looper.getQueue());
    messageQueue.reset();
    synchronized (lock) {
      if (state == State.SHUTDOWN) {
        // Looper has been shutdown, just ignore
        // pending tasks should already have been cleared
        return;
      }
      pendingTasks.addAll(controlQueue);
      controlQueue.clear();

      if (looper == getMainLooper() && looperMode() == PAUSED) {
        state = State.PAUSED;
      } else {
        state = State.UNPAUSED;
      }
    }

    // this is unlikely this will be non empty. but clear and release all pending control tasks
    for (RunnableFuture<Void> rf : pendingTasks) {
      rf.cancel(false);
    }
    // add an empty task to controlQueue to wake up PAUSE or CRASHED state
    controlQueue.add(new EmptyCancelableFuture());
    if (looper == Looper.getMainLooper() && looperMode() == INSTRUMENTATION_TEST) {
      // force a task to run synchronously to ensure any potential pending crashes are cleared
      executeControlTask(new InitRunnableForMainLooper(), false);
    }
  }

  private class InitRunnableForMainLooper implements Runnable {

    @Override
    public void run() {
      synchronized (lock) {
        state = State.UNPAUSED;
        ShadowPausedMessageQueue messageQueue = Shadow.extract(looper.getQueue());
        messageQueue.reset();
      }
    }
  }

  public boolean isPaused() {
    synchronized (lock) {
      return state == State.PAUSED;
    }
  }

  void pause() {
    PauseStartedNotifier pauseStartedNotifier = new PauseStartedNotifier();
    synchronized (lock) {
      if (state == State.PAUSED) {
        return;
      }
      checkState(state == State.UNPAUSED);
      // need to also add pauseStartedNotifier to controlQueue so it can be canceled
      controlQueue.add(pauseStartedNotifier);
      looperExecutor.execute(new PauseRunnable(pauseStartedNotifier));
    }
    getAndThrow(pauseStartedNotifier);
  }

  private static class PauseStartedNotifier extends AbstractFuture<Void>
      implements CancelableRunnableFuture {
    @SuppressWarnings("NullArgumentForNonNullParameter")
    boolean markDone() {
      return super.set(null);
    }

    @Override
    public void run() {
      // ignore
    }

    @Override
    public boolean isCancelable() {
      return true;
    }
  }

  //
  void unpause() {
    checkState(looper != getMainLooper() || looperMode() == Mode.INSTRUMENTATION_TEST);
    executeControlTask(
        () -> {
          synchronized (lock) {
            checkState(
                state != State.SHUTDOWN,
                "unpause of %s failed: Looper is shutdown",
                Thread.currentThread().getName());
            state = State.UNPAUSED;
          }
        });
  }

  void executeControlTask(Runnable runnable) {
    executeControlTask(runnable, true);
  }

  void executeControlTask(Runnable runnable, boolean cancelable) {
    if (looper.getThread() == Thread.currentThread()) {
      synchronized (lock) {
        checkState(state != State.SHUTDOWN);
      }
      runnable.run();
    } else {
      checkState(
          looper != getMainLooper() || looperMode() == Mode.INSTRUMENTATION_TEST,
          "Main looper can only be controlled from its thread in PAUSED mode");
      PropagatingRunnableFuture task = PropagatingRunnableFuture.create(runnable, cancelable);
      task.addListener(() -> controlQueue.remove(task), MoreExecutors.directExecutor());
      synchronized (lock) {
        checkState(state != State.SHUTDOWN);
        controlQueue.add(task);
        if (state == State.UNPAUSED) {
          looperExecutor.execute(task);
        }
      }
      getAndThrow(task);
    }
    // throw immediately if looper died while executing tasks
    ShadowPausedMessageQueue sq = Shadow.extract(looper.getQueue());
    sq.checkQueueState();
  }

  private static void getAndThrow(Future<?> task) {
    try {
      task.get();
    } catch (CancellationException e) {
      // ignore
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore the interrupted status
    } catch (ExecutionException e) {
      if (e.getCause() instanceof RuntimeException) {
        throw (RuntimeException) e.getCause();
      }
      throw new RuntimeException(e.getCause());
    }
  }

  public void shutdown() {
    List<CancelableRunnableFuture> pendingTasks = new ArrayList<>();

    synchronized (lock) {
      checkState(state != State.CRASHED);
      if (state == State.SHUTDOWN) {
        return;
      }
      pendingTasks.addAll(controlQueue);
      controlQueue.clear();
      state = State.SHUTDOWN;
      // add an empty task to controlQueue to wake up PauseRunnable if necessary
      controlQueue.add(new EmptyCancelableFuture());
    }
    for (Future<?> future : pendingTasks) {
      future.cancel(true);
    }
  }

  /**
   * Mark Looper as CRASHED due to uncaught exception.
   *
   * <p>This method will block until state changes
   */
  public void crashed() {
    checkState(getMainLooper().isCurrentThread());
    synchronized (lock) {
      checkState(state != State.SHUTDOWN && state != State.CRASHED);
      state = State.CRASHED;
    }
    while (isCrashed()) {
      try {
        // init() will post a task here to unblock
        CancelableRunnableFuture task = controlQueue.take();
        if (task.isCancelable()) {
          task.cancel(true);
        } else {
          task.run();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private boolean isCrashed() {
    synchronized (lock) {
      return state == State.CRASHED;
    }
  }

  /**
   * A RunnableFuture that indicates whether it is cancelable.
   *
   * <p>An uncancelable Runnable is one that should always be run when recovering from crashed
   * state. Currently this only is used for the init() case - the task that will force the Looper
   * back to its initial state.
   */
  interface CancelableRunnableFuture extends RunnableFuture<Void> {
    boolean isCancelable();
  }

  private static class EmptyCancelableFuture extends FutureTask<Void>
      implements CancelableRunnableFuture {
    private EmptyCancelableFuture() {
      super(() -> {}, null);
    }

    @Override
    public boolean isCancelable() {
      return true;
    }
  }

  /** A RunnableFuture that will throw any exceptions on current thread. */
  private static class PropagatingRunnableFuture
      extends ForwardingListenableFuture.SimpleForwardingListenableFuture<Void>
      implements CancelableRunnableFuture {

    private final boolean cancelable;

    private PropagatingRunnableFuture(ListenableFuture<Void> delegateTask, boolean cancelable) {
      super(delegateTask);
      this.cancelable = cancelable;
    }

    @SuppressWarnings("NullArgumentForNonNullParameter")
    static PropagatingRunnableFuture create(Runnable runnable, boolean cancelable) {
      return new PropagatingRunnableFuture(ListenableFutureTask.create(runnable, null), cancelable);
    }

    @Override
    public void run() {
      ((Runnable) delegate()).run();
      // want to rethrow any exception on the current Looper thread
      getAndThrow(this);
    }

    @Override
    public boolean isCancelable() {
      return cancelable;
    }
  }

  private class PauseRunnable implements Runnable {
    private final PauseStartedNotifier waitForPausedFuture;

    private PauseRunnable(PauseStartedNotifier waitForPausedFuture) {
      this.waitForPausedFuture = waitForPausedFuture;
    }

    @Override
    public void run() {
      synchronized (lock) {
        checkState(state == State.UNPAUSED);
        state = State.PAUSED;
      }
      checkState(waitForPausedFuture.markDone());
      while (isPaused()) {
        Runnable task = Uninterruptibles.takeUninterruptibly(controlQueue);
        task.run();
      }
    }
  }

  private static class HandlerExecutor implements Executor {
    private final Handler handler;

    private HandlerExecutor(Looper looper) {
      // always post async so control tasks  get processed even if Looper is blocked on a
      // sync barrier
      this.handler = ShadowPausedLooper.createAsyncHandler(looper);
    }

    @Override
    public void execute(@Nonnull Runnable runnable) {
      if (handler.getLooper().getThread() == Thread.currentThread()) {
        runnable.run();
      } else {
        if (!handler.post(runnable)) {
          throw new IllegalStateException(
              String.format("post to %s failed. Is handler thread dead?", handler));
        }
      }
    }
  }
}
