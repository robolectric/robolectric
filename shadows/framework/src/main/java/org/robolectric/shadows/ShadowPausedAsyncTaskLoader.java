package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.AsyncTaskLoader;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * The shadow {@link AsyncTaskLoader} for {@link LooperMode.Mode.PAUSED}.
 *
 * <p>In {@link LooperMode.Mode.PAUSED} mode, Robolectric just uses the real AsyncTaskLoader for
 * now.
 */
@Implements(
    value = AsyncTaskLoader.class,
    shadowPicker = ShadowAsyncTaskLoader.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowPausedAsyncTaskLoader<D> extends ShadowAsyncTaskLoader<D> {

  @RealObject private AsyncTaskLoader<D> realObject;

  /**
   * Allows overriding background executor used by the AsyncLoader.
   *
   * @deprecated It is recommended to switch to androidx's AsyncTaskLoader, which provides an
   *     overridable getExecutor method.
   */
  @Deprecated
  public void setExecutor(Executor executor) {
    reflector(ReflectorAsyncTaskLoader.class, realObject).setExecutor(executor);
  }

  /** Accessor interface for {@link android.content.AsyncTaskLoader}'s internals. */
  @ForType(AsyncTaskLoader.class)
  private interface ReflectorAsyncTaskLoader {
    @Accessor("mExecutor")
    void setExecutor(Executor executor);
  }
}
