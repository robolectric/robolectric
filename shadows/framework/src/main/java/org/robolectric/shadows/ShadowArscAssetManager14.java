package org.robolectric.shadows;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.U;
import org.robolectric.versioning.AndroidVersions.V;

// TODO: update path to released version.
// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-10.0.0_rXX/core/jni/android_util_AssetManager.cpp

@Implements(
    value = AssetManager.class,
    minSdk = U.SDK_INT,
    shadowPicker = ShadowAssetManager.Picker.class)
@SuppressWarnings("NewApi")
public class ShadowArscAssetManager14 extends ShadowArscAssetManager10 {

  // static void NativeSetConfiguration(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint mcc, jint
  // mnc,
  //                                    jstring locale, jint orientation, jint touchscreen, jint
  // density,
  //                                    jint keyboard, jint keyboard_hidden, jint navigation,
  //                                    jint screen_width, jint screen_height,
  //                                    jint smallest_screen_width_dp, jint screen_width_dp,
  //                                    jint screen_height_dp, jint screen_layout, jint ui_mode,
  //                                    jint color_mode, jint major_version) {
  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static void nativeSetConfiguration(
      long ptr,
      int mcc,
      int mnc,
      @Nullable String locale,
      int orientation,
      int touchscreen,
      int density,
      int keyboard,
      int keyboard_hidden,
      int navigation,
      int screen_width,
      int screen_height,
      int smallest_screen_width_dp,
      int screen_width_dp,
      int screen_height_dp,
      int screen_layout,
      int ui_mode,
      int color_mode,
      int grammaticalGender, // ignore for now?
      int major_version) {
    ShadowArscAssetManager10.nativeSetConfiguration(
        ptr,
        mcc,
        mnc,
        locale,
        orientation,
        touchscreen,
        density,
        keyboard,
        keyboard_hidden,
        navigation,
        screen_width,
        screen_height,
        smallest_screen_width_dp,
        screen_width_dp,
        screen_height_dp,
        screen_layout,
        ui_mode,
        color_mode,
        major_version);
  }

  @Implementation(minSdk = V.SDK_INT)
  protected static void nativeSetConfiguration(
      long ptr,
      int mcc,
      int mnc,
      /* Used only when locales is null or empty. */
      @Nullable String defaultLocale,
      /* At this moment, only the first element in locales is used and others are ignored. */
      @NonNull String[] locales,
      int orientation,
      int touchscreen,
      int density,
      int keyboard,
      int keyboardHidden,
      int navigation,
      int screenWidth,
      int screenHeight,
      int smallestScreenWidthDp,
      int screenWidthDp,
      int screenHeightDp,
      int screenLayout,
      int uiMode,
      int colorMode,
      int grammaticalGender,
      int majorVersion,
      boolean forceRefresh) {
    String localeToUse;
    if (locales != null && locales.length != 0) {
      localeToUse = locales[0];
    } else {
      localeToUse = defaultLocale;
    }
    nativeSetConfiguration(
        ptr,
        mcc,
        mnc,
        localeToUse,
        orientation,
        touchscreen,
        density,
        keyboard,
        keyboardHidden,
        navigation,
        screenWidth,
        screenHeight,
        smallestScreenWidthDp,
        screenWidthDp,
        screenHeightDp,
        screenLayout,
        uiMode,
        colorMode,
        grammaticalGender,
        majorVersion);
  }

  @Implementation(minSdk = V.SDK_INT)
  protected static void nativeSetApkAssets(
      long ptr, @NonNull ApkAssets[] apkAssets, boolean invalidateCaches, boolean preset) {
    nativeSetApkAssets(ptr, apkAssets, invalidateCaches);
  }
}
// namespace android
