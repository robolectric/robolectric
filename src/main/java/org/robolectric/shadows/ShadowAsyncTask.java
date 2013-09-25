package org.robolectric.shadows;

import android.os.AsyncTask;
import android.os.ShadowAsyncTaskBridge;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.SimpleFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Implements(AsyncTask.class)
public class ShadowAsyncTask<Params, Progress, Result> {

  @RealObject private AsyncTask<Params, Progress, Result> realAsyncTask;

  private final SimpleFuture<Result> future;
  private final BackgroundWorker worker;
  private AsyncTask.Status status = AsyncTask.Status.PENDING;

  public ShadowAsyncTask() {
    worker = new BackgroundWorker();
    future = new SimpleFuture<Result>(worker) {
      @Override
      protected void done() {
        status = AsyncTask.Status.FINISHED;
        try {
          final Result result = get();

          try {
            Robolectric.getUiThreadScheduler().post(new Runnable() {
              @Override
              public void run() {
                getBridge().onPostExecute(result);
              }
            });
          } catch (Throwable t) {
            throw new OnPostExecuteException(t);
          }
        } catch (CancellationException e) {
          Robolectric.getUiThreadScheduler().post(new Runnable() {
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
  public boolean isCancelled() {
    return future.isCancelled();
  }

  @Implementation
  public boolean cancel(boolean mayInterruptIfRunning) {
    return future.cancel(mayInterruptIfRunning);
  }

  @Implementation
  public Result get() throws InterruptedException, ExecutionException {
    return future.get();
  }

  @Implementation
  public Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return future.get(timeout, unit);
  }

  @Implementation
  public AsyncTask<Params, Progress, Result> execute(final Params... params) {
    status = AsyncTask.Status.RUNNING;
    getBridge().onPreExecute();

    worker.params = params;

    Robolectric.getBackgroundScheduler().post(new Runnable() {
      @Override
      public void run() {
        future.run();
      }
    });

    return realAsyncTask;
  }

  @Implementation
  public AsyncTask<Params, Progress, Result> executeOnExecutor(Executor executor, Params... params) {
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
  public AsyncTask.Status getStatus() {
    return status;
  }

  /**
   * Enqueue a call to {@link AsyncTask#onProgressUpdate(Object[])} on UI looper (or run it immediately
   * if the looper it is not paused).
   *
   * @param values The progress values to update the UI with.
   * @see AsyncTask#publishProgress(Object[])
   */
  @Implementation
  public void publishProgress(final Progress... values) {
    Robolectric.getUiThreadScheduler().post(new Runnable() {
      @Override
      public void run() {
        getBridge().onProgressUpdate(values);
      }
    });
  }

  private ShadowAsyncTaskBridge<Params, Progress, Result> getBridge() {
    return new ShadowAsyncTaskBridge<Params, Progress, Result>(realAsyncTask);
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
