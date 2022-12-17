package org.robolectric.nativeruntime;

/**
 * Native methods for Shader JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Shader.java
 */
public final class ShaderNatives {

  public static native long nativeGetFinalizer();

  private ShaderNatives() {}
}
