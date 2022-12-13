package org.robolectric.nativeruntime;

/**
 * Native methods for ComposeShader JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/ComposeShader.java
 */
public class ComposeShaderNatives {
  public static native long nativeCreate(
      long nativeMatrix, long nativeShaderA, long nativeShaderB, int porterDuffMode);

  private ComposeShaderNatives() {}
}
