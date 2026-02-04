package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.BitmapRegionDecoderNatives;
import org.robolectric.shadows.ShadowNativeBitmapRegionDecoder.Picker;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link BitmapRegionDecoder} that is backed by native code */
@Implements(
    value = BitmapRegionDecoder.class,
    isInAndroidSdk = false,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeBitmapRegionDecoder {

  /**
   * The real implementation checks to see if the InputStream is an AssetInputStream. However,
   * Robolectric does not support native assets for all SDK levels.
   */
  @Implementation(minSdk = S)
  protected static BitmapRegionDecoder newInstance(InputStream is) throws IOException {
    byte[] tempStorage = new byte[16 * 1024];
    return reflector(BitmapRegionDecoderReflector.class).nativeNewInstance(is, tempStorage);
  }

  /**
   * The real implementation checks to see if the InputStream is an AssetInputStream. However,
   * Robolectric does not support native assets for all SDK levels.
   */
  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static BitmapRegionDecoder newInstance(InputStream is, boolean isShareable)
      throws IOException {
    byte[] tempStorage = new byte[16 * 1024];
    return nativeNewInstance(is, tempStorage, isShareable);
  }

  @Implementation(minSdk = Q, maxSdk = UPSIDE_DOWN_CAKE)
  protected static Bitmap nativeDecodeRegion(
      long lbm,
      int startX,
      int startY,
      int width,
      int height,
      BitmapFactory.Options options,
      long inBitmapHandle,
      long colorSpaceHandle) {
    return BitmapRegionDecoderNatives.nativeDecodeRegion(
        lbm, startX, startY, width, height, options, inBitmapHandle, colorSpaceHandle);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static Bitmap nativeDecodeRegion(
      long lbm, int startX, int startY, int width, int height, BitmapFactory.Options options) {
    return BitmapRegionDecoderNatives.nativeDecodeRegion(
        lbm, startX, startY, width, height, options, 0, 0);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeGetWidth(long lbm) {
    return BitmapRegionDecoderNatives.nativeGetWidth(lbm);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nativeGetHeight(long lbm) {
    return BitmapRegionDecoderNatives.nativeGetHeight(lbm);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nativeClean(long lbm) {
    BitmapRegionDecoderNatives.nativeClean(lbm);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static BitmapRegionDecoder nativeNewInstance(byte[] data, int offset, int length) {
    return BitmapRegionDecoderNatives.nativeNewInstance(data, offset, length);
  }

  @Implementation(minSdk = O, maxSdk = R)
  protected static BitmapRegionDecoder nativeNewInstance(
      byte[] data, int offset, int length, boolean isShareable) {
    return BitmapRegionDecoderNatives.nativeNewInstance(data, offset, length);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static BitmapRegionDecoder nativeNewInstance(FileDescriptor fd) {
    return BitmapRegionDecoderNatives.nativeNewInstance(fd);
  }

  @Implementation(minSdk = O, maxSdk = R)
  protected static BitmapRegionDecoder nativeNewInstance(FileDescriptor fd, boolean isShareable) {
    return BitmapRegionDecoderNatives.nativeNewInstance(fd);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static BitmapRegionDecoder nativeNewInstance(InputStream is, byte[] storage) {
    return BitmapRegionDecoderNatives.nativeNewInstance(is, storage);
  }

  @Implementation(minSdk = O, maxSdk = R)
  protected static BitmapRegionDecoder nativeNewInstance(
      InputStream is, byte[] storage, boolean isShareable) {
    return BitmapRegionDecoderNatives.nativeNewInstance(is, storage);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static BitmapRegionDecoder nativeNewInstance(long asset) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Implementation(minSdk = O, maxSdk = R)
  protected static BitmapRegionDecoder nativeNewInstance(long asset, boolean isShareable) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @ForType(BitmapRegionDecoder.class)
  interface BitmapRegionDecoderReflector {
    @Static
    BitmapRegionDecoder nativeNewInstance(InputStream is, byte[] storage);

    @Static
    BitmapRegionDecoder nativeNewInstance(InputStream is, byte[] storage, boolean isShareable);
  }

  /** Shadow picker for {@link BitmapRegionDecoder}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowBitmapRegionDecoder.class, ShadowNativeBitmapRegionDecoder.class);
    }
  }
}
