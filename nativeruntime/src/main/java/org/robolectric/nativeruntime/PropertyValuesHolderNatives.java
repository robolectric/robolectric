package org.robolectric.nativeruntime;

/**
 * Native methods for PropertyValuesHolder JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/PropertyValuesHolder.java
 */
public final class PropertyValuesHolderNatives {

  public static native long nGetIntMethod(Class<?> targetClass, String methodName);

  public static native long nGetFloatMethod(Class<?> targetClass, String methodName);

  public static native long nGetMultipleIntMethod(
      Class<?> targetClass, String methodName, int numParams);

  public static native long nGetMultipleFloatMethod(
      Class<?> targetClass, String methodName, int numParams);

  public static native void nCallIntMethod(Object target, long methodID, int arg);

  public static native void nCallFloatMethod(Object target, long methodID, float arg);

  public static native void nCallTwoIntMethod(Object target, long methodID, int arg1, int arg2);

  public static native void nCallFourIntMethod(
      Object target, long methodID, int arg1, int arg2, int arg3, int arg4);

  public static native void nCallMultipleIntMethod(Object target, long methodID, int[] args);

  public static native void nCallTwoFloatMethod(
      Object target, long methodID, float arg1, float arg2);

  public static native void nCallFourFloatMethod(
      Object target, long methodID, float arg1, float arg2, float arg3, float arg4);

  public static native void nCallMultipleFloatMethod(Object target, long methodID, float[] args);

  private PropertyValuesHolderNatives() {}
}
