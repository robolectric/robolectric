package org.robolectric.integrationtests.splitapk;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPackageManager;

/**
 * Tests modeled after the DynamicFeatures sample's {@code MyApplication} class which uses {@code
 * SplitCompat.install(this)} in {@code attachBaseContext()}.
 *
 * <p>In the official sample (https://github.com/android/app-bundle-samples), MyApplication extends
 * Application and calls SplitCompat.install() to enable access to code and resources from dynamic
 * feature modules. This test verifies that Robolectric's split APK infrastructure supports the same
 * split metadata patterns that SplitCompat relies on.
 *
 * <p>Reference: {@code DynamicFeatures/app/src/main/java/.../MyApplication.kt}
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.O)
public class SplitCompatApplicationTest {

  private static final String PACKAGE_NAME = "com.google.android.samples.dynamicfeatures";

  private PackageManager packageManager;
  private ShadowPackageManager shadowPackageManager;

  @Before
  public void setUp() {
    packageManager = RuntimeEnvironment.getApplication().getPackageManager();
    shadowPackageManager = shadowOf(packageManager);
  }

  /**
   * Verifies that a base app can be installed with split metadata, mirroring how SplitCompat
   * discovers installed splits via ApplicationInfo.splitNames and splitSourceDirs.
   *
   * <p>In the real DynamicFeatures app, after SplitCompat.install(), the framework reads
   * ApplicationInfo to find available split APKs.
   */
  @Test
  public void splitCompat_applicationInfoReflectsSplits() throws Exception {
    // Install base app with dynamic features (mimicking post-Play-Store install state)
    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames =
        new String[] {"feature_kotlin", "feature_java", "feature_assets", "initialInstall"};

    Map<String, String> splitPaths = new LinkedHashMap<>();
    for (String splitName : info.applicationInfo.splitNames) {
      splitPaths.put(
          splitName,
          ShadowPackageManager.createSplitApkWithAssets(
              splitName, Map.of(splitName + ".dat", splitName.getBytes(StandardCharsets.UTF_8))));
    }
    shadowPackageManager.installPackageWithSplitApks(info, splitPaths);

    // SplitCompat reads ApplicationInfo to discover splits
    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    ApplicationInfo appInfo = installed.applicationInfo;

    assertThat(appInfo.splitNames).hasLength(4);
    assertThat(appInfo.splitNames)
        .asList()
        .containsExactly("feature_kotlin", "feature_java", "feature_assets", "initialInstall");
    assertThat(appInfo.splitSourceDirs).hasLength(4);
    // SplitCompat uses splitPublicSourceDirs as well
    assertThat(appInfo.splitPublicSourceDirs).isEqualTo(appInfo.splitSourceDirs);
  }

  /**
   * Verifies that the base app can be queried even with no splits installed, matching the initial
   * install state before any dynamic features are downloaded.
   */
  @Test
  public void splitCompat_noSplitsInstalled_baseAppAccessible() throws Exception {
    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    shadowPackageManager.installPackage(info);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).isNull();
    assertThat(installed.applicationInfo.splitSourceDirs).isNull();
  }

  /**
   * Verifies that PackageInfo.splitNames is synced with ApplicationInfo.splitNames, as both are
   * used by different parts of the SplitCompat framework.
   */
  @Test
  public void splitCompat_packageInfoAndAppInfoSplitNamesInSync() throws Exception {
    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = new String[] {"feature_kotlin", "feature_java"};
    info.splitNames = new String[] {"feature_kotlin", "feature_java"};

    Map<String, String> splits = new LinkedHashMap<>();
    splits.put(
        "feature_kotlin",
        ShadowPackageManager.createSplitApkWithAssets(
            "feature_kotlin", Map.of("k.txt", "kt".getBytes(StandardCharsets.UTF_8))));
    splits.put(
        "feature_java",
        ShadowPackageManager.createSplitApkWithAssets(
            "feature_java", Map.of("j.txt", "java".getBytes(StandardCharsets.UTF_8))));
    shadowPackageManager.installPackageWithSplitApks(info, splits);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.splitNames).asList().containsExactly("feature_kotlin", "feature_java");
    assertThat(installed.applicationInfo.splitNames)
        .asList()
        .containsExactly("feature_kotlin", "feature_java");
  }
}
