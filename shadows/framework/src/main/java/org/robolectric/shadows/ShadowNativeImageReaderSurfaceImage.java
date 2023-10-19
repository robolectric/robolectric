package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.ImageReaderSurfaceImageNatives;
import org.robolectric.shadows.ShadowImageReader.ShadowSurfaceImage;

/** Shadow for {@code ImageReader.SurfaceImage} that is backed by native code. */
@Implements(
    className = "android.media.ImageReader$SurfaceImage",
    minSdk = Q,
    looseSignatures = true,
    isInAndroidSdk = false,
    shadowPicker = ShadowNativeImageReaderSurfaceImage.Picker.class)
public class ShadowNativeImageReaderSurfaceImage {

  @RealObject private Object realSurfaceImage;

  @Implementation(maxSdk = R)
  protected synchronized /*SurfacePlane[]*/ Object nativeCreatePlanes(
      /*int*/ Object numPlanes, /*int*/ Object readerFormat) {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageCreatePlanes(
        realSurfaceImage, (int) numPlanes, (int) readerFormat, /* readerUsage= */ 0);
  }

  @Implementation(minSdk = S)
  protected synchronized /*SurfacePlane[]*/ Object nativeCreatePlanes(
      /*int*/ Object numPlanes, /*int*/ Object readerFormat, /*long*/ Object readerUsage) {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageCreatePlanes(
        realSurfaceImage, (int) numPlanes, (int) readerFormat, (long) readerUsage);
  }

  @Implementation
  protected synchronized int nativeGetWidth() {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageGetWidth(realSurfaceImage);
  }

  @Implementation
  protected synchronized int nativeGetHeight() {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageGetHeight(realSurfaceImage);
  }

  @Implementation
  protected synchronized int nativeGetFormat(int readerFormat) {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageGetFormat(
        realSurfaceImage, readerFormat);
  }

  /** Shadow picker for {@code ImageReader.SurfaceImage}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowSurfaceImage.class, ShadowNativeImageReaderSurfaceImage.class);
    }

    @Override
    protected int getMinApiLevel() {
      return S;
    }
  }
}
