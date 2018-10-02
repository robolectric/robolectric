package org.robolectric.shadows;

import android.graphics.NinePatch;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(NinePatch.class)
public class ShadowNinePatch {
  @Implementation
  public static boolean isNinePatchChunk(byte[] chunk) {
    return chunk != null;
  }
}
