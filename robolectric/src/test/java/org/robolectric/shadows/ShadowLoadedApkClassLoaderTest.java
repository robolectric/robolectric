package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.app.LoadedApk;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.net.URLClassLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Tests for ClassLoader sharing support in {@link ShadowLoadedApk}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.O)
public class ShadowLoadedApkClassLoaderTest {

  private ShadowLoadedApk shadowLoadedApk;

  @Before
  public void setUp() {
    // Obtain the real LoadedApk from the ContextImpl backing the test application.
    android.content.Context base = RuntimeEnvironment.getApplication().getBaseContext();
    LoadedApk loadedApk = ReflectionHelpers.getField(base, "mPackageInfo");
    shadowLoadedApk = Shadow.extract(loadedApk);
    // Register some test splits so getSplitClassLoader() does not throw NameNotFoundException.
    shadowLoadedApk.registerSplitNames("feature_camera", "feature_maps", "config.xxhdpi");
  }

  @Test
  public void getSplitClassLoader_withoutExplicitLoader_returnsDefaultClassLoader()
      throws NameNotFoundException {
    ClassLoader cl = shadowLoadedApk.getSplitClassLoader("feature_camera");
    assertThat(cl).isNotNull();
  }

  @Test
  public void setSplitClassLoader_returnsRegisteredClassLoader() throws NameNotFoundException {
    ClassLoader custom = new URLClassLoader(new java.net.URL[0], getClass().getClassLoader());
    shadowLoadedApk.setSplitClassLoader("feature_camera", custom);

    assertThat(shadowLoadedApk.getSplitClassLoader("feature_camera")).isSameInstanceAs(custom);
  }

  @Test
  public void setSplitClassLoader_doesNotAffectOtherSplits() throws NameNotFoundException {
    ClassLoader custom = new URLClassLoader(new java.net.URL[0], getClass().getClassLoader());
    shadowLoadedApk.setSplitClassLoader("feature_camera", custom);

    ClassLoader other = shadowLoadedApk.getSplitClassLoader("feature_maps");
    assertThat(other).isNotSameInstanceAs(custom);
  }

  @Test
  public void createIsolatedSplitClassLoader_returnsDifferentInstanceThanDefault()
      throws NameNotFoundException {
    ClassLoader defaultCl = shadowLoadedApk.getSplitClassLoader("feature_camera");
    ClassLoader isolated = shadowLoadedApk.createIsolatedSplitClassLoader("feature_camera");

    assertThat(isolated).isInstanceOf(URLClassLoader.class);
    // The isolated loader is a child of the app's loader.
    assertThat(isolated.getParent()).isSameInstanceAs(defaultCl);
  }

  @Test
  public void createIsolatedSplitClassLoader_sameCallReturnsSameInstance() {
    ClassLoader first = shadowLoadedApk.createIsolatedSplitClassLoader("feature_camera");
    ClassLoader second = shadowLoadedApk.createIsolatedSplitClassLoader("feature_camera");

    assertThat(first).isSameInstanceAs(second);
  }

  @Test
  public void createIsolatedSplitClassLoader_differentSplitsGetDifferentLoaders() {
    ClassLoader cameraLoader = shadowLoadedApk.createIsolatedSplitClassLoader("feature_camera");
    ClassLoader mapsLoader = shadowLoadedApk.createIsolatedSplitClassLoader("feature_maps");

    assertThat(cameraLoader).isNotSameInstanceAs(mapsLoader);
  }

  @Test
  public void setSplitClassLoader_overridesIsolatedLoader() throws NameNotFoundException {
    shadowLoadedApk.createIsolatedSplitClassLoader("feature_camera");
    ClassLoader custom = new URLClassLoader(new java.net.URL[0], getClass().getClassLoader());
    shadowLoadedApk.setSplitClassLoader("feature_camera", custom);

    assertThat(shadowLoadedApk.getSplitClassLoader("feature_camera")).isSameInstanceAs(custom);
  }

  @Test
  public void getSplitClassLoader_unknownSplitThrowsNameNotFoundException() {
    assertThrows(
        NameNotFoundException.class,
        () -> shadowLoadedApk.getSplitClassLoader("nonexistent_split"));
  }
}
