package org.robolectric.internal;

import javax.annotation.Nullable;

/** Interface for tracking loaded classes. */
public interface ClassTracker {
  boolean isClassLoaded(@Nullable String className);
}
