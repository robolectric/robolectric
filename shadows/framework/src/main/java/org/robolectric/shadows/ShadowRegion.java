package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Region;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(Region.class)
public class ShadowRegion {
  @RealObject Region realRegion;

  public static long nextId = 1;

  /**
   * The real {@link Region#equals(Object)} calls into native code, which is a no-op in Robolectric,
   * and will always return false no matter what is compared. We can special-case some simple
   * scenarios here.
   */
  @Implementation
  @SuppressWarnings("EqualsHashCode")
  public boolean equals(Object obj) {
    if (obj == realRegion) {
      return true;
    }
    if (!(obj instanceof Region)) {
      return false;
    }
    return reflector(RegionReflector.class, realRegion).equals(obj);
  }

  @HiddenApi
  @Implementation
  protected static Number nativeConstructor() {
    return RuntimeEnvironment.castNativePtr(nextId++);
  }

  @ForType(Region.class)
  interface RegionReflector {
    @Direct
    boolean equals(Object obj);
  }
}
