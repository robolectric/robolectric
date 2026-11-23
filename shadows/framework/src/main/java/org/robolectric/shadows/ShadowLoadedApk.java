package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.newInstanceOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Application;
import android.app.LoadedApk;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(value = LoadedApk.class, isInAndroidSdk = false)
public class ShadowLoadedApk {
  @RealObject private LoadedApk realLoadedApk;
  private boolean isClassLoaderInitialized = false;
  private final Object classLoaderLock = new Object();
  private final Map<String, ClassLoader> splitClassLoaders = new HashMap<>();

  @Implementation
  public ClassLoader getClassLoader() {
    // The AppComponentFactory was introduced from SDK 28.
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.P) {
      synchronized (classLoaderLock) {
        if (!isClassLoaderInitialized) {
          isClassLoaderInitialized = true;
          tryInitAppComponentFactory(realLoadedApk);
        }
      }
    }
    return this.getClass().getClassLoader();
  }

  @Implementation(minSdk = VERSION_CODES.O)
  public ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException {
    if (splitName == null) {
      return getClassLoader();
    }
    ApplicationInfo appInfo = realLoadedApk.getApplicationInfo();
    if (appInfo != null && appInfo.splitNames != null && appInfo.splitNames.length > 0) {
      if (!Arrays.asList(appInfo.splitNames).contains(splitName)) {
        throw new NameNotFoundException("Unknown split name: " + splitName);
      }
      if (appInfo.requestsIsolatedSplitLoading()) {
        ClassLoader parent = getClassLoader();
        synchronized (classLoaderLock) {
          return splitClassLoaders.computeIfAbsent(
              splitName, k -> new URLClassLoader(new URL[0], parent));
        }
      }
    }
    return getClassLoader();
  }

  private void tryInitAppComponentFactory(LoadedApk realLoadedApk) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.P) {
      ApplicationInfo applicationInfo = realLoadedApk.getApplicationInfo();
      if (applicationInfo == null || applicationInfo.appComponentFactory == null) {
        return;
      }
      LoadedApkReflector loadedApkReflector = reflector(LoadedApkReflector.class, realLoadedApk);
      if (!loadedApkReflector.getIncludeCode()) {
        return;
      }
      String fullQualifiedClassName =
          calculateFullQualifiedClassName(
              applicationInfo.appComponentFactory, applicationInfo.packageName);
      android.app.AppComponentFactory factory =
          (android.app.AppComponentFactory) newInstanceOf(fullQualifiedClassName);
      if (factory == null) {
        factory = new android.app.AppComponentFactory();
      }
      loadedApkReflector.setAppFactory(factory);
    }
  }

  private String calculateFullQualifiedClassName(String className, String packageName) {
    if (packageName == null) {
      return className;
    }
    return className.startsWith(".") ? packageName + className : className;
  }

  /** Accessor interface for {@link LoadedApk}'s internals. */
  @ForType(LoadedApk.class)
  public interface LoadedApkReflector {

    @Accessor("mApplication")
    void setApplication(Application application);

    @Accessor("mResources")
    void setResources(Resources resources);

    @Accessor("mIncludeCode")
    boolean getIncludeCode();

    @Accessor("mAppComponentFactory")
    void setAppFactory(Object appFactory);

    ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException;
  }
}
