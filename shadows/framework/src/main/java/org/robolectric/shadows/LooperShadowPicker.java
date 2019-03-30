package org.robolectric.shadows;

import org.robolectric.shadow.api.ShadowPicker;

public class LooperShadowPicker<T> implements ShadowPicker<T> {

  private Class<? extends T> legacyShadowClass;
  private Class<? extends T> deterministicShadowClass;

  public LooperShadowPicker(
      Class<? extends T> legacyShadowClass, Class<? extends T> deterministicShadowClass) {
    this.legacyShadowClass = legacyShadowClass;
    this.deterministicShadowClass = deterministicShadowClass;
  }

  @Override
  public Class<? extends T> pickShadowClass() {
    if (ShadowBaseLooper.useRealisticLooper()) {
      return deterministicShadowClass;
    } else {
      return legacyShadowClass;
    }
  }
}
