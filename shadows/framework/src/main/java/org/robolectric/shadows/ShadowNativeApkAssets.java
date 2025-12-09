package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.shadows.ShadowArscApkAssets9.FRAMEWORK_APK_PATH;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.ApkAssets;
import android.content.res.loader.AssetsProvider;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.Direct.DirectFormat;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(
    value = ApkAssets.class,
    minSdk = VANILLA_ICE_CREAM,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowApkAssets.Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeApkAssets extends ShadowApkAssets {

  // cached native apk assets.
  // native resources currently cannot use the cross-sandbox ApkAssetsCache cache
  // because each sandbox currently loads its own copy of the native runtime
  private static final Map<String, Long> cachedApkAssetsPtrs = new HashMap<>();

  @Implementation
  protected static long nativeLoad(int format, String path, int flags, AssetsProvider asset) {
    boolean system = false;
    if (path.equals(FRAMEWORK_APK_PATH)) {
      path = RuntimeEnvironment.getAndroidFrameworkJarPath().toString();
      system = true;
    }
    if (cachedApkAssetsPtrs.containsKey(path)) {
      return cachedApkAssetsPtrs.get(path);
    }
    final String adjustedPath = path;
    return PerfStatsCollector.getInstance()
        .measure(
            "load native " + (system ? "framework" : "app") + " resources",
            () -> {
              long ptr =
                  reflector(ApkAssetsReflector.class)
                      .nativeLoad(format, adjustedPath, flags, asset);
              if (ptr > 0) {
                cachedApkAssetsPtrs.put(adjustedPath, ptr);
              }
              return ptr;
            });
  }

  @Implementation
  protected static void nativeDestroy(long ptr) {
    // ignoring nativeDestroy in order to cache assets across tests
  }

  @ForType(ApkAssets.class)
  interface ApkAssetsReflector {
    @Static
    @Direct(format = DirectFormat.NATIVE)
    long nativeLoad(int format, String path, int flags, AssetsProvider asset);
  }
}
