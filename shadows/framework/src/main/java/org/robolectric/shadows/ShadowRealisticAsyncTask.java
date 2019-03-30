package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.os.AsyncTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

@Implements(
    value = AsyncTask.class,
    shadowPicker = ShadowBaseAsyncTask.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowRealisticAsyncTask<Params, Progress, Result> extends ShadowBaseAsyncTask {

  @RealObject private AsyncTask<Params, Progress, Result> realObject;

  // an optimization flag to ensure the expensive draining/reset logic is only run when needed
  private static AtomicBoolean resetNeeded = new AtomicBoolean(false);

  @Implementation
  protected void __constructor__() {
    resetNeeded.set(true);

    invokeConstructor(AsyncTask.class, realObject);
  }

  @Resetter
  public static void reset() {
    if (resetNeeded.getAndSet(false)) {
      idleQuietly();
    }
  }

  private static void idleQuietly() {
    try {
      waitForIdle();
    } catch (ExecutionException e) {
    } catch (InterruptedException e) {
    }
  }

  /**
   * Ensures prior background tasks posted via the single threaded AsyncTask#execute() have been
   * executed.
   *
   * <p>Does NOT currently guarantee idleness for tasks posted via execute(Executor). TODO: look at
   * reusing Espresso's AsyncTaskPoolMonitor
   */
  public static void waitForIdle() throws ExecutionException, InterruptedException {
    AsyncTask<Void, Void, Void> idle =
        new AsyncTask() {
          @Override
          protected Object doInBackground(Object... objects) {
            return null;
          }
        };
    idle.execute();
    idle.get();
  }
}
