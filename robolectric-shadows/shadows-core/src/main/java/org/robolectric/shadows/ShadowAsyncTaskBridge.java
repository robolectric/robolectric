package org.robolectric.shadows;

import android.os.AsyncTask;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.*;

/**
 * This must be placed in the same package as the underlying AsyncTask because it calls protected methods.
 */
@DoNotInstrument
public class ShadowAsyncTaskBridge<Params, Progress, Result> {
  private AsyncTask<Params, Progress, Result> asyncTask;

  public ShadowAsyncTaskBridge(AsyncTask<Params, Progress, Result> asyncTask) {
    this.asyncTask = asyncTask;
  }

  public Result doInBackground(Params... params) {
    return ReflectionHelpers.callInstanceMethodReflectively(asyncTask, "doInBackground", from(Object[].class, params));
  }

  public void onPreExecute() {
    ReflectionHelpers.callInstanceMethodReflectively(asyncTask, "onPreExecute");
  }

  public void onPostExecute(Result result) {
    ReflectionHelpers.callInstanceMethodReflectively(asyncTask, "onPostExecute", from(Object.class, result));
  }

  public void onProgressUpdate(Progress... values) {
    ReflectionHelpers.callInstanceMethodReflectively(asyncTask, "onProgressUpdate", from(Object[].class, values));
  }

  public void onCancelled() {
    ReflectionHelpers.callInstanceMethodReflectively(asyncTask, "onCancelled");
  }
}
