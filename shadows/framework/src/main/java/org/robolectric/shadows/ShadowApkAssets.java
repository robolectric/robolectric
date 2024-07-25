package org.robolectric.shadows;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/jni/android_content_res_ApkAssets.cpp

public abstract class ShadowApkAssets {

  public static class Picker extends ResourceModeShadowPicker<ShadowApkAssets> {

    public Picker() {
      super(
          null,
          ShadowArscApkAssets9.class,
          ShadowArscApkAssets9.class,
          ShadowArscApkAssets9.class,
          ShadowNativeApkAssets.class);
    }
  }
}
