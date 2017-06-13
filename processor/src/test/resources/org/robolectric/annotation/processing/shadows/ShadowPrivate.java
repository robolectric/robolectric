package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value=Robolectric.Anything.class,
            className="com.example.objects.Private")
public class ShadowPrivate {
  @Resetter
  public static void resetMethod() {
  }
}
