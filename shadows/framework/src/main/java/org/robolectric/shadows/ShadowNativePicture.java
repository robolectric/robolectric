package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.Picture;
import java.io.InputStream;
import java.io.OutputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.PictureNatives;
import org.robolectric.shadows.ShadowNativePicture.Picker;

/** Shadow for {@link Picture} that is backed by native code */
@Implements(value = Picture.class, minSdk = O, shadowPicker = Picker.class, isInAndroidSdk = false)
public class ShadowNativePicture {

  @Implementation
  protected static long nativeConstructor(long nativeSrcOr0) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PictureNatives.nativeConstructor(nativeSrcOr0);
  }

  @Implementation
  protected static long nativeCreateFromStream(InputStream stream, byte[] storage) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PictureNatives.nativeCreateFromStream(stream, storage);
  }

  @Implementation
  protected static int nativeGetWidth(long nativePicture) {
    return PictureNatives.nativeGetWidth(nativePicture);
  }

  @Implementation
  protected static int nativeGetHeight(long nativePicture) {
    return PictureNatives.nativeGetHeight(nativePicture);
  }

  @Implementation
  protected static long nativeBeginRecording(long nativeCanvas, int w, int h) {
    return PictureNatives.nativeBeginRecording(nativeCanvas, w, h);
  }

  @Implementation
  protected static void nativeEndRecording(long nativeCanvas) {
    PictureNatives.nativeEndRecording(nativeCanvas);
  }

  @Implementation
  protected static void nativeDraw(long nativeCanvas, long nativePicture) {
    PictureNatives.nativeDraw(nativeCanvas, nativePicture);
  }

  @Implementation
  protected static boolean nativeWriteToStream(
      long nativePicture, OutputStream stream, byte[] storage) {
    return PictureNatives.nativeWriteToStream(nativePicture, stream, storage);
  }

  @Implementation
  protected static void nativeDestructor(long nativePicture) {
    PictureNatives.nativeDestructor(nativePicture);
  }

  /** Shadow picker for {@link Picture}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowPicture.class, ShadowNativePicture.class);
    }
  }
}
