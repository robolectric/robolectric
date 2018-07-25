package org.robolectric.shadow.api;

public interface ShadowFactory<T> {
  T newInstance();
}
