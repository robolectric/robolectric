package org.robolectric.nativeruntime;

/**
 * Native methods for NativeAllocationRegistry JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:libcore/luni/src/main/java/libcore/util/NativeAllocationRegistry.java
 */
public final class NativeAllocationRegistryNatives {
  public static native void applyFreeFunction(long freeFunction, long nativePtr);

  private NativeAllocationRegistryNatives() {}
}
