package org.robolectric.shadows;

import static org.robolectric.shadows.ShadowArscApkAssets9.FRAMEWORK_APK_PATH;

import android.content.res.ApkAssets;
import android.content.res.loader.AssetsProvider;
import java.io.IOException;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.versioning.AndroidVersions.V;

@Implements(
    value = ApkAssets.class,
    minSdk = V.SDK_INT,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowApkAssets.Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeApkAssets extends ShadowApkAssets {
  @Implementation
  protected static long nativeLoad(int format, String path, int flags, AssetsProvider asset)
      throws IOException {
    if (path.equals(FRAMEWORK_APK_PATH)) {
      path = RuntimeEnvironment.getAndroidFrameworkJarPath().toString();
    }
    return ReflectionHelpers.callStaticMethod(
        ApkAssets.class,
        Shadow.directNativeMethodName(ApkAssets.class.getName(), "nativeLoad"),
        ClassParameter.from(int.class, format),
        ClassParameter.from(String.class, path),
        ClassParameter.from(int.class, flags),
        ClassParameter.from(AssetsProvider.class, asset));
  }
}
