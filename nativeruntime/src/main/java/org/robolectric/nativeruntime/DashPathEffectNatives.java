package org.robolectric.nativeruntime;

/**
 * Native methods for DashPathEffect JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/DashPathEffect.java
 */
public final class DashPathEffectNatives {
  public static native long nativeCreate(float[] intervals, float phase);

  private DashPathEffectNatives() {}
}
