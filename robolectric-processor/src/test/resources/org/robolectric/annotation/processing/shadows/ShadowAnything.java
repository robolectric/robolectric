package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.annotation.processing.objects.Dummy;

@Implements(value = Robolectric.Anything.class,
            className = "org.robolectric.annotation.processing.objects.AnyObject")
public class ShadowAnything {
  public static int resetCount = 0;
  @Resetter
  public static void anotherResetter() {
    resetCount++;
  }
}
