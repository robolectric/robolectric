package org.robolectric.shadows;

import android.os.Debug;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Debug.class)
public class ShadowDebug {
  @Implementation
  public static long getNativeHeapAllocatedSize() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }
}
