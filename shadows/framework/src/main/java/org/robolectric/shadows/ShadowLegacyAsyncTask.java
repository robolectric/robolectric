package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.AsyncTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.ForType;

/** A {@link AsyncTask} shadow for {@link LooperMode.Mode.LEGACY}. */
@Implements(
    value = AsyncTask.class,
    shadowPicker = ShadowAsyncTask.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowLegacyAsyncTask<Params, Progress, Result> extends ShadowAsyncTask {

  @RealObject private AsyncTask<Params, Progress, Result> realAsyncTask;

  private final FutureTask<Result> future;
  private final BackgroundWorker worker;
  private AsyncTask.Status status = AsyncTask.Status.PENDING;

  public ShadowLegacyAsyncTask() {
    worker = new BackgroundWorker();
    future = createFuture(worker);
  }

  protected FutureTask<Result> createFuture(Callable<Result> callable) {
    return new FutureTask<Result>(callable) {
      @Override
      protected void done() {
        status = AsyncTask.Status.FINISHED;
        try {
          final Result result = get();

          try {
            RuntimeEnvironment.getMasterScheduler()
                .post(
                    () ->
                        reflector(LegacyAsyncTaskReflector.class, realAsyncTask)
                            .onPostExecute(result));
          } catch (Throwable t) {
            throw new OnPostExecuteException(t);
          }
        } catch (CancellationException e) {
          RuntimeEnvironment.getMasterScheduler()
              .post(
                  () -> reflector(LegacyAsyncTaskReflector.class, realAsyncTask).onCancelled(null));
        } catch (InterruptedException e) {
          // Ignore.
        } catch (OnPostExecuteException e) {
          throw new RuntimeException(e.getCause());
        } catch (Throwable t) {
          throw new RuntimeException(
              "An error occurred while executing doInBackground()", t.getCause());
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
    reflector(LegacyAsyncTaskReflector.class, realAsyncTask).onPreExecute();

    worker.params = params;

    ShadowLegacyLooper.getBackgroundThreadScheduler().post(future);

    return realAsyncTask;
  }

  @Implementation
  protected AsyncTask<Params, Progress, Result> executeOnExecutor(
      Executor executor, Params... params) {
    status = AsyncTask.Status.RUNNING;
    reflector(LegacyAsyncTaskReflector.class, realAsyncTask).onPreExecute();

    worker.params = params;
    executor.execute(future);

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
    RuntimeEnvironment.getMasterScheduler()
        .post(
            () ->
                reflector(LegacyAsyncTaskReflector.class, realAsyncTask).onProgressUpdate(values));
  }

  private final class BackgroundWorker implements Callable<Result> {
    Params[] params;

    @Override
    @SuppressWarnings("unchecked")
    public Result call() throws Exception {
      return (Result)
          reflector(LegacyAsyncTaskReflector.class, realAsyncTask).doInBackground(params);
    }
  }

  private static class OnPostExecuteException extends Exception {
    public OnPostExecuteException(Throwable throwable) {
      super(throwable);
    }
  }

  @ForType(AsyncTask.class)
  interface LegacyAsyncTaskReflector {
    Object doInBackground(Object... params);

    void onPreExecute();

    void onPostExecute(Object result);

    void onProgressUpdate(Object... values);

    void onCancelled(Object object);
  }
}
