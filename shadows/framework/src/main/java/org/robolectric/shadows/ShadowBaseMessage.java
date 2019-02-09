package org.robolectric.shadows;

public abstract class ShadowBaseMessage {

  public static class Picker extends LooperShadowPicker<ShadowBaseMessage> {

    public Picker() {
      super(ShadowMessage.class, ShadowRealisticMessage.class);
    }
  }
}
