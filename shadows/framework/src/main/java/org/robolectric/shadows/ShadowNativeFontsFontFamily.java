package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.fonts.FontFamily;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.FontFamilyBuilderNatives;
import org.robolectric.nativeruntime.FontsFontFamilyNatives;
import org.robolectric.shadows.ShadowNativeFontsFontFamily.Picker;

/** Shadow for {@link FontFamily} that is backed by native code */
@Implements(
    value = FontFamily.class,
    minSdk = Q,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeFontsFontFamily {
  @Implementation(minSdk = S)
  protected static int nGetFontSize(long family) {
    return FontsFontFamilyNatives.nGetFontSize(family);
  }

  @Implementation(minSdk = S)
  protected static long nGetFont(long family, int i) {
    return FontsFontFamilyNatives.nGetFont(family, i);
  }

  @Implementation(minSdk = S)
  protected static String nGetLangTags(long family) {
    return FontsFontFamilyNatives.nGetLangTags(family);
  }

  @Implementation(minSdk = S)
  protected static int nGetVariant(long family) {
    return FontsFontFamilyNatives.nGetVariant(family);
  }

  /** Shadow for {@link FontFamily.Builder} that is backed by native code */
  @Implements(
      value = FontFamily.Builder.class,
      minSdk = Q,
      shadowPicker = ShadowNativeFontFamilyBuilder.Picker.class,
      isInAndroidSdk = false)
  public static class ShadowNativeFontFamilyBuilder {
    @Implementation
    protected static long nInitBuilder() {
      DefaultNativeRuntimeLoader.injectAndLoad();
      return FontFamilyBuilderNatives.nInitBuilder();
    }

    @Implementation
    protected static void nAddFont(long builderPtr, long fontPtr) {
      FontFamilyBuilderNatives.nAddFont(builderPtr, fontPtr);
    }

    @Implementation
    protected static long nBuild(
        long builderPtr, String langTags, int variant, boolean isCustomFallback) {
      return FontFamilyBuilderNatives.nBuild(builderPtr, langTags, variant, isCustomFallback);
    }

    @Implementation
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
