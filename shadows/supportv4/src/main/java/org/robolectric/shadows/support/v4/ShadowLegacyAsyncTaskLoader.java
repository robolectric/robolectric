package org.robolectric.shadows.support.v4;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;

/**
 * A {@link AsyncTaskLoader} shadow for {@link LooperMode.Mode.LEGACY}.
 *
 * <p>This class should not be referenced directly, use {@link ShadowAsyncTaskLoader} instead.
 */
@Implements(
    value = AsyncTaskLoader.class,
    shadowPicker = ShadowAsyncTaskLoader.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
@Deprecated
public class ShadowLegacyAsyncTaskLoader<D> extends ShadowAsyncTaskLoader {
  @RealObject private AsyncTaskLoader<D> realLoader;
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
              Robolectric.getForegroundThreadScheduler()
                  .post(
                      new Runnable() {
                        @Override
                        public void run() {
                          realLoader.deliverResult(result);
                        }
                      });
            } catch (InterruptedException e) {
              // Ignore
            } catch (ExecutionException e) {
              throw new RuntimeException(e.getCause());
            }
          }
        };
    Robolectric.getBackgroundThreadScheduler().post(future);
  }

  private final class BackgroundWorker implements Callable<D> {
    @Override
    public D call() throws Exception {
      return realLoader.loadInBackground();
    }
  }
}
