package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.graphics.fonts.FontFamily;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.FontFamilyBuilderNatives;
import org.robolectric.nativeruntime.FontsFontFamilyNatives;
import org.robolectric.shadows.ShadowNativeFontsFontFamily.Picker;
import org.robolectric.versioning.AndroidVersions.U;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for {@link FontFamily} that is backed by native code */
@Implements(
    value = FontFamily.class,
    minSdk = Q,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeFontsFontFamily {

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static int nGetFontSize(long family) {
    return FontsFontFamilyNatives.nGetFontSize(family);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static long nGetFont(long family, int i) {
    return FontsFontFamilyNatives.nGetFont(family, i);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static String nGetLangTags(long family) {
    return FontsFontFamilyNatives.nGetLangTags(family);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static int nGetVariant(long family) {
    return FontsFontFamilyNatives.nGetVariant(family);
  }

  /** Shadow for {@link FontFamily.Builder} that is backed by native code */
  @Implements(
      value = FontFamily.Builder.class,
      minSdk = Q,
      shadowPicker = ShadowNativeFontFamilyBuilder.Picker.class,
      isInAndroidSdk = false,
      callNativeMethodsByDefault = true)
  public static class ShadowNativeFontFamilyBuilder {

    @Implementation(minSdk = V.SDK_INT)
    protected static void __staticInitializer__() {}

    @Implementation(maxSdk = U.SDK_INT)
    protected static long nInitBuilder() {
      DefaultNativeRuntimeLoader.injectAndLoad();
      return FontFamilyBuilderNatives.nInitBuilder();
    }

    @Implementation(maxSdk = U.SDK_INT)
    protected static void nAddFont(long builderPtr, long fontPtr) {
      FontFamilyBuilderNatives.nAddFont(builderPtr, fontPtr);
    }

    @Implementation(maxSdk = TIRAMISU)
    protected static long nBuild(
        long builderPtr, String langTags, int variant, boolean isCustomFallback) {
      return FontFamilyBuilderNatives.nBuild(builderPtr, langTags, variant, isCustomFallback);
    }

    @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
    protected static long nBuild(
        long builderPtr,
        String langTags,
        int variant,
        boolean isCustomFallback,
        boolean isDefaultFallback) {
      return FontFamilyBuilderNatives.nBuild(builderPtr, langTags, variant, isCustomFallback);
    }

    @Implementation(maxSdk = U.SDK_INT)
    protected static long nGetReleaseNativeFamily() {
      return FontFamilyBuilderNatives.nGetReleaseNativeFamily();
    }

    /** Shadow picker for {@link FontFamily.Builder}. */
    public static final class Picker extends GraphicsShadowPicker<Object> {
      public Picker() {
        super(
            ShadowFontsFontFamily.ShadowFontsFontFamilyBuilder.class,
            ShadowNativeFontFamilyBuilder.class);
      }
    }
  }

  /** Shadow picker for {@link FontFamily}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowFontsFontFamily.class, ShadowNativeFontsFontFamily.class);
    }
  }
}
