package org.robolectric.shadows;

import dalvik.system.VMRuntime;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.lang.reflect.Array;

/**
 * Shadow for {@link dalvik.system.VMRuntime}.
 */
@Implements(value = VMRuntime.class, isInAndroidSdk = false)
public class ShadowVMRuntime {

  @Implementation
  public Object newUnpaddedArray(Class<?> klass, int size) {
    return Array.newInstance(klass, size);
  }
}
