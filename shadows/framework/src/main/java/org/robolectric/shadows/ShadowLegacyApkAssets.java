package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.content.res.ApkAssets;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/jni/android_content_res_ApkAssets.cpp

/** Shadow for {@link ApkAssets} that is used for legacy resources. */
@Implements(value = ApkAssets.class, minSdk = P, isInAndroidSdk = false)
public class ShadowLegacyApkAssets extends ShadowApkAssets {

  private String assetPath;

  @Implementation(maxSdk = Q)
  protected void __constructor__(
      String path, boolean system, boolean forceSharedLib, boolean overlay) throws IOException {
    Preconditions.checkNotNull(path, "path");
    this.assetPath = path;
  }


  @Implementation
  protected String getAssetPath() {
    return assetPath;
  }
}
