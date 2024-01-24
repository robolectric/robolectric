package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.content.res.AssetManager;
import android.graphics.FontFamily;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.FontFamilyNatives;
import org.robolectric.shadows.ShadowNativeFontFamily.Picker;
import org.robolectric.versioning.AndroidVersions.U;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for {@link FontFamily} that is backed by native code */
@Implements(
    value = FontFamily.class,
    minSdk = O,
    isInAndroidSdk = false,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeFontFamily {

  /**
   * {@link android.graphics.FontFamily} invokes its own native methods in its static initializer.
   * This must be deferred starting in Android V.
   */
  @Implementation(minSdk = V.SDK_INT)
  protected static void __staticInitializer__() {
    // deferred
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  public static long nInitBuilder(String langs, int variant) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return FontFamilyNatives.nInitBuilder(langs, variant);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nAllowUnsupportedFont(long builderPtr) {
    FontFamilyNatives.nAllowUnsupportedFont(builderPtr);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nCreateFamily(long mBuilderPtr) {
    return FontFamilyNatives.nCreateFamily(mBuilderPtr);
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static long nGetBuilderReleaseFunc() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return FontFamilyNatives.nGetBuilderReleaseFunc();
  }

  @Implementation(minSdk = P, maxSdk = U.SDK_INT)
  protected static long nGetFamilyReleaseFunc() {
    return FontFamilyNatives.nGetFamilyReleaseFunc();
  }

  // By passing -1 to weight argument, the weight value is resolved by OS/2 table in the font.
  // By passing -1 to italic argument, the italic value is resolved by OS/2 table in the font.
  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nAddFont(
      long builderPtr, ByteBuffer font, int ttcIndex, int weight, int isItalic) {
    return FontFamilyNatives.nAddFont(builderPtr, font, ttcIndex, weight, isItalic);
  }

  @Implementation(minSdk = O, maxSdk = Q)
  protected static boolean nAddFontFromAssetManager(
      long builderPtr,
      AssetManager mgr,
      String path,
      int cookie,
      boolean isAsset,
      int ttcIndex,
      int weight,
      int isItalic) {
    try {
      ByteBuffer byteBuffer = ShadowNativeFont.assetToBuffer(mgr, path, isAsset, cookie);
      return nAddFont(builderPtr, byteBuffer, ttcIndex, weight, isItalic);
    } catch (IOException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nAddFontWeightStyle(
      long builderPtr, ByteBuffer font, int ttcIndex, int weight, int isItalic) {
    return FontFamilyNatives.nAddFontWeightStyle(builderPtr, font, ttcIndex, weight, isItalic);
  }

  // The added axis values are only valid for the next nAddFont* method call.
  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nAddAxisValue(long builderPtr, int tag, float value) {
    FontFamilyNatives.nAddAxisValue(builderPtr, tag, value);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nAbort(long mBuilderPtr) {
    // no-op
  }

  /** Shadow picker for {@link FontFamily}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowFontFamily.class, ShadowNativeFontFamily.class);
    }
  }
}
