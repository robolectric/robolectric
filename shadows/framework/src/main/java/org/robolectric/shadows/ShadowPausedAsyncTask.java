package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.AsyncTask;
import androidx.test.annotation.Beta;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * A {@link AsyncTask} shadow for {@link LooperMode.Mode.PAUSED}
 *
 * <p>This is beta API, and will likely be renamed/removed in a future Robolectric release.
 */
@Implements(
    value = AsyncTask.class,
    shadowPicker = ShadowAsyncTask.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
@Beta
public class ShadowPausedAsyncTask<Params, Progress, Result> extends ShadowAsyncTask {

  private static Executor executorOverride = null;

  @RealObject private AsyncTask<Params, Progress, Result> realObject;

  @Resetter
  public static void reset() {
    executorOverride = null;
  }

  @Implementation
  protected AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params) {
    Executor executorToUse = executorOverride == null ? exec : executorOverride;
    return reflector(AsyncTaskReflector.class, realObject).executeOnExecutor(executorToUse, params);
  }

  private ClassParameter[] buildClassParams(Params... params) {
    ClassParameter[] classParameters = new ClassParameter[params.length];
    for (int i = 0; i < params.length; i++) {
      classParameters[i] = ClassParameter.from(Object.class, params[i]);
    }
    return classParameters;
  }

  /**
   * Globally override the executor used for all AsyncTask#execute* calls.
   *
   * <p>This can be useful if you want to use a more determinstic executor for tests, like {@link
   * org.robolectric.android.util.concurrent.PausedExecutorService} or {@link
   * org.robolectric.android.util.concurrent.InlineExecutorService}.
   *
   * <p>Use this API as a last resort. Its recommended instead to use dependency injection to
   * provide a custom executor to AsyncTask#executeOnExecutor.
   *
   * <p>Beta API, may be removed or changed in a future Robolectric release
   */
  @Beta
  public static void overrideExecutor(Executor executor) {
    executorOverride = executor;
  }

  @ForType(AsyncTask.class)
  interface AsyncTaskReflector {

    @Direct
    AsyncTask executeOnExecutor(Executor executorToUse, Object[] params);
  }
}
