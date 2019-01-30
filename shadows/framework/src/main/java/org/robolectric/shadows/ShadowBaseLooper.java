package org.robolectric.shadows;

public abstract class ShadowBaseLooper {

  public static class Picker extends LooperShadowPicker<ShadowBaseLooper> {

    public Picker() {
      super(ShadowLooper.class, ShadowSimplifiedLooper.class);
    }
  }
}
