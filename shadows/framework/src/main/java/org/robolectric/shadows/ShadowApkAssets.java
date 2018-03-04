// BEGIN-INTERNAL
package main.java.org.robolectric.shadows;

import android.content.res.ApkAssets;
import android.os.Build;

import com.android.internal.util.Preconditions;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.FileDescriptor;
import java.io.IOException;

@Implements(value = ApkAssets.class, minSdk = Build.VERSION_CODES.P, isInAndroidSdk = false)
public class ShadowApkAssets {
  private String assetPath;

  @Implementation
  protected void __constructor__(String path, boolean system, boolean forceSharedLib,
      boolean overlay) throws IOException {
    Preconditions.checkNotNull(path, "path");
    assetPath = path;
  }

  @Implementation
  protected void __constructor__(FileDescriptor fd, String name, boolean system,
      boolean forceSharedLib) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Implementation
  protected String getAssetPath() {
    return assetPath;
  }

  @Implementation
  protected CharSequence getStringFromPool(int idx) {
    throw new UnsupportedOperationException();
  }
}
// END-INTERNAL