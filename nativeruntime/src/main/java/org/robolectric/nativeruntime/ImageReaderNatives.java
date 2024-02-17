package org.robolectric.nativeruntime;

import android.media.Image;
import android.media.ImageReader;
import android.view.Surface;

/**
 * Native methods for {@link ImageReader} JNI registration.
 *
 * <p>Native method signatures are derived from
 *
 * <pre>
 * API 33 (T, Android 13)
 * https://cs.android.com/android/platform/superproject/+/android-13.0.0_r1:frameworks/base/media/java/android/media/ImageReader.java
 *
 * API 31/32 (S, S_V2, Android 12)
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
public final class ImageReaderNatives {

  // Name must match gImageReaderClassInfo fields in android_media_ImageReader.cpp
  public long mNativeContext;

  // Name must match gImageReaderClassInfo fields in android_media_ImageReader.cpp
  public static void postEventFromNative(Object o) {
    throw new IllegalStateException("ImageReaderNatives.postEventFromNative is not implemented");
  }

  /** Returned by nativeImageSetup when acquiring the image was successful. */
  public static final int ACQUIRE_SUCCESS = 0;

  /**
   * Returned by nativeImageSetup when we couldn't acquire the buffer, because there were no buffers
   * available to acquire.
   */
  public static final int ACQUIRE_NO_BUFS = 1;

  /**
   * Returned by nativeImageSetup when we couldn't acquire the buffer because the consumer has
   * already acquired {@code maxImages} and cannot acquire more than that.
   */
  public static final int ACQUIRE_MAX_IMAGES = 2;

  public synchronized native void nativeInit(
      Object weakSelf, int w, int h, int fmt, int maxImgs, long consumerUsage); // Q-S only

  public synchronized native void nativeClose(); // Q+

  public synchronized native void nativeReleaseImage(Image i); // Q+

  public synchronized native Surface nativeGetSurface(); // Q+

  public synchronized native int nativeDetachImage(Image i); // Q+

  public synchronized native void nativeDiscardFreeBuffers(); // Q+

  /**
   * Setup image in native level.
   *
   * @return A return code {@code ACQUIRE_*}
   * @see #ACQUIRE_SUCCESS
   * @see #ACQUIRE_NO_BUFS
   * @see #ACQUIRE_MAX_IMAGES
   */
  public synchronized native int nativeImageSetup(Image i); // Q-S, U, excluding T

  /** We use a class initializer to allow the native code to cache some field offsets. */
  public static native void nativeClassInit(); // Q+

  public ImageReaderNatives() {}
}
