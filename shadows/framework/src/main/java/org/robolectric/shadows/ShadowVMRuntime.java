package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.annotation.TargetApi;
import dalvik.system.VMRuntime;
import java.lang.reflect.Array;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.NativeObjRegistry;

@Implements(value = VMRuntime.class, isInAndroidSdk = false)
public class ShadowVMRuntime {

  private final NativeObjRegistry<Object> nativeObjRegistry =
      new NativeObjRegistry<>("VRRuntime.nativeObjectRegistry");
  // There actually isn't any android JNI code to call through to in Robolectric due to
  // cross-platform compatibility issues. We default to a reasonable value that reflects the devices
  // that would commonly run this code.
  private static boolean is64Bit = true;

  @Implementation(minSdk = LOLLIPOP)
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
    return nativeObjRegistry.register(obj);
  }

  /**
   * Returns the object previously registered with {@link #addressOf(Object)}.
   */
  public @Nullable
  Object getObjectForAddress(long address) {
    return nativeObjRegistry.getNativeObject(address);
  }

  /**
   * Returns whether the VM is running in 64-bit mode. Available in Android L+. Defaults to true.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected boolean is64Bit() {
    return ShadowVMRuntime.is64Bit;
  }

  /** Sets whether the VM is running in 64-bit mode. */
  @TargetApi(LOLLIPOP)
  public static void setIs64Bit(boolean is64Bit) {
    ShadowVMRuntime.is64Bit = is64Bit;
  }

  @Resetter
  public static void reset() {
    ShadowVMRuntime.is64Bit = true;
  }
}
