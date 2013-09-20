package org.robolectric.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

public class SimpleFuture<Result> {

  private boolean cancelled;
  private boolean hasRun;
  private Callable<Result> callable;
  private Result result;

  public SimpleFuture(Callable<Result> callable) {
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

  public synchronized Result get() throws InterruptedException {
    if (cancelled) {
      throw new CancellationException();
    } else {
      while (!hasRun) this.wait();
      return result;
    }
  }

  public synchronized Result get(long timeout, TimeUnit unit) throws InterruptedException {
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