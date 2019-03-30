package org.robolectric.shadows;

public abstract class ShadowBaseAsyncTaskLoader {

  public static class Picker extends LooperShadowPicker<ShadowBaseAsyncTaskLoader> {

    public Picker() {
      super(ShadowAsyncTaskLoader.class, ShadowRealisticAsyncTaskLoader.class);
    }
  }
}
