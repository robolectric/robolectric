package org.robolectric.shadows;

import android.content.AsyncTaskLoader;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;

/**
 * The shadow API for {@link AsyncTaskLoader}.
 *
 * <p>Different shadow implementations will be used based on the current {@link LooperMode.Mode}.
 *
 * @deprecated {@link AsyncTaskLoader} is deprecated in the Android SDK.
 * @see ShadowLegacyAsyncTaskLoader
 * @see ShadowPausedAsyncTaskLoader
 */
@Deprecated
@Implements(value = AsyncTaskLoader.class, shadowPicker = ShadowAsyncTaskLoader.Picker.class)
public abstract class ShadowAsyncTaskLoader<D> {

  public static class Picker extends LooperShadowPicker<ShadowAsyncTaskLoader> {

    public Picker() {
      super(ShadowLegacyAsyncTaskLoader.class, ShadowPausedAsyncTaskLoader.class);
    }
  }
}
