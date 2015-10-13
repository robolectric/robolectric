package org.robolectric.shadows;

import android.os.AsyncTask;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.internal.Shadow;
import org.robolectric.util.ReflectionHelpers;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

/**
 * Shadow for {@link android.os.AsyncTask}. This shadow's main purpose is to interface between the
 * real Android code and the Robolectric foreground & background schedulers. (Note that if
 * {@link org.robolectric.RoboSettings#isUseGlobalScheduler()} is set, the foreground & background
 * schedulers are the same.) All calls to {@link AsyncTask#doInBackground(Object[])} are scheduled
 * on the background scheduler, which in turn signals to the background thread when it should
 * execute the task. It also provides an {@link #abort()}} method which is used to terminate a task
 * without following the normal post-cancellation callback dispatches, and a resetter which ensures
 * that all pending background tasks are aborted at the end of each test run.
 */
@Implements(AsyncTask.class)
public class ShadowAsyncTask<Params, Progress, Result> {

  @RealObject private AsyncTask<Params, Progress, Result> realAsyncTask;

  public static Set<AsyncTask> queuedTasks = Collections.newSetFromMap(new WeakHashMap<AsyncTask,Boolean>());
  public static Set<AsyncTask> runningTasks = Collections.newSetFromMap(new WeakHashMap<AsyncTask,Boolean>());
  private CountDownLatch startFlag = new CountDownLatch(1);
  private CountDownLatch endFlag = new CountDownLatch(1);

  /**
   * Waits until all pending and running background tasks have exited. This is necessary to ensure
   * that no side effects will carry over into the next test.
    */
  @Resetter
  public static void reset() {
    for (AsyncTask<?,?,?> task : queuedTasks) {
      if (task != null) {
        shadowOf(task).abort();
      }
    }
    queuedTasks.clear();
    // Wait for all of the tasks to exit properly so that
    // they don't inadvertently affect the next test.
    for (AsyncTask<?,?,?> task : runningTasks) {
      if (task != null) {
        try {
          shadowOf(task).endFlag.await();
        } catch (InterruptedException e) {
          // TODO: something more sensible.
        }
      }
    }
    runningTasks.clear();
  }

  // Flag indicating that the current request was aborted.
  private AtomicBoolean aborted = new AtomicBoolean(false);

  /**
   * Aborts the current task. Similar to the standard method {@link AsyncTask#cancel(boolean)}, but
   * this method will ensure that {@link AsyncTask#onCancelled(Object)} is not called. Called by
   * {@link #reset()} when it terminates all running tasks.
   *
   * Note that while this method will prevent further task-related events being scheduled, it will
   * not wait for the background thread to exit (if it has been started already by
   * {@link AsyncTask#execute(Object[])}.
   */
  public void abort() {
    aborted.set(true);
    startFlag.countDown();
  }

  private class FutureTaskWrapper extends FutureTask<Result> {
    private FutureTask<Result> wrapped;
    public FutureTaskWrapper(FutureTask<Result> wrapped) {
      super(wrapped, null);
      this.wrapped = wrapped;
    }

    @Override
    public boolean isCancelled() {
      return wrapped.isCancelled();
    }

    @Override
    public boolean isDone() {
      return wrapped.isDone();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return wrapped.cancel(mayInterruptIfRunning);
    }

    @Override
    public Result get() throws InterruptedException, ExecutionException {
      return wrapped.get();
    }

    @Override
    public Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return wrapped.get(timeout, unit);
    }

    @Override
    protected void done() {
      ReflectionHelpers.callInstanceMethod(wrapped, "done");
    }

    @Override
    protected void set(Result result) {
      ReflectionHelpers.callInstanceMethod(wrapped, "set", from(Object.class, result));
    }

    @Override
    protected void setException(Throwable t) {
      ReflectionHelpers.callInstanceMethod(wrapped, "setException", from(Throwable.class, t));
    }

    @Override
    protected boolean runAndReset() {
      return ReflectionHelpers.callInstanceMethod(wrapped, "runAndReset");
    }

    @Override
    public void run() {
      try {
        // This could be run on the main thread if eg you have an executor which simply
        // calls the run() method on the runnable directly.
        if (!RuntimeEnvironment.isMainThread()) {
          try {
            startFlag.await();
          } catch (InterruptedException e) {
            // TODO: something sensible?
          }
        }
        if (aborted.get()) {
          return;
        }
        wrapped.run();
      } finally {
        endFlag.countDown();
      }
    }
  };

  public void __constructor__() {
    Shadow.invokeConstructor(AsyncTask.class, realAsyncTask);
    FutureTask<Result> orig = ReflectionHelpers.getField(realAsyncTask, "mFuture");
    FutureTaskWrapper wrapper = new FutureTaskWrapper(orig);
    ReflectionHelpers.setField(realAsyncTask, "mFuture", wrapper);
    queuedTasks.add(realAsyncTask);
  }

  @Implementation
  public void finish(Result r) {
    if (aborted.get()) {
      return;
    }
    directlyOn(realAsyncTask, AsyncTask.class, "finish", from(Object.class, r));
  }

  @Implementation
  public AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
    if (aborted.get()) {
      return realAsyncTask;
    }
    directlyOn(realAsyncTask, AsyncTask.class, "executeOnExecutor", from(Executor.class, exec), from(Object[].class, params));
    // Once the real execute() has been called, we cannot stop the task from being started on the background
    // thread. If we need to reset, then we keep track of the tasks so that we can wait for them
    // to finish before proceeding.
    runningTasks.add(realAsyncTask);
    ShadowApplication.getInstance().getBackgroundThreadScheduler().post(new Runnable() {
      public void run() {
        startFlag.countDown();
        try {
          endFlag.await();
        } catch (InterruptedException e) {
          // TODO: something a bit more useful and robust?
        }
      }
    });
    return realAsyncTask;
  }
}
