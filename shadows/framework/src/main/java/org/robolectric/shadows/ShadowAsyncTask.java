package org.robolectric.shadows;

import android.os.AsyncTask;
import org.robolectric.annotation.Implements;

/**
 * The shadow API for {@link AsyncTask}.
 *
 * @deprecated {@link AsyncTask} is deprecated in the Android SDK.
 */
@Deprecated
@Implements(value = AsyncTask.class, shadowPicker = ShadowAsyncTask.Picker.class)
public abstract class ShadowAsyncTask<Params, Progress, Result> {

  public static class Picker extends LooperShadowPicker<ShadowAsyncTask> {

    public Picker() {
      super(ShadowLegacyAsyncTask.class, ShadowPausedAsyncTask.class);
    }
  }
}
