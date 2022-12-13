package org.robolectric.nativeruntime;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Native methods for Picture JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Picture.java
 */
public class PictureNatives {

  public static native long nativeConstructor(long nativeSrcOr0);

  public static native long nativeCreateFromStream(InputStream stream, byte[] storage);

  public static native int nativeGetWidth(long nativePicture);

  public static native int nativeGetHeight(long nativePicture);

  public static native long nativeBeginRecording(long nativeCanvas, int w, int h);

  public static native void nativeEndRecording(long nativeCanvas);

  public static native void nativeDraw(long nativeCanvas, long nativePicture);

  public static native boolean nativeWriteToStream(
      long nativePicture, OutputStream stream, byte[] storage);

  public static native void nativeDestructor(long nativePicture);
}
