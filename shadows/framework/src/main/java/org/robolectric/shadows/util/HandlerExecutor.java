package org.robolectric.shadows.util;

import android.os.Handler;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/** An adapter {@link Executor} that posts all executed tasks onto the given {@link Handler}. */
public class HandlerExecutor implements Executor {
  private final Handler handler;

  public HandlerExecutor(Handler handler) {
    this.handler = Objects.requireNonNull(handler);
  }

  @Override
  public void execute(Runnable command) {
    if (!handler.post(command)) {
      throw new RejectedExecutionException(handler + " is shutting down");
    }
  }
}
