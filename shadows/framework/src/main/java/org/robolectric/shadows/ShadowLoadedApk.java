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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
  private final Set<String> registeredSplitNames = new HashSet<>();
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
    // If the app has registered split names, validate the requested split
    Set<String> allSplits = new HashSet<>(registeredSplitNames);
    ApplicationInfo appInfo = realLoadedApk.getApplicationInfo();
    if (appInfo != null && appInfo.splitNames != null) {
      allSplits.addAll(Arrays.asList(appInfo.splitNames));
    }

    if (!allSplits.isEmpty() && !allSplits.contains(splitName)) {
      throw new NameNotFoundException("Unknown split name: " + splitName);
    }
    ClassLoader registered = splitClassLoaders.get(splitName);
    if (registered != null) {
      return registered;
    }
    return this.getClass().getClassLoader();
  }

  /**
   * Registers split names that this LoadedApk knows about. After registration, {@link
   * #getSplitClassLoader(String)} will throw {@link NameNotFoundException} for unregistered split
   * names.
   */
  public void registerSplitNames(String... splitNames) {
    registeredSplitNames.addAll(Arrays.asList(splitNames));
  }

  /**
   * Registers an explicit {@link ClassLoader} for the given split. Subsequent calls to {@link
   * #getSplitClassLoader(String)} with the same {@code splitName} will return this loader.
   */
  public void setSplitClassLoader(String splitName, ClassLoader classLoader) {
    splitClassLoaders.put(splitName, classLoader);
  }

  /**
   * Creates and caches an isolated child {@link ClassLoader} for the given split. The returned
   * loader has no entries of its own but delegates unknown classes to the app's main ClassLoader,
   * simulating Android's split isolation behavior. Repeated calls with the same {@code splitName}
   * return the same instance.
   */
  public ClassLoader createIsolatedSplitClassLoader(String splitName) {
    return splitClassLoaders.computeIfAbsent(
        splitName, k -> new URLClassLoader(new URL[0], this.getClass().getClassLoader()));
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
  }
}
