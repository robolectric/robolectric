package org.robolectric.shadows;

import android.graphics.NinePatch;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(NinePatch.class)
public class ShadowNinePatch {
    @Implementation
    public static boolean isNinePatchChunk(byte[] chunk) {
        return chunk != null;
    }
}
