package org.robolectric.nativeruntime;

/**
 * Native methods for Color JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Color.java
 */
public final class ColorNatives {

  public static native void nativeRGBToHSV(int red, int greed, int blue, float[] hsv);

  public static native int nativeHSVToColor(int alpha, float[] hsv);

  private ColorNatives() {}
}
