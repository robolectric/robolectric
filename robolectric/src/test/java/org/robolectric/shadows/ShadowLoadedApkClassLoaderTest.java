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
import java.net.URLClassLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContextImpl.ContextImplReflector;
import org.robolectric.shadows.ShadowLoadedApk.LoadedApkReflector;

/** Tests for split ClassLoader support in {@link ShadowLoadedApk}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.O)
public class ShadowLoadedApkClassLoaderTest {

  private Context baseContext;
  private ApplicationInfo appInfo;
  private LoadedApkReflector loadedApkReflector;
  private ClassLoader baseClassLoader;

  @Before
  public void setUp() {
    baseContext = RuntimeEnvironment.getApplication().getBaseContext();
    LoadedApk loadedApk = reflector(ContextImplReflector.class, baseContext).getPackageInfo();
    appInfo = loadedApk.getApplicationInfo();
    appInfo.splitNames = new String[] {"feature_camera", "feature_maps", "config.xxhdpi"};
    loadedApkReflector = reflector(LoadedApkReflector.class, loadedApk);
    baseClassLoader = loadedApk.getClassLoader();
  }

  @Test
  public void getSplitClassLoader_nullSplitName_returnsBaseClassLoader()
      throws NameNotFoundException {
    assertThat(loadedApkReflector.getSplitClassLoader(null)).isSameInstanceAs(baseClassLoader);
  }

  @Test
  public void getSplitClassLoader_withoutIsolatedSplits_returnsBaseClassLoader()
      throws NameNotFoundException {
    ClassLoader cl = loadedApkReflector.getSplitClassLoader("feature_camera");
    assertThat(cl).isSameInstanceAs(baseClassLoader);
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
  public void getSplitClassLoader_isolatedSplits_returnsChildOfBaseClassLoader()
      throws NameNotFoundException {
    appInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_ISOLATED_SPLIT_LOADING;

    ClassLoader isolated = loadedApkReflector.getSplitClassLoader("feature_camera");

    assertThat(isolated).isInstanceOf(URLClassLoader.class);
    assertThat(isolated.getParent()).isSameInstanceAs(baseClassLoader);
  }

  @Test
  public void getSplitClassLoader_isolatedSplits_sameSplitReturnsSameInstance()
      throws NameNotFoundException {
    appInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_ISOLATED_SPLIT_LOADING;

    ClassLoader first = loadedApkReflector.getSplitClassLoader("feature_camera");
    ClassLoader second = loadedApkReflector.getSplitClassLoader("feature_camera");

    assertThat(first).isSameInstanceAs(second);
  }

  @Test
  public void getSplitClassLoader_isolatedSplits_differentSplitsGetDifferentLoaders()
      throws NameNotFoundException {
    appInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_ISOLATED_SPLIT_LOADING;

    ClassLoader cameraLoader = loadedApkReflector.getSplitClassLoader("feature_camera");
    ClassLoader mapsLoader = loadedApkReflector.getSplitClassLoader("feature_maps");

    assertThat(cameraLoader).isNotSameInstanceAs(mapsLoader);
  }

  @Test
  public void getSplitClassLoader_isolatedSplits_nullSplitReturnsBaseClassLoader()
      throws NameNotFoundException {
    appInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_ISOLATED_SPLIT_LOADING;

    assertThat(loadedApkReflector.getSplitClassLoader(null)).isSameInstanceAs(baseClassLoader);
  }

  @Test
  public void createContextForSplit_isolatedSplits_usesSplitClassLoaderAndValidatesSplitName()
      throws NameNotFoundException {
    appInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_ISOLATED_SPLIT_LOADING;

    Context splitContext = baseContext.createContextForSplit("feature_camera");

    assertThat(splitContext.getClassLoader())
        .isSameInstanceAs(loadedApkReflector.getSplitClassLoader("feature_camera"));
    assertThat(splitContext.getClassLoader().getParent())
        .isSameInstanceAs(baseContext.getClassLoader());
    assertThrows(
        NameNotFoundException.class, () -> baseContext.createContextForSplit("nonexistent_split"));
  }
}
