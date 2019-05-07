package org.robolectric.shadows;

import android.content.res.AssetManager;
import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.graphics.ImageDecoder;
import android.graphics.ImageDecoder.DecodeException;
import android.graphics.ImageDecoder.Source;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Size;
import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/jni/android/graphics/ImageDecoder.cpp
@SuppressWarnings({"NewApi", "UnusedDeclaration"})
// ImageDecoder is in fact in SDK, but make it false for now so projects which compile against < P
// still work
@Implements(value = ImageDecoder.class, isInAndroidSdk = false, minSdk = Build.VERSION_CODES.P)
public class ShadowImageDecoder {

  private abstract static class ImgStream {

    private final int width;
    private final int height;
    private final boolean animated = false;
    private final boolean ninePatch;

    ImgStream() {
      InputStream inputStream = getInputStream();
      final Point size = ImageUtil.getImageSizeFromStream(inputStream);
      this.width = size == null ? 10 : size.x;
      this.height = size == null ? 10 : size.y;
      if (inputStream instanceof AssetManager.AssetInputStream) {
        ShadowAssetInputStream sis = Shadow.extract(inputStream);
        this.ninePatch = sis.isNinePatch();
      } else {
        this.ninePatch = false;
      }
    }

    protected abstract InputStream getInputStream();

    int getWidth() {
      return width;
    }

    int getHeight() {
      return height;
    }

    boolean isAnimated() {
      return animated;
    }

    boolean isNinePatch() {
      return ninePatch;
    }
  }

  private static final class CppImageDecoder {

    private final ImgStream imgStream;

    CppImageDecoder(ImgStream imgStream) {
      this.imgStream = imgStream;
    }

  }

  private static final NativeObjRegistry<CppImageDecoder> NATIVE_IMAGE_DECODER_REGISTRY =
      new NativeObjRegistry<>(CppImageDecoder.class);

  @RealObject private ImageDecoder realObject;

  private static ImageDecoder jniCreateDecoder(ImgStream imgStream) {
    CppImageDecoder cppImageDecoder = new CppImageDecoder(imgStream);
    long cppImageDecoderPtr = NATIVE_IMAGE_DECODER_REGISTRY.register(cppImageDecoder);
    return ReflectionHelpers.callConstructor(
        ImageDecoder.class,
        ClassParameter.from(long.class, cppImageDecoderPtr),
        ClassParameter.from(int.class, imgStream.getWidth()),
        ClassParameter.from(int.class, imgStream.getHeight()),
        ClassParameter.from(boolean.class, imgStream.isAnimated()),
        ClassParameter.from(boolean.class, imgStream.isNinePatch()));
  }

  static ImageDecoder ImageDecoder_nCreateFd(
      FileDescriptor fileDescriptor, Source source) {
    throw new UnsupportedOperationException();
    // int descriptor = jniGetFDFromFileDescriptor(fileDescriptor);
    // struct stat fdStat;
    // if (fstat(descriptor, &fdStat) == -1) {
    //   throw_exception(ShadowImageDecoder.Error.kSourceMalformedData,
    //       "broken file descriptor; fstat returned -1", null, source);
    // }
    // int dupDescriptor = dup(descriptor);
    // FILE* file = fdopen(dupDescriptor, "r");
    // if (file == NULL) {
    //   close(dupDescriptor);
    //   throw_exception(ShadowImageDecoder.Error.kSourceMalformedData, "Could not open file",
    //       null, source);
    // }
    // SkFILEStream fileStream(new SkFILEStream(file));
    // return native_create(fileStream, source);
  }

  static ImageDecoder ImageDecoder_nCreateInputStream(
      InputStream is, byte[] storage, Source source) {
    // SkStream stream = CreateJavaInputStreamAdaptor(is, storage, false);
    // if (!isTruthy(stream)) {
    //   throw_exception(ShadowImageDecoder.Error.kSourceMalformedData, "Failed to create a stream",
    //       null, source);
    // }
    // SkStream bufferedStream =
    //     SkFrontBufferedStream.Make(stream,
    //     SkCodec.MinBufferedBytesNeeded()));
    // return native_create(bufferedStream, source);

    return jniCreateDecoder(new ImgStream() {
      @Override
      protected InputStream getInputStream() {
        return is;
      }
    });
  }

