package org.robolectric.android.util.concurrent;

import androidx.annotation.NonNull;
import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractFuture;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.LooperMode;
import org.robolectric.util.Logger;

/**
 * Executor service that queues any posted tasks.
 *
 * Users must explicitly call {@link runAll()} to execute all pending tasks.
 *
 * Intended to be a replacement for {@link RoboExecutorService} when using
 * {@link LooperMode.Mode#PAUSED}.
 * Unlike {@link RoboExecutorService}, will execute tasks on a background thread. This is useful
 * to test Android code that enforces it runs off the main thread.
 *
 * NOTE: Beta API, subject to change.
 */
@Beta
public class PausedExecutorService extends AbstractExecutorService {

  /**
   * Run given callable on the given executor and try to preserve original exception if possible.
   */
  static <T> T getFutureResultWithExceptionPreserved(Future<T> future) {
    try {
      return future.get();
    } catch (ExecutionException e) {
      // try to preserve original exception if possible
      Throwable cause = e.getCause();
      if (cause == null) {
        throw new RuntimeException(e);
      } else if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(cause);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private final ExecutorService realService;
  private final Queue<Runnable> deferredTasks = new ConcurrentLinkedQueue<>();
  private Thread executorThread;

  private static class DeferredTask<V> extends AbstractFuture<V> implements RunnableFuture<V> {

    private final Callable<V> callable;
    private final ExecutorService executor;

    DeferredTask(Callable<V> callable, ExecutorService executor) {
      this.callable = callable;
      this.executor = executor;
    }

    @Override
    public void run() {
      Future<V> future = executor.submit(callable);
      set(getFutureResultWithExceptionPreserved(future));
    }
  }

  public PausedExecutorService() {
    this.realService =
        Executors.newSingleThreadExecutor(
            r -> {
              executorThread = new Thread(r);
              return executorThread;
            });
  }

  /**
   * Execute all posted tasks and block until they are complete.
   *
   * @return the number of tasks executed
   */
  public int runAll() {
    int numTasksRun = 0;
    if (Thread.currentThread().equals(executorThread)) {
      Logger.info("ignoring request to execute task - called from executor's own thread");
      return numTasksRun;
    }
    while (hasQueuedTasks()) {
      runNext();
      numTasksRun++;
    }
    return numTasksRun;
  }

  /**
   * Executes the next queued task.
   *
   * Will be ignored if called from the executor service thread to prevent deadlocks.
   *
   * @return true if task was run, false if queue was empty
   */
  public boolean runNext() {
    if (!hasQueuedTasks()) {
      return false;
    }
    if (Thread.currentThread().equals(executorThread)) {
      Logger.info("ignoring request to execute task - called from executor's own thread");
      return false;
    }
    Runnable task = deferredTasks.poll();
    task.run();
    return true;
  }

  /**
   * @return true if there are queued pending tasks
   */
  public boolean hasQueuedTasks() {
    return !deferredTasks.isEmpty();
  }

  @Override
  public void shutdown() {
    realService.shutdown();
    deferredTasks.clear();
  }

  @Override
  public List<Runnable> shutdownNow() {
    realService.shutdownNow();
    List<Runnable> copy = ImmutableList.copyOf(deferredTasks);
    deferredTasks.clear();
    return copy;
  }

  @Override
  public boolean isShutdown() {
    return realService.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return realService.isTerminated();
  }

  @Override
  public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
    // If not shut down first, timeout would occur with normal behavior.
    return realService.awaitTermination(l, timeUnit);
  }

  @Override
  public void execute(@NonNull Runnable command) {
    if (command instanceof DeferredTask) {
      deferredTasks.add(command);
    } else {
      deferredTasks.add(new DeferredTask<>(Executors.callable(command), realService));
    }
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
    return newTaskFor(Executors.callable(runnable, value));
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
    return new DeferredTask<T>(callable, realService);
  }
}
