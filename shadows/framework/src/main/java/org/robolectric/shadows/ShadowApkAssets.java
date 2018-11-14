package org.robolectric.shadows;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/jni/android_content_res_ApkAssets.cpp

abstract public class ShadowApkAssets {

  public static class Picker extends ResourceModeShadowPicker<ShadowApkAssets> {

    public Picker() {
      super(ShadowLegacyApkAssets.class, null, ShadowArscApkAssets9.class);
    }
  }

}
