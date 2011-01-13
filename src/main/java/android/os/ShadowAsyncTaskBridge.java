package android.os;

import com.xtremelabs.robolectric.internal.DoNotInstrument;

@DoNotInstrument
public class ShadowAsyncTaskBridge<Params, Progress, Result> {
    private AsyncTask<Params, Progress, Result> asyncTask;

    public ShadowAsyncTaskBridge(AsyncTask<Params, Progress, Result> asyncTask) {
        this.asyncTask = asyncTask;
    }

    public Result doInBackground(Params... params) {
        return asyncTask.doInBackground(params);
    }

    public void onPreExecute() {
        asyncTask.onPreExecute();
    }

    public void onPostExecute(Result result) {
        asyncTask.onPostExecute(result);
    }

    public void onProgressUpdate(Progress... values) {
        asyncTask.onProgressUpdate(values);
    }

    public void onCancelled() {
        asyncTask.onCancelled();
    }
}
