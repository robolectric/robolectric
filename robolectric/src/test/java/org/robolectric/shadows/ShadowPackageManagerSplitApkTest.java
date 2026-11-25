package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Application;
import android.app.LoadedApk;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContextImpl.ContextImplReflector;
import org.robolectric.util.reflector.ForType;

/** Tests for split APK support in {@link ShadowPackageManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowPackageManagerSplitApkTest {

  private Application application;
  private PackageManager packageManager;

  @Before
  public void setUp() {
    application = ApplicationProvider.getApplicationContext();
    packageManager = application.getPackageManager();
  }

  @Test
  @Config(minSdk = O)
  public void installPackage_propagatesSplitNamesFromPackageInfoToApplicationInfo()
      throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.frompkg";
    packageInfo.splitNames = new String[] {"feature_a", "config.xxhdpi"};

    shadowOf(packageManager).installPackage(packageInfo);

    ApplicationInfo appInfo = packageManager.getApplicationInfo("com.example.frompkg", 0);
    assertThat(appInfo.splitNames).asList().containsExactly("feature_a", "config.xxhdpi").inOrder();
  }

  @Test
  @Config(minSdk = O)
  public void installPackage_propagatesSplitNamesFromApplicationInfoToPackageInfo()
      throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.fromapp";
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = "com.example.fromapp";
    packageInfo.applicationInfo.splitNames = new String[] {"feature_b"};

    shadowOf(packageManager).installPackage(packageInfo);

    PackageInfo installed = packageManager.getPackageInfo("com.example.fromapp", 0);
    assertThat(installed.splitNames).asList().containsExactly("feature_b");
  }

  @Test
  public void installPackage_propagatesSplitPublicSourceDirsFromSplitSourceDirs() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.dirs";
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = "com.example.dirs";
    packageInfo.applicationInfo.splitSourceDirs = new String[] {"/path/to/split_a.apk"};

    shadowOf(packageManager).installPackage(packageInfo);

    ApplicationInfo appInfo = packageManager.getApplicationInfo("com.example.dirs", 0);
    assertThat(appInfo.splitPublicSourceDirs).asList().containsExactly("/path/to/split_a.apk");
  }

  @Test
  public void addSplit_uninstalledPackage_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> shadowOf(packageManager).addSplit("com.example.missing", "feature_a", null));
  }

  @Test
  public void addSplit_toAppUnderTest_updatesPackageManagerAppInfoLoadedApkAndAssets()
      throws Exception {
    String ownPkg = application.getPackageName();
    PackageInfo before = packageManager.getPackageInfo(ownPkg, PackageManager.GET_ACTIVITIES);
    int activityCountBefore = before.activities != null ? before.activities.length : 0;

    Path splitApk = createAssetSplitApk("feature_camera", "camera/config.txt", "fps=60");
    shadowOf(packageManager).addSplit(ownPkg, "feature_camera", splitApk.toString());

    PackageInfo after = packageManager.getPackageInfo(ownPkg, PackageManager.GET_ACTIVITIES);
    assertThat(after.splitNames).asList().contains("feature_camera");
    assertThat(after.activities != null ? after.activities.length : 0)
        .isEqualTo(activityCountBefore);

    ApplicationInfo appInfo = application.getApplicationInfo();
    assertThat(appInfo.splitSourceDirs).asList().contains(splitApk.toString());
    assertThat(appInfo.splitPublicSourceDirs).asList().contains(splitApk.toString());
    if (RuntimeEnvironment.getApiLevel() >= O) {
      assertThat(appInfo.splitNames).asList().contains("feature_camera");
      LoadedApk loadedApk =
          reflector(ContextImplReflector.class, application.getBaseContext()).getPackageInfo();
      assertThat(
              reflector(TestLoadedApkReflector.class, loadedApk)
                  .getSplitClassLoader("feature_camera"))
          .isNotNull();
      Context splitContext = application.createContextForSplit("feature_camera");
      assertThat(splitContext).isNotNull();
    }

    assertThat(readAsset(application.getAssets(), "camera/config.txt")).isEqualTo("fps=60");
  }

  @Test
  public void addSplit_toAppUnderTest_withoutApkPath_updatesMetadataOnly() throws Exception {
    String ownPkg = application.getPackageName();
    shadowOf(packageManager).addSplit(ownPkg, "feature_metadata_only", null);

    PackageInfo info = packageManager.getPackageInfo(ownPkg, 0);
    assertThat(info.splitNames).asList().contains("feature_metadata_only");
    if (RuntimeEnvironment.getApiLevel() >= O) {
      assertThat(application.getApplicationInfo().splitNames)
          .asList()
          .contains("feature_metadata_only");
    }
  }

  @Test
  public void addSplit_toAnotherPackage_loadsAssetsViaGetResourcesForApplication()
      throws Exception {
    String pkgName = "com.example.other";
    PackageInfo pkg = new PackageInfo();
    pkg.packageName = pkgName;
    shadowOf(packageManager).installPackage(pkg);

    Path split1 = createAssetSplitApk("feature_one", "one.txt", "hello-one");
    shadowOf(packageManager).addSplit(pkgName, "feature_one", split1.toString());

    Resources resources = packageManager.getResourcesForApplication(pkgName);
    assertThat(readAsset(resources.getAssets(), "one.txt")).isEqualTo("hello-one");

    Path split2 = createAssetSplitApk("feature_two", "two.txt", "hello-two");
    shadowOf(packageManager).addSplit(pkgName, "feature_two", split2.toString());

    assertThat(readAsset(resources.getAssets(), "two.txt")).isEqualTo("hello-two");
    PackageInfo updated = packageManager.getPackageInfo(pkgName, 0);
    assertThat(updated.splitNames).asList().containsExactly("feature_one", "feature_two").inOrder();
  }

  @Test
  public void addSplit_updatingExistingSplit_replacesPathWithoutDuplicatingName() throws Exception {
    String pkgName = "com.example.update";
    PackageInfo pkg = new PackageInfo();
    pkg.packageName = pkgName;
    shadowOf(packageManager).installPackage(pkg);

    Path v1 = createAssetSplitApk("feature_v1", "ver.txt", "v1");
    Path v2 = createAssetSplitApk("feature_v2", "ver.txt", "v2");

    shadowOf(packageManager).addSplit(pkgName, "feature", v1.toString());
    assertThat(readAsset(packageManager.getResourcesForApplication(pkgName).getAssets(), "ver.txt"))
        .isEqualTo("v1");

    shadowOf(packageManager).addSplit(pkgName, "feature", v2.toString());
    PackageInfo updated = packageManager.getPackageInfo(pkgName, 0);
    assertThat(updated.splitNames).asList().containsExactly("feature");
    assertThat(readAsset(packageManager.getResourcesForApplication(pkgName).getAssets(), "ver.txt"))
        .isEqualTo("v2");
  }

  private static Path createAssetSplitApk(String prefix, String assetPath, String content)
      throws IOException {
    Path dir = RuntimeEnvironment.getTempDirectory().createIfNotExists("split-test-apks");
    Path apkFile = Files.createTempFile(dir, prefix + "-", ".apk");
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(apkFile))) {
      zos.putNextEntry(new ZipEntry("assets/" + assetPath));
      zos.write(content.getBytes(UTF_8));
      zos.closeEntry();
    }
    return apkFile;
  }

  private static String readAsset(android.content.res.AssetManager assets, String path)
      throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(assets.open(path), UTF_8))) {
      return reader.readLine();
    }
  }

  @ForType(className = "android.app.LoadedApk")
  interface TestLoadedApkReflector {
    ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException;
  }
}
