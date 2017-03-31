package org.robolectric.shadows.support.v4;

import android.content.Context;
import org.robolectric.Robolectric;
import org.robolectric.util.SimpleFuture;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;
import android.support.v4.content.AsyncTaskLoader;
import java.util.concurrent.Callable;

/**
 * Shadow for {@link android.support.v4.content.AsyncTaskLoader}.
 */
@Implements(AsyncTaskLoader.class)
public class ShadowAsyncTaskLoader<D> {
  @RealObject private AsyncTaskLoader<D> realLoader;
  private SimpleFuture<D> future;

  public void __constructor__(Context context) {
    BackgroundWorker worker = new BackgroundWorker();
    future = new SimpleFuture<D>(worker) {
      @Override protected void done() {
        try {
          final D result = get();
          Robolectric.getForegroundThreadScheduler().post(new Runnable() {
            @Override public void run() {
              realLoader.deliverResult(result);
            }
          });
        } catch (InterruptedException e) {
          // Ignore
        }
      }
    };
  }

  @Implementation
  public void onForceLoad() {
    Robolectric.getBackgroundThreadScheduler().post(new Runnable() {
      @Override
      public void run() {
        future.run();
      }
    });
  }

  private final class BackgroundWorker implements Callable<D> {
    @Override public D call() throws Exception {
      return realLoader.loadInBackground();
    }
  }
}
