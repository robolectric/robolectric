package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.hardware.HardwareBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.ImageReaderSurfaceImageNatives;
import org.robolectric.shadows.ShadowImageReader.ShadowSurfaceImage;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@code ImageReader.SurfaceImage} that is backed by native code. */
@Implements(
    className = "android.media.ImageReader$SurfaceImage",
    minSdk = P,
    looseSignatures = true,
    isInAndroidSdk = false,
    shadowPicker = ShadowNativeImageReaderSurfaceImage.Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeImageReaderSurfaceImage {

  @RealObject private Object realSurfaceImage;

  @Implementation(maxSdk = R)
  protected synchronized /*SurfacePlane[]*/ Object nativeCreatePlanes(
      /*int*/ Object numPlanes, /*int*/ Object readerFormat) {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageCreatePlanes(
        realSurfaceImage, (int) numPlanes, (int) readerFormat, /* readerUsage= */ 0);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected synchronized /*SurfacePlane[]*/ Object nativeCreatePlanes(
      /*int*/ Object numPlanes, /*int*/ Object readerFormat, /*long*/ Object readerUsage) {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageCreatePlanes(
        realSurfaceImage, (int) numPlanes, (int) readerFormat, (long) readerUsage);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected synchronized int nativeGetWidth() {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageGetWidth(realSurfaceImage);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected synchronized int nativeGetHeight() {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageGetHeight(realSurfaceImage);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected synchronized int nativeGetFormat(int readerFormat) {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageGetFormat(
        realSurfaceImage, readerFormat);
  }

  @Implementation
  protected synchronized HardwareBuffer nativeGetHardwareBuffer() {
    return null; // TODO(hoisie): add an implementation
  }

  /** Shadow picker for {@code ImageReader.SurfaceImage}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowSurfaceImage.class, ShadowNativeImageReaderSurfaceImage.class);
    }

    @Override
    protected int getMinApiLevel() {
      return P;
    }
  }
}
