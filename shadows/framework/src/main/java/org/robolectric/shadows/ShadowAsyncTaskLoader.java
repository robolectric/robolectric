package org.robolectric.shadows;

import android.content.AsyncTaskLoader;
import android.content.Context;
import java.util.concurrent.Callable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.SimpleFuture;

@Implements(AsyncTaskLoader.class)
public class ShadowAsyncTaskLoader<D> {
  @RealObject private AsyncTaskLoader<D> realObject;
  private SimpleFuture<D> future;

  public void __constructor__(Context context) {
    BackgroundWorker worker = new BackgroundWorker();
    future = new SimpleFuture<D>(worker) {
      @Override
      protected void done() {
        try {
          final D result = get();
          ShadowApplication.getInstance().getForegroundThreadScheduler().post(new Runnable() {
            @Override
            public void run() {
              realObject.deliverResult(result);
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
    ShadowApplication.getInstance().getBackgroundThreadScheduler().post(new Runnable() {
      @Override
      public void run() {
        future.run();
      }
    });
  }

  private final class BackgroundWorker implements Callable<D> {
    @Override
    public D call() throws Exception {
      return realObject.loadInBackground();
    }
  }
}
