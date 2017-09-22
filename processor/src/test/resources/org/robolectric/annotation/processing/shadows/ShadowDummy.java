package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import com.example.objects.Dummy;

@Implements(Dummy.class)
public class ShadowDummy {
  public static int resetCount = 0;
  @Resetter
  public static void resetter_method() {
    resetCount++;
  }
}
