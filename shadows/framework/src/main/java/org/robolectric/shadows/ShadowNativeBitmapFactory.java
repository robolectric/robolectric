package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import java.io.FileDescriptor;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.BitmapFactoryNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowNativeBitmapFactory.Picker;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link BitmapFactory} that is backed by native code */
@Implements(
    value = BitmapFactory.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeBitmapFactory {

  static {
    DefaultNativeRuntimeLoader.injectAndLoad();
  }

  @Implementation
  protected static Bitmap decodeResource(Resources res, int id, BitmapFactory.Options options) {
    Bitmap bitmap = reflector(BitmapFactoryReflector.class).decodeResource(res, id, options);
    if (bitmap == null) {
      return null;
    }

    ShadowNativeBitmap shadowNativeBitmap = Shadow.extract(bitmap);
    shadowNativeBitmap.setCreatedFromResId(id);
    return bitmap;
  }

  @Implementation
  protected static Bitmap decodeStream(InputStream is, Rect outPadding, Options opts) {
    reflector(BitmapFactoryOptionsReflector.class).validate(opts);
    Bitmap bitmap =
        reflector(BitmapFactoryReflector.class).decodeStreamInternal(is, outPadding, opts);
    reflector(BitmapFactoryReflector.class).setDensityFromOptions(bitmap, opts);
    return bitmap;
  }

  @Implementation(minSdk = Q)
  protected static Bitmap nativeDecodeStream(
      InputStream is,
      byte[] storage,
      Rect padding,
      Options opts,
      long inBitmapHandle,
      long colorSpaceHandle) {
    return BitmapFactoryNatives.nativeDecodeStream(
        is, storage, padding, opts, inBitmapHandle, colorSpaceHandle);
  }

  @Implementation(maxSdk = P)
  protected static Bitmap nativeDecodeStream(
      InputStream is, byte[] storage, Rect padding, Options opts) {
    return nativeDecodeStream(is, storage, padding, opts, nativeInBitmap(opts), 0);
  }

  @Implementation(minSdk = Q)
  protected static Bitmap nativeDecodeFileDescriptor(
      FileDescriptor fd, Rect padding, Options opts, long inBitmapHandle, long colorSpaceHandle) {
    return BitmapFactoryNatives.nativeDecodeFileDescriptor(
        fd, padding, opts, inBitmapHandle, colorSpaceHandle);
  }

  @Implementation(maxSdk = P)
  protected static Bitmap nativeDecodeFileDescriptor(
      FileDescriptor fd, Rect padding, Options opts) {
    return nativeDecodeFileDescriptor(fd, padding, opts, nativeInBitmap(opts), 0);
  }

  @Implementation(minSdk = Q)
  protected static Bitmap nativeDecodeAsset(
      long nativeAsset, Rect padding, Options opts, long inBitmapHandle, long colorSpaceHandle) {
    return BitmapFactoryNatives.nativeDecodeAsset(
        nativeAsset, padding, opts, inBitmapHandle, colorSpaceHandle);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = P)
  protected static Bitmap nativeDecodeAsset(long nativeAsset, Rect padding, Options opts) {
    return nativeDecodeAsset(nativeAsset, padding, opts, nativeInBitmap(opts), 0);
  }

  @Implementation(minSdk = Q)
  protected static Bitmap nativeDecodeByteArray(
      byte[] data,
      int offset,
      int length,
      Options opts,
      long inBitmapHandle,
      long colorSpaceHandle) {
    return BitmapFactoryNatives.nativeDecodeByteArray(
        data, offset, length, opts, inBitmapHandle, colorSpaceHandle);
  }

  @Implementation(maxSdk = P)
  protected static Bitmap nativeDecodeByteArray(byte[] data, int offset, int length, Options opts) {
    return nativeDecodeByteArray(data, offset, length, opts, nativeInBitmap(opts), 0);
  }

  @Implementation
  protected static boolean nativeIsSeekable(FileDescriptor fd) {
    return BitmapFactoryNatives.nativeIsSeekable(fd);
  }

  /** Helper for passing inBitmap's native pointer to native. */
  static long nativeInBitmap(Options opts) {
    if (opts == null || opts.inBitmap == null) {
      return 0;
    }

    return opts.inBitmap.getNativeInstance();
  }

  @ForType(BitmapFactory.class)
  interface BitmapFactoryReflector {
    Bitmap decodeStreamInternal(InputStream is, Rect outPadding, Options opts);

    void setDensityFromOptions(Bitmap outputBitmap, Options opts);

    @Direct
    Bitmap decodeResource(Resources res, int id, BitmapFactory.Options options);
  }

  @ForType(BitmapFactory.Options.class)
  interface BitmapFactoryOptionsReflector {
    void validate(Options opts);
  }

  /** Shadow picker for {@link BitmapFactory}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowBitmapFactory.class, ShadowNativeBitmapFactory.class);
    }
  }
}
