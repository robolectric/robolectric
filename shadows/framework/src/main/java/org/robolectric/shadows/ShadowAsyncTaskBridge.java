package org.robolectric.shadows;

import android.os.AsyncTask;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Bridge between shadows and {@link android.os.AsyncTask}.
 */
@DoNotInstrument
public class ShadowAsyncTaskBridge<Params, Progress, Result> {
  private AsyncTask<Params, Progress, Result> asyncTask;

  public ShadowAsyncTaskBridge(AsyncTask<Params, Progress, Result> asyncTask) {
    this.asyncTask = asyncTask;
  }

  public Result doInBackground(Params... params) {
    return ReflectionHelpers.callInstanceMethod(asyncTask, "doInBackground", ClassParameter.from(Object[].class, params));
  }

  public void onPreExecute() {
    ReflectionHelpers.callInstanceMethod(asyncTask, "onPreExecute");
  }

  public void onPostExecute(Result result) {
    ReflectionHelpers.callInstanceMethod(asyncTask, "onPostExecute", ClassParameter.from(Object.class, result));
  }

  public void onProgressUpdate(Progress... values) {
    ReflectionHelpers.callInstanceMethod(asyncTask, "onProgressUpdate", ClassParameter.from(Object[].class, values));
  }

  public void onCancelled() {
    // Assume the result is null since the result cannot be retrieved from the FutureTask if it is
    // cancelled.
    ReflectionHelpers.callInstanceMethod(
        asyncTask, "onCancelled", ClassParameter.from(Object.class, null));
  }
}
