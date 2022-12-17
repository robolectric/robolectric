package org.robolectric.nativeruntime;

/**
 * Native methods for CornerPathEffect JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/CornerPathEffect.java
 */
public final class CornerPathEffectNatives {
  public static native long nativeCreate(float radius);

  private CornerPathEffectNatives() {}
}
