package org.robolectric.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

/**
 * A Future represents the result of an asynchronous computation.
 *
 * @param <T> The result type returned by this Future's get method.
 * @deprecation This class can introduce deadlocks, since its lock is held while invoking run().
 */
@Deprecated
public class SimpleFuture<T> {
  private T result;
  private boolean hasRun;
  private boolean cancelled;
  private final Callable<T> callable;

  public SimpleFuture(Callable<T> callable) {
    this.callable = callable;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    if (!hasRun) {
      cancelled = true;
      done();
    }

    return cancelled;
  }

  public synchronized T get() throws InterruptedException {
    if (cancelled) {
      throw new CancellationException();
    } else {
      while (!hasRun) this.wait();
      return result;
    }
  }

  public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException {
    if (cancelled) {
      throw new CancellationException();
    } else {
      while (!hasRun) this.wait(unit.toMillis(timeout));
      return result;
    }
  }

  public synchronized void run() {
    try {
      if (!cancelled) {
        result = callable.call();
        hasRun = true;
        done();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    this.notify();
  }

  protected void done() {
  }
}
