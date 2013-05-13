package org.robolectric.util;

/**
 *
 */
public interface Function<R, T> {
  public R call(Class<?> theClass, T value, Object[] params);
}
