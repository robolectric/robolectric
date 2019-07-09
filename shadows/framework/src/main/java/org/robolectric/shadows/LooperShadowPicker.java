package org.robolectric.shadows;

import org.robolectric.annotation.LooperMode;
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
  public Class<? extends T> pickShadowClass() {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      return pausedShadowClass;
    } else {
      return legacyShadowClass;
    }
  }
}
