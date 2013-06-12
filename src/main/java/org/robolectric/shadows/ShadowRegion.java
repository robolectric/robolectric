package org.robolectric.shadows;

import android.graphics.Region;
import org.robolectric.annotation.Implements;

@Implements(Region.class)
public class ShadowRegion {
  public static int nextId = 1;

  public static int nativeConstructor() {
    return nextId++;
  }
}
