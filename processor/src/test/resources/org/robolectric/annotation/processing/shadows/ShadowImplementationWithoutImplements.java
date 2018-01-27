package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implementation;

public class ShadowImplementationWithoutImplements {

  @Implementation
  protected static void implementation_method() {}
}
