package org.robolectric.shadows;

import android.os.AsyncTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(AsyncTask.class)
public class ShadowAsyncTask<Params, Progress, Result> {

  @RealObject private AsyncTask<Params, Progress, Result> realAsyncTask;

  private final FutureTask<Result> future;
  private final BackgroundWorker worker;
  private AsyncTask.Status status = AsyncTask.Status.PENDING;

  public ShadowAsyncTask() {
    worker = new BackgroundWorker();
    future = new FutureTask<Result>(worker) {
      @Override
      protected void done() {
        status = AsyncTask.Status.FINISHED;
        try {
          final Result result = get();

          try {
            ShadowApplication.getInstance().getForegroundThreadScheduler().post(new Runnable() {
              @Override
              public void run() {
                getBridge().onPostExecute(result);
              }
            });
          } catch (Throwable t) {
            throw new OnPostExecuteException(t);
          }
        } catch (CancellationException e) {
          ShadowApplication.getInstance().getForegroundThreadScheduler().post(new Runnable() {
            @Override
            public void run() {
              getBridge().onCancelled();
            }
          });
        } catch (InterruptedException e) {
          // Ignore.
        } catch (OnPostExecuteException e) {
          throw new RuntimeException(e.getCause());
        } catch (Throwable t) {
          throw new RuntimeException("An error occured while executing doInBackground()",
              t.getCause());
        }
      }
    };
  }

  @Implementation
  protected boolean isCancelled() {
    return future.isCancelled();
  }

  @Implementation
  protected boolean cancel(boolean mayInterruptIfRunning) {
    return future.cancel(mayInterruptIfRunning);
  }

  @Implementation
  protected Result get() throws InterruptedException, ExecutionException {
    return future.get();
  }

  @Implementation
  protected Result get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return future.get(timeout, unit);
  }

  @Implementation
  protected AsyncTask<Params, Progress, Result> execute(final Params... params) {
    status = AsyncTask.Status.RUNNING;
    getBridge().onPreExecute();

    worker.params = params;

    ShadowApplication.getInstance().getBackgroundThreadScheduler().post(new Runnable() {
      @Override
      public void run() {
        future.run();
      }
    });

    return realAsyncTask;
  }

  @Implementation
  protected AsyncTask<Params, Progress, Result> executeOnExecutor(
      Executor executor, Params... params) {
    status = AsyncTask.Status.RUNNING;
    getBridge().onPreExecute();

    worker.params = params;
    executor.execute(new Runnable() {
      @Override
      public void run() {
        future.run();
      }
    });

    return realAsyncTask;
  }

  @Implementation
  protected AsyncTask.Status getStatus() {
    return status;
  }

  /**
   * Enqueue a call to {@link AsyncTask#onProgressUpdate(Object[])} on UI looper (or run it
   * immediately if the looper it is not paused).
   *
   * @param values The progress values to update the UI with.
   * @see AsyncTask#publishProgress(Object[])
   */
  @Implementation
  protected void publishProgress(final Progress... values) {
    ShadowApplication.getInstance().getForegroundThreadScheduler().post(new Runnable() {
      @Override
      public void run() {
        getBridge().onProgressUpdate(values);
      }
    });
  }

  private ShadowAsyncTaskBridge<Params, Progress, Result> getBridge() {
    return new ShadowAsyncTaskBridge<>(realAsyncTask);
  }

  private final class BackgroundWorker implements Callable<Result> {
    Params[] params;

    @Override
    public Result call() throws Exception {
      return getBridge().doInBackground(params);
    }
  }

  private static class OnPostExecuteException extends Exception {
    public OnPostExecuteException(Throwable throwable) {
      super(throwable);
    }
  }
}
