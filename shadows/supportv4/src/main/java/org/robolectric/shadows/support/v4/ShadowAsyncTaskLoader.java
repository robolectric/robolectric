package org.robolectric.shadows.support.v4;

import androidx.loader.content.AsyncTaskLoader;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.LooperShadowPicker;

/**
 * The shadow API for {@link AsyncTaskLoader}.
 *
 * Different shadow implementations will be used based on the current {@link LooperMode.Mode}.
 * @see ShadowLegacyAsyncTaskLoader, ShadowPausedAsyncTaskLoader
 */
@Implements(value = AsyncTaskLoader.class, shadowPicker = ShadowAsyncTaskLoader.Picker.class)
public abstract class ShadowAsyncTaskLoader<D> {

  public static class Picker extends LooperShadowPicker<ShadowAsyncTaskLoader> {

    public Picker() {
      super(ShadowLegacyAsyncTaskLoader.class, ShadowPausedAsyncTaskLoader.class);
    }
  }
}
