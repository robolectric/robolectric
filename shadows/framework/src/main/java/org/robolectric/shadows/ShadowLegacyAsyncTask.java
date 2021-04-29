package org.robolectric.shadows;

import android.os.AsyncTask;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;

/** A {@link AsyncTask} shadow for {@link LooperMode.Mode#LEGACY}. */
@Implements(
    value = AsyncTask.class,
    shadowPicker = ShadowAsyncTask.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowLegacyAsyncTask<Params, Progress, Result> extends ShadowAsyncTask {

  @RealObject private AsyncTask<Params, Progress, Result> realAsyncTask;

  private final SettableFuture<Result> future;

  private Params[] params;
  private AsyncTask.Status status = AsyncTask.Status.PENDING;

  public ShadowLegacyAsyncTask() {
    future = SettableFuture.create();
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
    this.params = params;

    ShadowLegacyLooper.getBackgroundThreadScheduler().post(this::runTask);

    return realAsyncTask;
  }

  @Implementation
  protected AsyncTask<Params, Progress, Result> executeOnExecutor(
      Executor executor, Params... params) {
    status = AsyncTask.Status.RUNNING;
    getBridge().onPreExecute();
    this.params = params;
    executor.execute(this::runTask);
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
    RuntimeEnvironment.getMasterScheduler().post(() -> getBridge().onProgressUpdate(values));
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

  protected void runTask() {
    final AtomicReference<Result> result = new AtomicReference<>();
    boolean success = false;
    if (isCancelled()) {
      RuntimeEnvironment.getMasterScheduler().post(() -> getBridge().onCancelled());
    } else {
      try {
        result.set(getBridge().doInBackground(params));
        success = true;
      } catch (Exception e) {
        future.setException(e);
      }
    }
    if (success) {
      status = AsyncTask.Status.FINISHED;
      try {
        RuntimeEnvironment.getMasterScheduler().post(() -> getBridge().onPostExecute(result.get()));
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
      future.set(result.get());
    }
  }
}
