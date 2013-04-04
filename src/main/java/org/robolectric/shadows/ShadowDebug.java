package org.robolectric.shadows;

import android.os.Debug;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(Debug.class)
public class ShadowDebug {
    @Implementation
    public static long getNativeHeapAllocatedSize() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}
