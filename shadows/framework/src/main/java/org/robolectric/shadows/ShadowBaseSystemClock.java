package org.robolectric.shadows;

abstract class ShadowBaseSystemClock {

  public static class Picker extends LooperShadowPicker<ShadowBaseSystemClock> {

    public Picker() {
      super(ShadowSystemClock.class, ShadowRealisticSystemClock.class);
    }
  }
}
