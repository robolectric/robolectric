package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for split APK support in {@link ShadowPackageManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.O)
public class ShadowPackageManagerSplitApkTest {

  private static final String TEST_PACKAGE_NAME = "com.example.splitapk";
  private Context context;
  private PackageManager packageManager;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    packageManager = context.getPackageManager();
  }

  @Test
  public void installPackageWithSplits_setsSplitNames() throws NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager)
        .installPackageWithSplits(packageInfo, "config.hdpi", "config.x86", "feature_camera");

    PackageInfo retrieved = packageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
    assertThat(retrieved.applicationInfo.splitNames)
        .asList()
        .containsExactly("config.hdpi", "config.x86", "feature_camera");
  }

  @Test
  public void installPackageWithSplits_setsSplitSourceDirs() throws NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager).installPackageWithSplits(packageInfo, "config.hdpi", "config.x86");

    PackageInfo retrieved = packageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
    assertThat(retrieved.applicationInfo.splitSourceDirs).isNotNull();
    assertThat(retrieved.applicationInfo.splitSourceDirs).hasLength(2);
  }

  @Test
  public void installPackageWithSplits_setsSplitPublicSourceDirs() throws NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager).installPackageWithSplits(packageInfo, "config.hdpi");

    PackageInfo retrieved = packageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
    assertThat(retrieved.applicationInfo.splitPublicSourceDirs).isNotNull();
    assertThat(retrieved.applicationInfo.splitPublicSourceDirs).hasLength(1);
    assertThat(retrieved.applicationInfo.splitPublicSourceDirs)
        .isEqualTo(retrieved.applicationInfo.splitSourceDirs);
  }

  @Test
  public void installPackageWithSplits_propagatesSplitNamesToPackageInfo()
      throws NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager).installPackageWithSplits(packageInfo, "base_config", "dynamic_map");

    PackageInfo retrieved = packageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
    assertThat(retrieved.splitNames).asList().containsExactly("base_config", "dynamic_map");
  }

  @Test
  public void installPackage_withSplitNamesAndSourceDirs_preservesThem()
      throws NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.splitNames = new String[] {"feature_photos"};
    packageInfo.applicationInfo.splitSourceDirs = new String[] {"/data/app/split_feature.apk"};
    packageInfo.applicationInfo.splitPublicSourceDirs =
        new String[] {"/data/app/split_feature.apk"};

    shadowOf(packageManager).installPackage(packageInfo);

    PackageInfo retrieved = packageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
    assertThat(retrieved.applicationInfo.splitSourceDirs).isNotNull();
    assertThat(retrieved.applicationInfo.splitSourceDirs).hasLength(1);
    assertThat(retrieved.applicationInfo.splitSourceDirs[0])
        .isEqualTo("/data/app/split_feature.apk");
  }

  @Test
  public void installPackage_withPresetSplitSourceDirs_preservesThem()
      throws NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.splitNames = new String[] {"config.hdpi"};
    packageInfo.applicationInfo.splitSourceDirs = new String[] {"/data/app/split_config.hdpi.apk"};
    packageInfo.applicationInfo.splitPublicSourceDirs =
        new String[] {"/data/app/split_config.hdpi.apk"};

    shadowOf(packageManager).installPackage(packageInfo);

    PackageInfo retrieved = packageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
    assertThat(retrieved.applicationInfo.splitSourceDirs)
        .asList()
        .containsExactly("/data/app/split_config.hdpi.apk");
    assertThat(retrieved.applicationInfo.splitPublicSourceDirs)
        .asList()
        .containsExactly("/data/app/split_config.hdpi.apk");
  }

  @Test
  public void installPackage_withoutSplits_splitFieldsAreNull() throws NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager).installPackage(packageInfo);

    PackageInfo retrieved = packageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
    assertThat(retrieved.applicationInfo.splitNames).isNull();
    assertThat(retrieved.applicationInfo.splitSourceDirs).isNull();
    assertThat(retrieved.applicationInfo.splitPublicSourceDirs).isNull();
  }

  @Test
  public void installPackageWithSplits_configSplitsPattern() throws NameNotFoundException {
    // Simulates a typical AAB configuration splits scenario
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager)
        .installPackageWithSplits(
            packageInfo, "config.hdpi", "config.xxhdpi", "config.en", "config.arm64_v8a");

    PackageInfo retrieved = packageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
    assertThat(retrieved.applicationInfo.splitNames).hasLength(4);
    assertThat(retrieved.applicationInfo.splitSourceDirs).hasLength(4);
    assertThat(retrieved.applicationInfo.splitPublicSourceDirs).hasLength(4);
    // Each split should have its own unique directory
    assertThat(retrieved.applicationInfo.splitSourceDirs[0])
        .isNotEqualTo(retrieved.applicationInfo.splitSourceDirs[1]);
  }

  @Test
  public void installPackageWithSplits_dynamicFeaturePattern() throws NameNotFoundException {
    // Simulates dynamic feature modules from an AAB
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager)
        .installPackageWithSplits(packageInfo, "feature_camera", "feature_gallery");

    PackageInfo retrieved = packageManager.getPackageInfo(TEST_PACKAGE_NAME, 0);
    assertThat(retrieved.applicationInfo.splitNames)
        .asList()
        .containsExactly("feature_camera", "feature_gallery");
  }

  @Test
  public void getApplicationInfo_withSplits_returnsSplitInfo() throws NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager).installPackageWithSplits(packageInfo, "config.hdpi", "config.en");

    ApplicationInfo appInfo = packageManager.getApplicationInfo(TEST_PACKAGE_NAME, 0);
    assertThat(appInfo.splitNames).asList().containsExactly("config.hdpi", "config.en");
    assertThat(appInfo.splitSourceDirs).isNotNull();
    assertThat(appInfo.splitSourceDirs).hasLength(2);
  }
}
