package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implementation;

public class ShadowImplementationWithoutImplements {

  @Implementation
  public static void implementation_method() {}
}
