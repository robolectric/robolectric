package com.xtremelabs.robolectric.shadows;

import android.os.AsyncTask;
import android.os.ShadowAsyncTaskBridge;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
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
        getBridge().onPreExecute();

        Robolectric.getBackgroundScheduler().post(new Runnable() {
            @Override public void run() {
                if (cancelled) return;
                hasRun = true;
                final Result result = getBridge().doInBackground(params);

                Robolectric.getUiThreadScheduler().post(new Runnable() {
                    @Override public void run() {
                        getBridge().onPostExecute(result);
                    }
                });
            }
        });

        return null;
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
            @Override public void run() {
                getBridge().onProgressUpdate(values);
            }
        });
    }

    private ShadowAsyncTaskBridge<Params, Progress, Result> getBridge() {
        return new ShadowAsyncTaskBridge<Params, Progress, Result>(realAsyncTask);
    }
}
