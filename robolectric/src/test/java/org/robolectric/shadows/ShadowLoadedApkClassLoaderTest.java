package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.LoadedApk;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContextImpl.ContextImplReflector;
import org.robolectric.util.reflector.ForType;

/** Tests for split ClassLoader validation in {@link ShadowLoadedApk}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.O)
public class ShadowLoadedApkClassLoaderTest {

  private Context baseContext;
  private ApplicationInfo appInfo;
  private TestLoadedApkReflector loadedApkReflector;
  private ClassLoader baseClassLoader;

  @Before
  public void setUp() {
    baseContext = RuntimeEnvironment.getApplication().getBaseContext();
    LoadedApk loadedApk = reflector(ContextImplReflector.class, baseContext).getPackageInfo();
    appInfo = loadedApk.getApplicationInfo();
    appInfo.splitNames = new String[] {"feature_camera", "feature_maps", "config.xxhdpi"};
    loadedApkReflector = reflector(TestLoadedApkReflector.class, loadedApk);
    baseClassLoader = loadedApk.getClassLoader();
  }

  @Test
  public void getSplitClassLoader_nullSplitName_returnsBaseClassLoader()
      throws NameNotFoundException {
    assertThat(loadedApkReflector.getSplitClassLoader(null)).isSameInstanceAs(baseClassLoader);
  }

  @Test
  public void getSplitClassLoader_knownSplit_returnsBaseClassLoader() throws NameNotFoundException {
    assertThat(loadedApkReflector.getSplitClassLoader("feature_camera"))
        .isSameInstanceAs(baseClassLoader);
  }

  @Test
  public void getSplitClassLoader_noSplitsConfigured_returnsBaseClassLoader()
      throws NameNotFoundException {
    appInfo.splitNames = null;

    assertThat(loadedApkReflector.getSplitClassLoader("any_split"))
        .isSameInstanceAs(baseClassLoader);
  }

  @Test
  public void getSplitClassLoader_unknownSplitThrowsNameNotFoundException() {
    assertThrows(
        NameNotFoundException.class,
        () -> loadedApkReflector.getSplitClassLoader("nonexistent_split"));
  }

  @Test
  public void createContextForSplit_isolatedSplits_validatesSplitName()
      throws NameNotFoundException {
    appInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_ISOLATED_SPLIT_LOADING;

    Context splitContext = baseContext.createContextForSplit("feature_camera");

    assertThat(splitContext.getClassLoader()).isSameInstanceAs(baseClassLoader);
    assertThrows(
        NameNotFoundException.class, () -> baseContext.createContextForSplit("nonexistent_split"));
  }

  @ForType(className = "android.app.LoadedApk")
  interface TestLoadedApkReflector {
    ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException;
  }
}
