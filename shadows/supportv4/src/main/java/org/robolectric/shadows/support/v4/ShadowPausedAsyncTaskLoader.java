package org.robolectric.shadows.support.v4;

import static org.robolectric.util.reflector.Reflector.reflector;

import androidx.loader.content.AsyncTaskLoader;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * A shadow for {@link AsyncTaskLoader} that is active when {@link LooperMode} is {@link
 * Mode.PAUSED}.
 *
 * @deprecated use the androidx AsyncTaskLoader instead, which has an overriddable getExecutor
 *     method.
 */
@Implements(
    value = AsyncTaskLoader.class,
    shadowPicker = ShadowAsyncTaskLoader.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
@Deprecated
public class ShadowPausedAsyncTaskLoader<D> extends ShadowAsyncTaskLoader {

  @RealObject private AsyncTaskLoader<D> realObject;

  /**
   * Allows overriding background executor used by the AsyncLoader.
   *
   * <p>Its recommended to switch to androidx's AsyncTaskLoader, which provides an overriddable
   * getExecutor method.
   */
  public void setExecutor(Executor executor) {
    reflector(ReflectorAsyncTaskLoader.class, realObject).setExecutor(executor);
  }

  /** Accessor interface for {@link AsyncTaskLoader}'s internals. */
  @ForType(AsyncTaskLoader.class)
  private interface ReflectorAsyncTaskLoader {
    @Accessor("mExecutor")
    void setExecutor(Executor executor);
  }
}
