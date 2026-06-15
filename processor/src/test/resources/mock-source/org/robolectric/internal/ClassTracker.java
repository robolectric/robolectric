package org.robolectric.internal;

/** Interface for tracking loaded classes. */
public interface ClassTracker {
  boolean isClassLoaded(String className);
}
