package org.robolectric.shadows;

import org.robolectric.shadow.api.ShadowPicker;

public class LooperShadowPicker<T> implements ShadowPicker<T> {

  private Class<? extends T> legacyShadowClass;
  private Class<? extends T> pausedShadowClass;

  public LooperShadowPicker(
      Class<? extends T> legacyShadowClass, Class<? extends T> pausedShadowClass) {
    this.legacyShadowClass = legacyShadowClass;
    this.pausedShadowClass = pausedShadowClass;
  }

  @Override
  @SuppressWarnings("deprecation") // This is Robolectric library code
  public Class<? extends T> pickShadowClass() {
    switch (ShadowLooper.looperMode()) {
      case LEGACY:
        return legacyShadowClass;
      case PAUSED:
      case INSTRUMENTATION_TEST:
        return pausedShadowClass;
    }
    throw new UnsupportedOperationException("Unrecognized looperMode " + ShadowLooper.looperMode());
  }
}
