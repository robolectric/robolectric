package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;

import android.content.AsyncTaskLoader;
import android.content.Context;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;

/**
 * The shadow {@link AsyncTaskLoader} for {@link LooperMode.Mode#LEGACY}.
 *
 * @deprecated {@link AsyncTaskLoader} is deprecated in the Android SDK.
 */
@Deprecated
@Implements(
    value = AsyncTaskLoader.class,
    shadowPicker = ShadowAsyncTaskLoader.Picker.class,
    isInAndroidSdk = false)
public class ShadowLegacyAsyncTaskLoader<D> extends ShadowAsyncTaskLoader<D> {
  @RealObject private AsyncTaskLoader<D> realObject;
  private BackgroundWorker worker;

  @Implementation
  protected void __constructor__(Context context) {
    worker = new BackgroundWorker();
  }

  @Implementation
  protected void onForceLoad() {
    FutureTask<D> future =
        new FutureTask<D>(worker) {
          @Override
          protected void done() {
            try {
              final D result = get();
              RuntimeEnvironment.getMasterScheduler().post(() -> realObject.deliverResult(result));
            } catch (InterruptedException e) {
              // Ignore
            } catch (ExecutionException e) {
              throw new RuntimeException(e.getCause());
            }
          }
        };

    shadowOf(RuntimeEnvironment.getApplication()).getBackgroundThreadScheduler().post(future);
  }

  private final class BackgroundWorker implements Callable<D> {
    @Override
    public D call() throws Exception {
      return realObject.loadInBackground();
    }
  }
}
