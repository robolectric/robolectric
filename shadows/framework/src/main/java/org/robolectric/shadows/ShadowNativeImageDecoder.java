package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.content.res.AssetManager.AssetInputStream;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.ImageDecoder;
import android.graphics.ImageDecoder.Source;
import android.graphics.Rect;
import android.util.Size;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.ImageDecoderNatives;
import org.robolectric.shadows.ShadowNativeImageDecoder.Picker;

/** Shadow for {@link android.graphics.ImageDecoder} that is backed by native code */
@Implements(value = ImageDecoder.class, minSdk = P, shadowPicker = Picker.class)
public class ShadowNativeImageDecoder {

  static {
    DefaultNativeRuntimeLoader.injectAndLoad();
  }

  @Implementation(minSdk = P, maxSdk = Q)
  protected static ImageDecoder createFromAsset(AssetInputStream ais, Source source)
      throws IOException {
    return createFromAsset(ais, false, source);
  }

  @Implementation(minSdk = R)
  protected static ImageDecoder createFromAsset(
      AssetInputStream ais, boolean preferAnimation, Source source) throws IOException {
    int capacity = ais.available();
    ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);
    buffer.order(ByteOrder.nativeOrder());
    byte[] buf = new byte[8 * 1024]; // 8k
    int bytesRead;
    while ((bytesRead = ais.read(buf)) != -1) {
      buffer.put(buf, 0, bytesRead);
    }
    if (ais.read() != -1) {
      throw new IOException("Unable to access full contents of asset");
    }
    return nCreate(buffer, 0, bytesRead, preferAnimation, source);
  }

  @Implementation(minSdk = P, maxSdk = Q)
  protected static ImageDecoder nCreate(long asset, Source src) throws IOException {
    return nCreate(asset, false, src);
  }

  @Implementation(minSdk = R)
  protected static ImageDecoder nCreate(long asset, boolean preferAnimation, Source src)
      throws IOException {
    throw new UnsupportedEncodingException();
  }

  @Implementation(minSdk = P, maxSdk = Q)
  protected static ImageDecoder nCreate(ByteBuffer buffer, int position, int limit, Source src)
      throws IOException {
    return nCreate(buffer, position, limit, false, src);
  }

  @Implementation(minSdk = R)
  protected static ImageDecoder nCreate(
      ByteBuffer buffer, int position, int limit, boolean preferAnimation, Source src)
      throws IOException {
    return ImageDecoderNatives.nCreate(buffer, position, limit, preferAnimation, src);
  }

  @Implementation(minSdk = P, maxSdk = Q)
  protected static ImageDecoder nCreate(byte[] data, int offset, int length, Source src)
      throws IOException {
    return nCreate(data, offset, length, false, src);
  }

  @Implementation(minSdk = R)
  protected static ImageDecoder nCreate(
      byte[] data, int offset, int length, boolean preferAnimation, Source src) throws IOException {
    return ImageDecoderNatives.nCreate(data, offset, length, preferAnimation, src);
  }

  @Implementation(minSdk = P, maxSdk = Q)
  protected static ImageDecoder nCreate(InputStream is, byte[] storage, Source src)
      throws IOException {
    return nCreate(is, storage, false, src);
  }

  @Implementation(minSdk = R)
  protected static ImageDecoder nCreate(
      InputStream is, byte[] storage, boolean preferAnimation, Source src) throws IOException {
    return ImageDecoderNatives.nCreate(is, storage, preferAnimation, src);
  }

  @Implementation(maxSdk = Q)
  protected static ImageDecoder nCreate(FileDescriptor fd, Source src) throws IOException {
    throw new UnsupportedEncodingException();
  }

  @Implementation(minSdk = S)
  protected static ImageDecoder nCreate(
      FileDescriptor fd, long length, boolean preferAnimation, Source src) throws IOException {
    return ImageDecoderNatives.nCreate(fd, length, preferAnimation, src);
  }

  @Implementation(minSdk = P, maxSdk = P)
  protected static Bitmap nDecodeBitmap(
      long nativePtr,
      ImageDecoder decoder,
      boolean doPostProcess,
      int width,
      int height,
      Rect cropRect,
      boolean mutable,
      int allocator,
      boolean unpremulRequired,
      boolean conserveMemory,
      boolean decodeAsAlphaMask,
      ColorSpace desiredColorSpace)
      throws IOException {
    return nDecodeBitmap(
        nativePtr,
        decoder,
        doPostProcess,
        width,
        height,
        cropRect,
        mutable,
        allocator,
        unpremulRequired,
        conserveMemory,
        decodeAsAlphaMask,
        /* desiredColorSpace = */ 0, // Desired color space is currently not supported in P.
        /* extended = */ false);
  }

  @Implementation(minSdk = Q)
  protected static Bitmap nDecodeBitmap(
      long nativePtr,
      ImageDecoder decoder,
      boolean doPostProcess,
      int width,
      int height,
      Rect cropRect,
      boolean mutable,
      int allocator,
      boolean unpremulRequired,
      boolean conserveMemory,
      boolean decodeAsAlphaMask,
      long desiredColorSpace,
      boolean extended)
      throws IOException {
    return ImageDecoderNatives.nDecodeBitmap(
        nativePtr,
        decoder,
        doPostProcess,
        width,
        height,
        cropRect,
        mutable,
        allocator,
        unpremulRequired,
        conserveMemory,
        decodeAsAlphaMask,
        desiredColorSpace,
        extended);
  }

  @Implementation
  protected static Size nGetSampledSize(long nativePtr, int sampleSize) {
    return ImageDecoderNatives.nGetSampledSize(nativePtr, sampleSize);
  }

  @Implementation
  protected static void nGetPadding(long nativePtr, Rect outRect) {
    ImageDecoderNatives.nGetPadding(nativePtr, outRect);
  }

  @Implementation
  protected static void nClose(long nativePtr) {
    ImageDecoderNatives.nClose(nativePtr);
  }

  @Implementation
  protected static String nGetMimeType(long nativePtr) {
    return ImageDecoderNatives.nGetMimeType(nativePtr);
  }

  @Implementation
  protected static ColorSpace nGetColorSpace(long nativePtr) {
    return ImageDecoderNatives.nGetColorSpace(nativePtr);
  }

  /** Shadow picker for {@link ImageDecoder}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowImageDecoder.class, ShadowNativeImageDecoder.class);
    }
  }
}
