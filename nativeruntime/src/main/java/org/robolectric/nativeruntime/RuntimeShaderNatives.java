package org.robolectric.nativeruntime;

/**
 * Native methods for RuntimeShader JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/RuntimeShader.java
 */
public class RuntimeShaderNatives {

  public static native long nativeGetFinalizer();

  public static native long nativeCreateBuilder(String sksl);

  public static native long nativeCreateShader(long shaderBuilder, long matrix, boolean isOpaque);

  public static native void nativeUpdateUniforms(
      long shaderBuilder, String uniformName, float[] uniforms);

  public static native void nativeUpdateShader(long shaderBuilder, String shaderName, long shader);

  private RuntimeShaderNatives() {}
}
