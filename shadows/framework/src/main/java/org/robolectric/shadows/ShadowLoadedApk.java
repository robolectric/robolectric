package org.robolectric.shadows;

import android.app.Application;
import android.app.LoadedApk;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(value = LoadedApk.class, isInAndroidSdk = false)
public class ShadowLoadedApk {

  @Implementation
  public ClassLoader getClassLoader() {
    return this.getClass().getClassLoader();
  }

  @Implementation(minSdk = VERSION_CODES.O)
  public ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException {
    return this.getClass().getClassLoader();
  }

  /** Accessor interface for {@link LoadedApk}'s internals. */
  @ForType(LoadedApk.class)
  public interface _LoadedApk_ {

    @Accessor("mApplication")
    void setApplication(Application application);

    @Accessor("mResources")
    void setResources(Resources resources);
  }
}
