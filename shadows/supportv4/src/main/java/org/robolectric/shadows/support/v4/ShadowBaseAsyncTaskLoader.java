package org.robolectric.shadows.support.v4;

import org.robolectric.shadows.LooperShadowPicker;

public abstract class ShadowBaseAsyncTaskLoader {

  public static class Picker extends LooperShadowPicker<ShadowBaseAsyncTaskLoader> {

    public Picker() {
      super(ShadowAsyncTaskLoader.class, ShadowRealisticAsyncTaskLoader.class);
    }
  }
}
