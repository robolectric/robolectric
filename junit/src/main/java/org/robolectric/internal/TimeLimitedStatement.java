package org.robolectric.internal;

import java.util.concurrent.TimeUnit;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/**
 * Similar to JUnit's {@link org.junit.internal.runners.statements.FailOnTimeout}, but runs the
 * test on the current thread (with a timer on a new thread) rather than the other way around.
 */
class TimeLimitedStatement extends Statement {

  private final long timeout;
  private final Statement delegate;

  public TimeLimitedStatement(long timeout, Statement delegate) {
    this.timeout = timeout;
    this.delegate = delegate;
  }

  @Override
  public void evaluate() throws Throwable {
    Thread testThread = Thread.currentThread();
    Thread timeoutThread =
        new Thread(
            () -> {
              try {
                Thread.sleep(timeout);
                testThread.interrupt();
              } catch (InterruptedException e) {
                // ok
              }
            },
            "Robolectric time-limited test");
    timeoutThread.start();

    try {
      delegate.evaluate();
    } catch (InterruptedException e) {
      Exception e2 = new TestTimedOutException(timeout, TimeUnit.MILLISECONDS);
      e2.setStackTrace(e.getStackTrace());
      throw e2;
    } finally {
      timeoutThread.interrupt();
      timeoutThread.join();
    }
  }
}
