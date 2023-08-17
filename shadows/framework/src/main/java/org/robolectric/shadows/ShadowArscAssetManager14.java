package org.robolectric.shadows;


import android.annotation.Nullable;
import android.content.res.AssetManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.U;

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
  @Implementation(minSdk = U.SDK_INT)
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
}
// namespace android
