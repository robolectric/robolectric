package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;

@Implements(Object.class)
public class ShadowFilterWithNonVoidReturn {
  @Filter
  protected String someMethod() {
    return "foo";
  }
}
