package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Resetter;

public class ShadowResetterWithoutImplements {

  @Resetter
  public static void resetter_method() {}
}
