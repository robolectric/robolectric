package org.robolectric.shadows;

import dalvik.system.VMRuntime;
import java.lang.reflect.Array;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = VMRuntime.class, isInAndroidSdk = false)
public class ShadowVMRuntime {

  @Implementation
  public Object newUnpaddedArray(Class<?> klass, int size) {
    return Array.newInstance(klass, size);
  }

  @Implementation
  public Object newNonMovableArray(Class<?> type, int size) {
    if (type.equals(int.class)) {
      return new int[size];
    }
    return null;
  }
}
