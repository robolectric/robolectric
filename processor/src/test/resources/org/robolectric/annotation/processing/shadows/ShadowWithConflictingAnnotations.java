package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Object.class)
public class ShadowWithConflictingAnnotations {
  @Implementation
  @Filter
  protected void someMethod() {}
}
