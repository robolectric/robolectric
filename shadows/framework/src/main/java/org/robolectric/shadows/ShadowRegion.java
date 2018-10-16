package org.robolectric.shadows;

import android.graphics.Region;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Region.class)
public class ShadowRegion {
  public static long nextId = 1;

  @HiddenApi @Implementation
  public static Number nativeConstructor() {
    return RuntimeEnvironment.castNativePtr(nextId++);
  }
}
