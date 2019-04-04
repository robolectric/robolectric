package org.robolectric.shadows.support.v4;

import android.support.v4.content.AsyncTaskLoader;
import org.robolectric.shadows.LooperShadowPicker;

/**
 * The shadow API for {@link AsyncTaskLoader}
 */
public abstract class ShadowAsyncTaskLoader {

  public static class Picker extends LooperShadowPicker<ShadowAsyncTaskLoader> {

    public Picker() {
      super(ShadowLegacyAsyncTaskLoader.class, ShadowPausedAsyncTaskLoader.class);
    }
  }
}
