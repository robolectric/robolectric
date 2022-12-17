package org.robolectric.nativeruntime;

/**
 * Native methods for DiscretePathEffect JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/DiscretePathEffect.java
 */
public final class DiscretePathEffectNatives {
  public static native long nativeCreate(float length, float deviation);

  private DiscretePathEffectNatives() {}
}
