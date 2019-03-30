package org.robolectric.shadows;

abstract class ShadowBaseMessageQueue {

  public static class Picker extends LooperShadowPicker<ShadowBaseMessageQueue> {

    public Picker() {
      super(ShadowMessageQueue.class, ShadowRealisticMessageQueue.class);
    }
  }
}
