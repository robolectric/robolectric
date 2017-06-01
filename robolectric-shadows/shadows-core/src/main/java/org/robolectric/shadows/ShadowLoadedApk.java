package org.robolectric.shadows;

import android.app.LoadedApk;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import android.content.pm.PackageManager.NameNotFoundException;

@Implements(value = LoadedApk.class, isInAndroidSdk = false)
public class ShadowLoadedApk {

  @Implementation
  public ClassLoader getClassLoader() {
    return this.getClass().getClassLoader();
  }

  // TODO: change to VERSION_CODES.O when final
  @Implementation(minSdk = 26)
  public ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException {
    return this.getClass().getClassLoader();
  }
}
