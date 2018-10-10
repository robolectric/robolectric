package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** A Shadow that implements a private class name */
@Implements(className = "com.example.objects.Private")
public class ShadowPrivate {
  @Resetter
  public static void resetMethod() {
  }
}
