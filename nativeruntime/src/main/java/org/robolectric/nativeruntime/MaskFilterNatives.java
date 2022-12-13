package org.robolectric.nativeruntime;

/**
 * Native methods for MaskFilter JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/MaskFilter.java
 */
public final class MaskFilterNatives {

  public static native void nativeDestructor(long nativeFilter);

  private MaskFilterNatives() {}
}