  static ImageDecoder ImageDecoder_nCreateAsset(long asset_ptr,
      Source source) throws DecodeException {
    // Asset* asset = reinterpret_cast<Asset*>(assetPtr);
    // SkStream stream = new AssetStreamAdaptor(asset);
    // return jniCreateDecoder(stream, source);
    Resources resources = ReflectionHelpers.getField(source, "mResources");
    AssetInputStream assetInputStream = ShadowAssetInputStream.createAssetInputStream(
        null, asset_ptr, resources.getAssets());
    return jniCreateDecoder(
        new ImgStream() {
          @Override
          protected InputStream getInputStream() {
            return assetInputStream;
          }
        });
  }

  static ImageDecoder ImageDecoder_nCreateByteBuffer(ByteBuffer jbyteBuffer,
      int initialPosition, int limit, Source source) throws DecodeException {
    // SkStream stream = CreateByteBufferStreamAdaptor(jbyteBuffer,
    //     initialPosition, limit);
    // if (!isTruthy(stream)) {
    //   throw_exception(ShadowImageDecoder.Error.kSourceMalformedData, "Failed to read ByteBuffer",
    //       null, source);
    // }
    // return native_create(stream, source);
    return jniCreateDecoder(new ImgStream() {
      @Override
      protected InputStream getInputStream() {
        return new ByteArrayInputStream(jbyteBuffer.array());
      }
    });
  }

  static ImageDecoder ImageDecoder_nCreateByteArray(byte[] byteArray,
      int offset, int length, Source source) {
    // SkStream stream = CreateByteArrayStreamAdaptor(byteArray, offset, length);
    // return native_create(stream, source);
    return jniCreateDecoder(new ImgStream() {
      @Override
      protected InputStream getInputStream() {
        return new ByteArrayInputStream(byteArray);
      }
    });
  }

