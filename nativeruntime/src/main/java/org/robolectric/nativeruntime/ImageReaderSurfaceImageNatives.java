package org.robolectric.nativeruntime;

import android.media.ImageReader;

/**
 * Native methods for {@link ImageReader} JNI registration.
 *
 * <p>Native method signatures are derived from:
 *
 * <pre>
 * API 31/32 (S, S_V2, Android 12, all above)
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/media/java/android/media/ImageReader.java
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/media/jni/android_media_ImageReader.cpp
 *
 * API 30 (R, Android 11)
 * https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/media/java/android/media/ImageReader.java
 *
 * API 29 (Q, Android 10)
 * https://cs.android.com/android/platform/superproject/+/android-10.0.0_r1:frameworks/base/media/java/android/media/ImageReader.java
 * </pre>
 */
public final class ImageReaderSurfaceImageNatives {

  public synchronized native /*SurfacePlane[]*/ Object[] nativeCreatePlanes(
      int numPlanes, int readerFormat, long readerUsage); // S+, not Q or R

  public synchronized native int nativeGetWidth();

  public synchronized native int nativeGetHeight();

  public synchronized native int nativeGetFormat(int readerFormat);

  /**
   * RNG-specific native trampoline methods to invoke the native member functions on the proper
   * SurfaceImage object reference.
   */
  public static native Object[] nativeSurfaceImageCreatePlanes(
      Object realSurfaceImage, int numPlanes, int readerFormat, long readerUsage);

  public static native int nativeSurfaceImageGetWidth(Object realSurfaceImage);

  public static native int nativeSurfaceImageGetHeight(Object realSurfaceImage);

  public static native int nativeSurfaceImageGetFormat(Object realSurfaceImage, int readerFormat);

  public ImageReaderSurfaceImageNatives() {}
}
