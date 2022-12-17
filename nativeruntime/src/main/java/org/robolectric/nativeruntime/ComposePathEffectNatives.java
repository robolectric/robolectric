package org.robolectric.nativeruntime;

/**
 * Native methods for ComposePathEffect JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/ComposePathEffect.java
 */
public final class ComposePathEffectNatives {

  public static native long nativeCreate(long nativeOuterpe, long nativeInnerpe);

  private ComposePathEffectNatives() {}
}
