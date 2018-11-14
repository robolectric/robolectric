package org.robolectric.shadows;

import android.os.Build;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/jni/android_content_res_ApkAssets.cpp

@Implements(value = android.content.res.ApkAssets.class, minSdk = Build.VERSION_CODES.P, isInAndroidSdk = false)
public class ShadowLegacyApkAssets extends ShadowApkAssets {

  private String assetPath;

  @Implementation
  protected void __constructor__(String path, boolean system, boolean forceSharedLib,
      boolean overlay) throws IOException {
    Preconditions.checkNotNull(path, "path");
    this.assetPath = path;
  }


  @Implementation
  protected String getAssetPath() {
    return assetPath;
  }
}
