package org.robolectric.shadows;

/**
 * The shadow API for {@link android.os.AsyncTask}
 */
public abstract class ShadowAsyncTask {

  public static class Picker extends LooperShadowPicker<ShadowAsyncTask> {

    public Picker() {
      super(ShadowLegacyAsyncTask.class, ShadowPausedAsyncTask.class);
    }
  }
}
