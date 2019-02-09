package org.robolectric.shadows;

public abstract class ShadowBaseChoreographer {

  public static class Picker extends LooperShadowPicker<ShadowBaseChoreographer> {

    public Picker() {
      super(ShadowChoreographer.class, ShadowRealisticChoreographer.class);
    }
  }
}
