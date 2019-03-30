package org.robolectric.shadows;

public abstract class ShadowBaseAsyncTask {

  public static class Picker extends LooperShadowPicker<ShadowBaseAsyncTask> {

    public Picker() {
      super(ShadowAsyncTask.class, ShadowRealisticAsyncTask.class);
    }
  }
}
