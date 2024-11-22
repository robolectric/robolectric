package org.robolectric.android.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import org.robolectric.annotation.LooperMode;

/**
 * Executor service that executes posted tasks as soon as they are posted.
 *
 * <p>Intended to be a replacement for {@link RoboExecutorService} when using {@link
 * LooperMode.Mode#PAUSED}. Unlike {@link RoboExecutorService}, will execute tasks on a background
 * thread. This is useful to test Android code that enforces it runs off the main thread.
 *
 * <p>Also consider using {@link MoreExecutors#directExecutor()}, if your code under test can handle
 * being called from main thread.
 *
 * <p>Also see {@link PausedExecutorService} if you need control over when posted tasks are
 * executed.
 *
 * <p>NOTE: Beta API, subject to change.
 */
@Beta
public class InlineExecutorService implements ExecutorService {
  private final PausedExecutorService delegateService;

  public InlineExecutorService() {
    this.delegateService = new PausedExecutorService();
  }

  @Override
  public void shutdown() {
    delegateService.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return delegateService.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return delegateService.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return delegateService.isTerminated();
  }

  @Override
  public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
    // If not shut down first, timeout would occur with normal behavior.
    return delegateService.awaitTermination(l, timeUnit);
  }

  @Nonnull
  @Override
  public <T> Future<T> submit(@Nonnull Callable<T> task) {
    Future<T> future = delegateService.submit(task);
    delegateService.runAll();
    return future;
  }

  @Nonnull
  @Override
  public <T> Future<T> submit(@Nonnull Runnable task, T result) {
    Future<T> future = delegateService.submit(task, result);
    delegateService.runAll();
    return future;
  }

  @Nonnull
  @Override
  public Future<?> submit(@Nonnull Runnable task) {
    Future<?> future = delegateService.submit(task);
    delegateService.runAll();
    return future;
  }

  @Override
  @SuppressWarnings("FutureReturnValueIgnored")
  public void execute(@Nonnull Runnable command) {
    delegateService.execute(command);
    delegateService.runAll();
  }

  @Nonnull
  @Override
  public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    return delegateService.invokeAll(tasks);
  }

  @Nonnull
  @Override
  public <T> List<Future<T>> invokeAll(
      @Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit)
      throws InterruptedException {
    return delegateService.invokeAll(tasks, timeout, unit);
  }

  @Nonnull
  @Override
  public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks)
      throws ExecutionException, InterruptedException {
    return delegateService.invokeAny(tasks);
  }

  @Override
  public <T> T invokeAny(
      @Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit)
      throws ExecutionException, InterruptedException, TimeoutException {
    return delegateService.invokeAny(tasks, timeout, unit);
  }
}
