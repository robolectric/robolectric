package org.robolectric.nativeruntime;

/**
 * Native methods for SumPathEffect JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/SumPathEffect.java
 */
public final class SumPathEffectNatives {

  public static native long nativeCreate(long first, long second);

  private SumPathEffectNatives() {}
}
