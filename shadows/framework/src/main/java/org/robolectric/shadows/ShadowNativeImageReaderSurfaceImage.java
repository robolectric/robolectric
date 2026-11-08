package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.media.Image.Plane;
import java.nio.ByteBuffer;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.ImageReaderSurfaceImageNatives;
import org.robolectric.shadows.ShadowImageReader.ShadowSurfaceImage;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@code ImageReader.SurfaceImage} that is backed by native code. */
@Implements(
    className = "android.media.ImageReader$SurfaceImage",
    minSdk = P,
    isInAndroidSdk = false,
    shadowPicker = ShadowNativeImageReaderSurfaceImage.Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeImageReaderSurfaceImage {

  @RealObject private Object realSurfaceImage;
  private Plane[] rgbaPlanes;

  /** Exposes RGBA_8888 planes in RGBA order unless {@code robolectric.useRgbaPixelOrder=false}. */
  @Implementation
  protected Plane[] getPlanes() {
    Plane[] planes = reflector(SurfaceImageReflector.class, realSurfaceImage).getPlanes();
    if (!ShadowNativeBitmap.shouldSwapRedAndBlue()
        || ((Image) realSurfaceImage).getFormat() != PixelFormat.RGBA_8888) {
      return planes;
    }
    if (rgbaPlanes == null) {
      rgbaPlanes = new Plane[planes.length];
      for (int i = 0; i < planes.length; i++) {
        rgbaPlanes[i] = rgbaPlane(planes[i]);
      }
    }
    return rgbaPlanes.clone();
  }

  // Internal screenshots use host-order pixels on both sides.
  static void copyPixelsToBitmap(Image image, Bitmap bitmap) {
    Plane[] planes = reflector(SurfaceImageReflector.class, image).getPlanes();
    reflector(ShadowNativeBitmap.BitmapBufferReflector.class, bitmap)
        .copyPixelsFromBuffer(planes[0].getBuffer().duplicate());
  }

  private static Plane rgbaPlane(Plane plane) {
    ByteBuffer source = plane.getBuffer();
    ByteBuffer copy = ByteBuffer.allocateDirect(source.remaining()).order(source.order());
    copy.put(source.duplicate()).flip();
    ShadowNativeBitmap.swapRedAndBlue(copy, 0, copy.limit());
    ByteBuffer rgba = source.isReadOnly() ? copy.asReadOnlyBuffer().order(source.order()) : copy;
    return new Plane() {
      @Override
      public int getRowStride() {
        return plane.getRowStride();
      }

      @Override
      public int getPixelStride() {
        return plane.getPixelStride();
      }

      @Override
      public ByteBuffer getBuffer() {
        plane.getBuffer();
        return rgba;
      }
    };
  }

  @Implementation(maxSdk = R)
  protected synchronized @ClassName("android.media.ImageReader$SurfaceImage$SurfacePlane[]") Object
      nativeCreatePlanes(int numPlanes, int readerFormat) {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageCreatePlanes(
        realSurfaceImage, numPlanes, readerFormat, /* readerUsage= */ 0);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected synchronized @ClassName("android.media.ImageReader$SurfaceImage$SurfacePlane[]") Object
      nativeCreatePlanes(int numPlanes, int readerFormat, long readerUsage) {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageCreatePlanes(
        realSurfaceImage, numPlanes, readerFormat, readerUsage);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected synchronized int nativeGetWidth() {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageGetWidth(realSurfaceImage);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected synchronized int nativeGetHeight() {
    return ImageReaderSurfaceImageNatives.nativeSurfaceImageGetHeight(realSurfaceImage);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
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

  @ForType(className = "android.media.ImageReader$SurfaceImage")
  interface SurfaceImageReflector {
    @Direct
    Plane[] getPlanes();
  }
}
