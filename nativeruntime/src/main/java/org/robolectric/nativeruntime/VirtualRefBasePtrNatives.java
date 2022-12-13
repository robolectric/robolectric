package org.robolectric.nativeruntime;

/**
 * Native methods for VirtualRefBasePtr JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/VirtualRefBasePtr.java
 */
public final class VirtualRefBasePtrNatives {

  public static native void nIncStrong(long ptr);

  public static native void nDecStrong(long ptr);

  private VirtualRefBasePtrNatives() {}
}
