package org.robolectric.shadows;

import dalvik.system.VMRuntime;
import java.lang.reflect.Array;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = VMRuntime.class, isInAndroidSdk = false)
public class ShadowVMRuntime {

  private NativeObjRegistry<Object> nativeObjRegistry = new NativeObjRegistry<>();

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

  /**
   * Returns a unique identifier of the object instead of a 'native' address.
   */
  @Implementation
  public long addressOf(Object obj) {
    return nativeObjRegistry.getNativeObjectId(obj);
  }

  /**
   * Returns the object previously registered with {@link #addressOf(Object)}.
   */
  public @Nullable
  Object getObjectForAddress(long address) {
    return nativeObjRegistry.getNativeObject(address);
  }
}
