package org.robolectric.android.util.concurrent;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.Scheduler;

/**
 * Executor service that runs all operations on the background scheduler.
 *
 * @deprecated only works when used in conjunction with the deprecated {@link
 *     LooperMode.Mode#LEGACY} mode. Consider using guava's {@link MoreExecutors#directExecutor()}
 *     or {@link PausedExecutorService} or {@link InlineExecutorService}.
 */
@Deprecated
public class RoboExecutorService implements ExecutorService {
  private final Scheduler scheduler;
  private boolean isShutdown;
  private final HashSet<Runnable> runnables = new HashSet<>();

  static class AdvancingFutureTask<V> extends FutureTask<V> {
    private final Scheduler scheduler;

    public AdvancingFutureTask(Scheduler scheduler, Callable<V> callable) {
      super(callable);
      this.scheduler = scheduler;
    }

    public AdvancingFutureTask(Scheduler scheduler, Runnable runnable, V result) {
      super(runnable, result);
      this.scheduler = scheduler;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
      while (!isDone()) {
        scheduler.advanceToNextPostedRunnable();
      }
      return super.get();
    }
  }

  public RoboExecutorService() {
    this.scheduler = ShadowApplication.getInstance().getBackgroundThreadScheduler();
  }

  @Override
  public void shutdown() {
    shutdownNow();
  }

  @Nonnull
  @Override
  public List<Runnable> shutdownNow() {
    isShutdown = true;
    List<Runnable> notExecutedRunnables = new ArrayList<>();
    for (Runnable runnable : runnables) {
      scheduler.remove(runnable);
      notExecutedRunnables.add(runnable);
    }
    runnables.clear();
    return notExecutedRunnables;
  }

  @Override
  public boolean isShutdown() {
    return isShutdown;
  }

  @Override
  public boolean isTerminated() {
    return isShutdown;
  }

  @Override
  public boolean awaitTermination(long l, @Nonnull TimeUnit timeUnit) throws InterruptedException {
    // If not shut down first, timeout would occur with normal behavior.
    return isShutdown();
  }

  @Nonnull
  @Override
  public <T> Future<T> submit(@Nonnull Callable<T> tCallable) {
    return schedule(new AdvancingFutureTask<>(scheduler, tCallable));
  }

  @Nonnull
  @Override
  public <T> Future<T> submit(@Nonnull Runnable runnable, T t) {
    return schedule(new AdvancingFutureTask<>(scheduler, runnable, t));
  }

  @Nonnull
  @Override
  public Future<?> submit(@Nonnull Runnable runnable) {
    return submit(runnable, null);
  }

  private <T> Future<T> schedule(final FutureTask<T> futureTask) {
    Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            futureTask.run();
            runnables.remove(this);
          }
        };
    runnables.add(runnable);
    scheduler.post(runnable);

    return futureTask;
  }

  @Nonnull
  @Override
  public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> callables)
      throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public <T> List<Future<T>> invokeAll(
      @Nonnull Collection<? extends Callable<T>> callables, long l, @Nonnull TimeUnit timeUnit)
      throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> callables)
      throws InterruptedException, ExecutionException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T invokeAny(
      @Nonnull Collection<? extends Callable<T>> callables, long l, @Nonnull TimeUnit timeUnit)
      throws InterruptedException, ExecutionException, TimeoutException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void execute(@Nonnull Runnable runnable) {
    @SuppressWarnings({"unused", "nullness"})
    Future<?> possiblyIgnoredError = submit(runnable);
  }
}
