package org.robolectric.shadows;

import android.app.LoadedApk;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION_CODES;

@Implements(value = LoadedApk.class, isInAndroidSdk = false)
public class ShadowLoadedApk {

  @Implementation
  public ClassLoader getClassLoader() {
    return this.getClass().getClassLoader();
  }

  @Implementation(minSdk = VERSION_CODES.CUR_DEVELOPMENT)
  public ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException {
    return this.getClass().getClassLoader();
  }
}
