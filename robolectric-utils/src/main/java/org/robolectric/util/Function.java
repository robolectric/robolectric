package org.robolectric.util;

/**
 * Interface defining a function object.
 */
public interface Function<R, T> {
  R call(Class<?> theClass, T value, Object[] params);
}