  protected static Bitmap ImageDecoder_nDecodeBitmap(long nativePtr,
      ImageDecoder decoder,
      boolean doPostProcess,
      int width, int height,
      Rect cropRect, boolean mutable,
      int allocator, boolean unpremulRequired,
      boolean conserveMemory, boolean decodeAsAlphaMask,
      ColorSpace desiredColorSpace)
      throws IOException {
    CppImageDecoder cppImageDecoder = NATIVE_IMAGE_DECODER_REGISTRY.getNativeObject(nativePtr);

    final ImgStream imgStream = cppImageDecoder.imgStream;
    final InputStream stream = imgStream.getInputStream();

    if (stream == null) {
      return null;
    }

    Bitmap bitmap = BitmapFactory.decodeStream(stream);

    // TODO: Make this more efficient by transliterating nDecodeBitmap
    // Ensure that nDecodeBitmap should return a scaled bitmap as specified by height/width
    if (bitmap.getWidth() != width || bitmap.getHeight() != height) {
      bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    if (imgStream.isNinePatch() && ReflectionHelpers.getField(bitmap, "mNinePatchChunk") == null) {
      ReflectionHelpers.setField(Bitmap.class, bitmap, "mNinePatchChunk", new byte[0]);
    }
    return bitmap;
  }

  static Size ImageDecoder_nGetSampledSize(long nativePtr,
      int sampleSize) {
    CppImageDecoder decoder = NATIVE_IMAGE_DECODER_REGISTRY.getNativeObject(nativePtr);
    // SkISize size = decoder.mCodec.getSampledDimensions(sampleSize);
    // return env.NewObject(gSize_class, gSize_constructorMethodID, size.width(), size.height());
    // return new Size(size.width(), size.height());
    throw new UnsupportedOperationException();
  }

  static void ImageDecoder_nGetPadding(long nativePtr,
      Rect outPadding) {
    CppImageDecoder decoder = NATIVE_IMAGE_DECODER_REGISTRY.getNativeObject(nativePtr);
    // decoder.mPeeker.getPadding(outPadding);
    if (decoder.imgStream.isNinePatch()) {
      outPadding.set(0, 0, 0, 0);
    } else {
      outPadding.set(-1, -1, -1, -1);
    }
  }

  static void ImageDecoder_nClose(long nativePtr) {
    // delete reinterpret_cast<ImageDecoder*>(nativePtr);
    NATIVE_IMAGE_DECODER_REGISTRY.unregister(nativePtr);
  }

  static String ImageDecoder_nGetMimeType(long nativePtr) {
    CppImageDecoder decoder = NATIVE_IMAGE_DECODER_REGISTRY.getNativeObject(nativePtr);
    // return encodedFormatToString(decoder.mCodec.getEncodedFormat());
    throw new UnsupportedOperationException();
  }

  static ColorSpace ImageDecoder_nGetColorSpace(long nativePtr) {
    CppImageDecoder decoder = NATIVE_IMAGE_DECODER_REGISTRY.getNativeObject(nativePtr);
    // auto colorType = codec.computeOutputColorType(codec.getInfo().colorType());
    // sk_sp<SkColorSpace> colorSpace = codec.computeOutputColorSpace(colorType);
    // return GraphicsJNI.getColorSpace(colorSpace, colorType);
    throw new UnsupportedOperationException();
  }


  // native method implementations...

  @Implementation
  protected static ImageDecoder nCreate(long asset, Source source) throws IOException {
    return ImageDecoder_nCreateAsset(asset, source);
  }

  @Implementation
  protected static ImageDecoder nCreate(ByteBuffer buffer, int position,
      int limit, Source src) throws IOException {
    return ImageDecoder_nCreateByteBuffer(buffer, position, limit, src);
  }

  @Implementation
  protected static ImageDecoder nCreate(byte[] data, int offset, int length,
      Source src) throws IOException {
    return ImageDecoder_nCreateByteArray(data, offset, length, src);
  }

  @Implementation
  protected static ImageDecoder nCreate(InputStream is, byte[] storage, Source source) {
    return ImageDecoder_nCreateInputStream(is, storage, source);
  }

  // The fd must be seekable.
  @Implementation
  protected static ImageDecoder nCreate(FileDescriptor fd, Source src) throws IOException {
    return ImageDecoder_nCreateFd(fd, src);
  }

  @Implementation(maxSdk = Build.VERSION_CODES.P)
  protected static Bitmap nDecodeBitmap(long nativePtr,
      ImageDecoder decoder,
      boolean doPostProcess,
      int width, int height,
      android.graphics.Rect cropRect, boolean mutable,
      int allocator, boolean unpremulRequired,
      boolean conserveMemory, boolean decodeAsAlphaMask,
      android.graphics.ColorSpace desiredColorSpace)
      throws IOException {
    return ImageDecoder_nDecodeBitmap(nativePtr,
        decoder,
        doPostProcess,
        width, height,
        cropRect, mutable,
        allocator, unpremulRequired,
        conserveMemory, decodeAsAlphaMask,
        desiredColorSpace);
  }

  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected static Bitmap nDecodeBitmap(long nativePtr,
      ImageDecoder decoder,
      boolean doPostProcess,
      int width, int height,
      Rect cropRect, boolean mutable,
      int allocator, boolean unpremulRequired,
      boolean conserveMemory, boolean decodeAsAlphaMask,
      long desiredColorSpace, boolean extended)
      throws IOException {
    return ImageDecoder_nDecodeBitmap(nativePtr,
        decoder,
        doPostProcess,
        width, height,
        cropRect, mutable,
        allocator, unpremulRequired,
        conserveMemory, decodeAsAlphaMask,
        null);
  }

  @Implementation
  protected static Size nGetSampledSize(long nativePtr,
      int sampleSize) {
    return ImageDecoder_nGetSampledSize(nativePtr, sampleSize);
  }

  @Implementation
  protected static void nGetPadding(long nativePtr, Rect outRect) {
    ImageDecoder_nGetPadding(nativePtr, outRect);
  }

  @Implementation
  protected static void nClose(long nativePtr) {
    ImageDecoder_nClose(nativePtr);
  }

  @Implementation
  protected static String nGetMimeType(long nativePtr) {
    return ImageDecoder_nGetMimeType(nativePtr);
  }

  @Implementation
  protected static ColorSpace nGetColorSpace(long nativePtr) {
    return ImageDecoder_nGetColorSpace(nativePtr);
  }
}
