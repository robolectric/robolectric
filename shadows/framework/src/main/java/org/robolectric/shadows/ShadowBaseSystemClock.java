package org.robolectric.shadows;

/** The abstract class for SystemClock shadow class */
public abstract class ShadowBaseSystemClock {

  /** The LooperShadowPicker class to choose to use which SystemClock shadow class */
  public static class Picker extends LooperShadowPicker<ShadowBaseSystemClock> {

    public Picker() {
      super(ShadowSystemClock.class, ShadowRealisticSystemClock.class);
    }
  }
}
