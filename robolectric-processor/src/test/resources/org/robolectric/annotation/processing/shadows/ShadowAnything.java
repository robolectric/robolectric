package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value = Robolectric.Anything.class,
            className = "com.example.objects.AnyObject")
public class ShadowAnything {
  public static int resetCount = 0;
  @Resetter
  public static void anotherResetter() {
    resetCount++;
  }
}
