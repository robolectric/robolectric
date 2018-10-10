package org.robolectric.shadow.api;

public interface ShadowPicker<T> {
  Class<? extends T> pickShadowClass();
}
