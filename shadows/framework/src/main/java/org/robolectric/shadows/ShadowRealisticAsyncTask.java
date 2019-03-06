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

  @Implementation
  protected void __constructor__() {
    if (RuntimeEnvironment.getApiLevel() <= 21) {
      // this is kind of hacky, but ensure the static sHandler field refers to the current main
      // looper on api levels > 21 this is not needed because sHandler is lazy-initialized
      Object newHandler = ReflectionHelpers.callConstructor(
          ReflectionHelpers.loadClass(this.getClass().getClassLoader(), "android.os.AsyncTask$InternalHandler")
      );
      reflector(ReflectorAsyncTask.class).setHandler((Handler)newHandler);
    }
    invokeConstructor(AsyncTask.class, realObject);
  }

  @Resetter
  public static void reset() {
    if (ShadowBaseLooper.useRealisticLooper()) {
      idleQuietly();
      if (RuntimeEnvironment.getApiLevel() > 21) {
        reflector(ReflectorAsyncTask.class).setHandler(null);
      }
    }
  }

  private static void idleQuietly() {
    try {
      idle();
    } catch (ExecutionException e) {

    } catch (InterruptedException e) {

    }
  }

  @ForType(AsyncTask.class)
  interface ReflectorAsyncTask {
    @Static
    @Accessor("sHandler")
    void setHandler(Handler handler);

    @Static
    @Accessor("sHandler")
    Handler getHandler();
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
