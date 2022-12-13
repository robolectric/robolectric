package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.ColorSpace.Rgb.TransferParameters;
import android.graphics.Matrix;
import android.hardware.HardwareBuffer;
import android.os.Parcel;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.nativeruntime.BitmapNatives;
import org.robolectric.nativeruntime.ColorSpaceRgbNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.NativeAllocationRegistryNatives;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link Bitmap} that is backed by native code */
@Implements(value = Bitmap.class, looseSignatures = true, minSdk = O, isInAndroidSdk = false)
public class ShadowNativeBitmap extends ShadowBitmap {

  @RealObject Bitmap realBitmap;

  private int createdFromResId;

  private static final List<Long> colorSpaceAllocationsP =
      Collections.synchronizedList(new ArrayList<>());

  /** Called by {@link ShadowNativeBitmapFactory}. */
  void setCreatedFromResId(int createdFromResId) {
    this.createdFromResId = createdFromResId;
  }

  @Implementation(minSdk = Q)
  protected static Bitmap nativeCreate(
      int[] colors,
      int offset,
      int stride,
      int width,
      int height,
      int nativeConfig,
      boolean mutable,
      long nativeColorSpace) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return BitmapNatives.nativeCreate(
        colors, offset, stride, width, height, nativeConfig, mutable, nativeColorSpace);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static Bitmap nativeCreate(
      int[] colors,
      int offset,
      int stride,
      int width,
      int height,
      int nativeConfig,
      boolean mutable,
      float[] xyzD50,
      ColorSpace.Rgb.TransferParameters p) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    long colorSpacePtr = 0;
    if (xyzD50 != null && p != null) {
      colorSpacePtr =
          ColorSpaceRgbNatives.nativeCreate(
              (float) p.a,
              (float) p.b,
              (float) p.c,
              (float) p.d,
              (float) p.e,
              (float) p.f,
              (float) p.g,
              xyzD50);
      colorSpaceAllocationsP.add(colorSpacePtr);
    }
    return nativeCreate(
        colors, offset, stride, width, height, nativeConfig, mutable, colorSpacePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static Bitmap nativeCopy(long nativeSrcBitmap, int nativeConfig, boolean isMutable) {
    return BitmapNatives.nativeCopy(nativeSrcBitmap, nativeConfig, isMutable);
  }

  @Implementation(minSdk = M)
  protected static Bitmap nativeCopyAshmem(long nativeSrcBitmap) {
    return BitmapNatives.nativeCopyAshmem(nativeSrcBitmap);
  }

  @Implementation(minSdk = N)
  protected static Bitmap nativeCopyAshmemConfig(long nativeSrcBitmap, int nativeConfig) {
    return BitmapNatives.nativeCopyAshmemConfig(nativeSrcBitmap, nativeConfig);
  }

  @Implementation(minSdk = N)
  protected static long nativeGetNativeFinalizer() {
    return BitmapNatives.nativeGetNativeFinalizer();
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static Object nativeRecycle(Object nativeBitmap) {
    BitmapNatives.nativeRecycle((long) nativeBitmap);
    return true;
  }

  @Implementation(minSdk = O)
  protected static void nativeReconfigure(
      long nativeBitmap, int width, int height, int config, boolean isPremultiplied) {
    BitmapNatives.nativeReconfigure(nativeBitmap, width, height, config, isPremultiplied);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativeCompress(
      long nativeBitmap, int format, int quality, OutputStream stream, byte[] tempStorage) {
    return BitmapNatives.nativeCompress(nativeBitmap, format, quality, stream, tempStorage);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeErase(long nativeBitmap, int color) {
    BitmapNatives.nativeErase(nativeBitmap, color);
  }

  @Implementation(minSdk = Q)
  protected static void nativeErase(long nativeBitmap, long colorSpacePtr, long color) {
    BitmapNatives.nativeErase(nativeBitmap, colorSpacePtr, color);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeRowBytes(long nativeBitmap) {
    return BitmapNatives.nativeRowBytes(nativeBitmap);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeConfig(long nativeBitmap) {
    return BitmapNatives.nativeConfig(nativeBitmap);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeGetPixel(long nativeBitmap, int x, int y) {
    return BitmapNatives.nativeGetPixel(nativeBitmap, x, y);
  }

  @Implementation(minSdk = Q)
  protected static long nativeGetColor(long nativeBitmap, int x, int y) {
    return BitmapNatives.nativeGetColor(nativeBitmap, x, y);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeGetPixels(
      long nativeBitmap,
      int[] pixels,
      int offset,
      int stride,
      int x,
      int y,
      int width,
      int height) {
    BitmapNatives.nativeGetPixels(nativeBitmap, pixels, offset, stride, x, y, width, height);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeSetPixel(long nativeBitmap, int x, int y, int color) {
    BitmapNatives.nativeSetPixel(nativeBitmap, x, y, color);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeSetPixels(
      long nativeBitmap,
      int[] colors,
      int offset,
      int stride,
      int x,
      int y,
      int width,
      int height) {
    BitmapNatives.nativeSetPixels(nativeBitmap, colors, offset, stride, x, y, width, height);
  }

  @Implementation
  protected static void nativeCopyPixelsToBuffer(long nativeBitmap, Buffer dst) {
    BitmapNatives.nativeCopyPixelsToBuffer(nativeBitmap, dst);
  }

  @Implementation
  protected static void nativeCopyPixelsFromBuffer(long nativeBitmap, Buffer src) {
    BitmapNatives.nativeCopyPixelsFromBuffer(nativeBitmap, src);
  }

  @Implementation
  protected static int nativeGenerationId(long nativeBitmap) {
    return BitmapNatives.nativeGenerationId(nativeBitmap);
  }

  // returns a new bitmap built from the native bitmap's alpha, and the paint
  @Implementation
  protected static Bitmap nativeExtractAlpha(long nativeBitmap, long nativePaint, int[] offsetXY) {
    return BitmapNatives.nativeExtractAlpha(nativeBitmap, nativePaint, offsetXY);
  }

  @Implementation
  protected static boolean nativeHasAlpha(long nativeBitmap) {
    return BitmapNatives.nativeHasAlpha(nativeBitmap);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativeIsPremultiplied(long nativeBitmap) {
    return BitmapNatives.nativeIsPremultiplied(nativeBitmap);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeSetPremultiplied(long nativeBitmap, boolean isPremul) {
    BitmapNatives.nativeSetPremultiplied(nativeBitmap, isPremul);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeSetHasAlpha(
      long nativeBitmap, boolean hasAlpha, boolean requestPremul) {
    BitmapNatives.nativeSetHasAlpha(nativeBitmap, hasAlpha, requestPremul);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected static boolean nativeHasMipMap(long nativeBitmap) {
    return BitmapNatives.nativeHasMipMap(nativeBitmap);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected static void nativeSetHasMipMap(long nativeBitmap, boolean hasMipMap) {
    BitmapNatives.nativeSetHasMipMap(nativeBitmap, hasMipMap);
  }

  @Implementation
  protected static boolean nativeSameAs(long nativeBitmap0, long nativeBitmap1) {
    return BitmapNatives.nativeSameAs(nativeBitmap0, nativeBitmap1);
  }

  @Implementation(minSdk = N_MR1)
  protected static void nativePrepareToDraw(long nativeBitmap) {
    BitmapNatives.nativePrepareToDraw(nativeBitmap);
  }

  @Implementation(minSdk = O)
  protected static int nativeGetAllocationByteCount(long nativeBitmap) {
    return BitmapNatives.nativeGetAllocationByteCount(nativeBitmap);
  }

  @Implementation(minSdk = O)
  protected static Bitmap nativeCopyPreserveInternalConfig(long nativeBitmap) {
    return BitmapNatives.nativeCopyPreserveInternalConfig(nativeBitmap);
  }

  @Implementation(minSdk = Q)
  protected static Bitmap nativeWrapHardwareBufferBitmap(
      HardwareBuffer buffer, long nativeColorSpace) {
    return BitmapNatives.nativeWrapHardwareBufferBitmap(buffer, nativeColorSpace);
  }

  @Implementation(minSdk = R)
  protected static HardwareBuffer nativeGetHardwareBuffer(long nativeBitmap) {
    return BitmapNatives.nativeGetHardwareBuffer(nativeBitmap);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static boolean nativeGetColorSpace(long nativePtr, float[] xyz, float[] params) {
    ColorSpace colorSpace = nativeComputeColorSpace(nativePtr);
    if (colorSpace == null) {
      return false;
    }
    // In Android P, 'nativeGetColorSpace' is responsible for filling out the 'xyz' and 'params'
    // float arrays. However, in Q and above, 'nativeGetColorSpace' was removed, and
    // 'nativeComputeColorSpace' returns the ColorSpace object itself. This means for P, we need to
    // do the reverse operations and generate the float arrays given the detected color space.
    if (colorSpace instanceof ColorSpace.Rgb) {
      TransferParameters transferParameters = ((ColorSpace.Rgb) colorSpace).getTransferParameters();
      params[0] = (float) transferParameters.a;
      params[1] = (float) transferParameters.b;
      params[2] = (float) transferParameters.c;
      params[3] = (float) transferParameters.d;
      params[4] = (float) transferParameters.e;
      params[5] = (float) transferParameters.f;
      params[6] = (float) transferParameters.g;
      ColorSpace.Rgb rgb =
          (ColorSpace.Rgb)
              ColorSpace.adapt(
                  colorSpace, reflector(ColorSpaceReflector.class).getIlluminantD50XYZ());
      rgb.getTransform(xyz);
    }
    return true;
  }

  @Implementation(minSdk = Q)
  protected static ColorSpace nativeComputeColorSpace(long nativePtr) {
    return BitmapNatives.nativeComputeColorSpace(nativePtr);
  }

  @Implementation(minSdk = Q)
  protected static void nativeSetColorSpace(long nativePtr, long nativeColorSpace) {
    BitmapNatives.nativeSetColorSpace(nativePtr, nativeColorSpace);
  }

  @Implementation(minSdk = O)
  protected static boolean nativeIsSRGB(long nativePtr) {
    return BitmapNatives.nativeIsSRGB(nativePtr);
  }

  @Implementation(minSdk = P)
  protected static boolean nativeIsSRGBLinear(long nativePtr) {
    return BitmapNatives.nativeIsSRGBLinear(nativePtr);
  }

  @Implementation(minSdk = Q)
  protected static void nativeSetImmutable(long nativePtr) {
    BitmapNatives.nativeSetImmutable(nativePtr);
  }

  @Implementation(minSdk = Q)
  protected static boolean nativeIsImmutable(long nativePtr) {
    return BitmapNatives.nativeIsImmutable(nativePtr);
  }

  @Implementation(minSdk = S)
  protected static boolean nativeIsBackedByAshmem(long nativePtr) {
    return BitmapNatives.nativeIsBackedByAshmem(nativePtr);
  }

  @ForType(ColorSpace.class)
  interface ColorSpaceReflector {
    @Accessor("ILLUMINANT_D50_XYZ")
    @Static
    float[] getIlluminantD50XYZ();

    @Accessor("sNamedColorSpaces")
    ColorSpace[] getNamedColorSpaces();
  }

  @Implementation
  protected void writeToParcel(Parcel p, int flags) {
    // Modeled after
    // https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/libs/hwui/jni/Bitmap.cpp;l=872.
    reflector(BitmapReflector.class, realBitmap).checkRecycled("Can't parcel a recycled bitmap");
    int width = realBitmap.getWidth();
    int height = realBitmap.getHeight();
    p.writeInt(width);
    p.writeInt(height);
    p.writeInt(realBitmap.getDensity());
    p.writeBoolean(realBitmap.isMutable());
    p.writeSerializable(realBitmap.getConfig());
    p.writeString(realBitmap.getColorSpace().getName());
    p.writeBoolean(realBitmap.hasAlpha());
    int[] pixels = new int[width * height];
    realBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
    p.writeIntArray(pixels);
  }

  @Implementation
  protected static Bitmap nativeCreateFromParcel(Parcel p) {
    int parceledWidth = p.readInt();
    int parceledHeight = p.readInt();
    int density = p.readInt();
    boolean mutable = p.readBoolean();
    Bitmap.Config parceledConfig = (Bitmap.Config) p.readSerializable();
    String colorSpaceName = p.readString();
    boolean hasAlpha = p.readBoolean();
    ColorSpace colorSpace = null;
    ColorSpace[] namedColorSpaces = reflector(ColorSpaceReflector.class).getNamedColorSpaces();
    for (ColorSpace named : namedColorSpaces) {
      if (named.getName().equals(colorSpaceName)) {
        colorSpace = named;
        break;
      }
    }
    int[] parceledColors = new int[parceledHeight * parceledWidth];
    p.readIntArray(parceledColors);
    Bitmap bitmap =
        Bitmap.createBitmap(parceledWidth, parceledHeight, parceledConfig, hasAlpha, colorSpace);
    bitmap.setPixels(parceledColors, 0, parceledWidth, 0, 0, parceledWidth, parceledHeight);
    bitmap.setDensity(density);
    if (!mutable) {
      bitmap = bitmap.copy(parceledConfig, false);
    }
    return bitmap;
  }

  @ForType(Bitmap.class)
  interface BitmapReflector {
    void checkRecycled(String errorMessage);
  }

  @Override
  public Bitmap getCreatedFromBitmap() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  /**
   * Resource ID from which this Bitmap was created.
   *
   * @return Resource ID from which this Bitmap was created, or {@code 0} if this Bitmap was not
   *     created from a resource.
   */
  @Override
  public int getCreatedFromResId() {
    return createdFromResId;
  }

  @Override
  public String getCreatedFromPath() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public InputStream getCreatedFromStream() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public byte[] getCreatedFromBytes() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public int getCreatedFromX() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public int getCreatedFromY() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public int getCreatedFromWidth() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public int getCreatedFromHeight() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public int[] getCreatedFromColors() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public Matrix getCreatedFromMatrix() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public boolean getCreatedFromFilter() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public void setMutable(boolean mutable) {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public void appendDescription(String s) {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public String getDescription() {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Override
  public void setDescription(String s) {
    throw new UnsupportedOperationException("Legacy ShadowBitmap APIs are not supported");
  }

  @Resetter
  public static void reset() {
    synchronized (colorSpaceAllocationsP) {
      for (Long ptr : colorSpaceAllocationsP) {
        NativeAllocationRegistryNatives.applyFreeFunction(
            ColorSpaceRgbNatives.nativeGetNativeFinalizer(), ptr);
      }
      colorSpaceAllocationsP.clear();
    }
  }
}
