package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.annotation.processing.objects.Dummy;

@Implements(Dummy.class)
public class ShadowResetterNonStatic {

  @Resetter
  public void resetter_method() {}
}
