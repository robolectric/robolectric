package org.robolectric.shadows;

import android.os.Debug;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.os.Debug}.
 */
@Implements(Debug.class)
public class ShadowDebug {
  @Implementation
  public static void __staticInitializer__() {
    // Avoid calling Environment.getLegacyExternalStorageDirectory()
  }

  @Implementation
  public static long getNativeHeapAllocatedSize() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }
}
