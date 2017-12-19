package org.robolectric.shadows;

import static android.content.pm.PackageManager.NameNotFoundException;

import android.app.LoadedApk;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = LoadedApk.class, isInAndroidSdk = false)
public class ShadowLoadedApk {

  @Implementation
  protected ClassLoader getClassLoader() {
    return this.getClass().getClassLoader();
  }

  @Implementation(minSdk = VERSION_CODES.O)
  protected ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException {
    return this.getClass().getClassLoader();
  }
}
