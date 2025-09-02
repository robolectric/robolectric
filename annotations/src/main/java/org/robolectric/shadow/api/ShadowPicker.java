package org.robolectric.shadow.api;

import org.jspecify.annotations.Nullable;

// TODO: move this to org.robolectric.annotation
public interface ShadowPicker<T> {

  /**
   * Determines the shadow class to be used depending on the configuration of the {@link
   * org.robolectric.internal.Environment}. Must be deterministic.
   *
   * @return the shadow class to be used
   */
  @Nullable Class<? extends T> pickShadowClass();
}
