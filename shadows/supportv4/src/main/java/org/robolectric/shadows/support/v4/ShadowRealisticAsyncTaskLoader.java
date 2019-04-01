package org.robolectric.shadows.support.v4;


import static org.robolectric.util.reflector.Reflector.reflector;

import android.support.v4.content.AsyncTaskLoader;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(
    value = AsyncTaskLoader.class,
    shadowPicker = ShadowBaseAsyncTaskLoader.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowRealisticAsyncTaskLoader<D> extends ShadowBaseAsyncTaskLoader {

  @RealObject private AsyncTaskLoader<D> realObject;

  /**
   * Allows overriding background executor used by the AsyncLoader.
   *
   * Its recommended to switch to androidx's AsyncTaskLoader, which provides an overriddable
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
