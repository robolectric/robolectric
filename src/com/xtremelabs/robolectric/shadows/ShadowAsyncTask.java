package com.xtremelabs.robolectric.shadows;

import android.os.AsyncTask;
import android.os.ShadowAsyncTaskBridge;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(AsyncTask.class)
public class ShadowAsyncTask<Params, Progress, Result> {
    @RealObject private AsyncTask<Params, Progress, Result> realAsyncTask;
    private boolean cancelled = false;
    private boolean hasRun = false;

//    public android.os.AsyncTask.Status getStatus() {
//        return null;
//    }

//    public boolean isCancelled() {
//        return false;
//    }

    @Implementation
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (hasRun) return false;
        cancelled = true;
        return true;
    }

//    public Result get() throws java.lang.InterruptedException, java.util.concurrent.ExecutionException {
//        return null;
//    }

//    public Result get(long timeout, java.util.concurrent.TimeUnit unit) throws java.lang.InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
//        return null;
//    }

    @Implementation
    public android.os.AsyncTask<Params, Progress, Result> execute(final Params... params) {
        final ShadowAsyncTaskBridge<Params, Progress, Result> bridge = new ShadowAsyncTaskBridge<Params, Progress, Result>(realAsyncTask);
        bridge.onPreExecute();

        Robolectric.getBackgroundScheduler().post(new Runnable() {
            @Override public void run() {
                if (cancelled) return;
                hasRun = true;
                final Result result = bridge.doInBackground(params);

                Robolectric.getUiThreadScheduler().post(new Runnable() {
                    @Override public void run() {
                        bridge.onPostExecute(result);
                    }
                });
            }
        });

        return null;
    }

//    public void publishProgress(Progress... values) {
//    }
}
