package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import java.util.concurrent.ExecutionException;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;
import org.robolectric.util.reflector.Static;

@Implements(
    value = AsyncTask.class,
    shadowPicker = ShadowBaseAsyncTask.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowRealisticAsyncTask<Params, Progress, Result>  extends ShadowBaseAsyncTask {

  @RealObject
  private AsyncTask<Params, Progress, Result> realObject;

  @Resetter
  public static void reset() {
    // TODO: figure out how to reset in a more performant manner
//    if (ShadowBaseLooper.useRealisticLooper()) {
//      idleQuietly();
//    }
  }

  private static void idleQuietly() {
    try {
      idle();
    } catch (ExecutionException e) {

    } catch (InterruptedException e) {

    }
  }

  /**
   * Ensures prior background tasks posted via the single threaded AsyncTask#execute() have been executed.
   *
   * Does NOT currently guarantee idleness for tasks posted via execute(Executor).
   * TODO: look at reusing Espresso's AsyncTaskPoolMonitor
   */
  public static void idle() throws ExecutionException, InterruptedException {
    AsyncTask<Void, Void, Void> idle = new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] objects) {
        return null;
      }
    };
    idle.execute();
    idle.get();
  }
}
