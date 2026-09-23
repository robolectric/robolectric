package org.robolectric.shadows;

import org.jspecify.annotations.Nullable;
import org.robolectric.shadow.api.ShadowPicker;

public class LooperShadowPicker<T> implements ShadowPicker<T> {

  private final Class<? extends T> legacyShadowClass;
  private final Class<? extends T> pausedShadowClass;
  private final Class<? extends T> runningShadowClass;

  public LooperShadowPicker(
      Class<? extends T> legacyShadowClass, Class<? extends T> pausedShadowClass) {
    this(legacyShadowClass, pausedShadowClass, pausedShadowClass);
  }

  public LooperShadowPicker(
      Class<? extends T> legacyShadowClass,
      Class<? extends T> pausedShadowClass,
      Class<? extends T> runningShadowClass) {
    this.legacyShadowClass = legacyShadowClass;
    this.pausedShadowClass = pausedShadowClass;
    this.runningShadowClass = runningShadowClass;
  }

  @Override
  @SuppressWarnings("deprecation") // This is Robolectric library code
  public @Nullable Class<? extends T> pickShadowClass() {
    switch (ShadowLooper.looperMode()) {
      case LEGACY:
        return legacyShadowClass;
      case PAUSED:
      case INSTRUMENTATION_TEST:
        return pausedShadowClass;
      case RUNNING:
        return runningShadowClass;
    }
    throw new UnsupportedOperationException("Unrecognized looperMode " + ShadowLooper.looperMode());
  }
}
