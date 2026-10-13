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
 * Tests for Android App Bundle configuration splits (density, ABI, language).
 *
 * <p>When the Play Store installs an AAB-based app, it generates config splits tailored to the
 * target device. These are in addition to any dynamic feature splits. The DynamicFeatures sample
 * relies on the Play Store to generate these splits, but in tests we need to verify the underlying
 * infrastructure handles them correctly.
 *
 * <p>Config split naming follows bundletool convention:
 *
 * <ul>
 *   <li>Density: {@code base-xxhdpi.apk}, {@code base-mdpi.apk}
 *   <li>ABI: {@code base-arm64_v8a.apk}, {@code base-x86_64.apk}
 *   <li>Language: {@code base-en.apk}, {@code base-pl.apk}
 *   <li>Feature config: {@code feature_kotlin-xxhdpi.apk}
 * </ul>
 *
 * <p>The DynamicFeatures sample supports language switching (English/Polish) via LanguageHelper,
 * which is backed by config splits containing locale-specific resources.
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.O)
public class ConfigSplitsTest {

  private static final String PACKAGE_NAME = "com.google.android.samples.dynamicfeatures";

  private PackageManager packageManager;
  private ShadowPackageManager shadowPackageManager;

  @Before
  public void setUp() {
    packageManager = RuntimeEnvironment.getApplication().getPackageManager();
    shadowPackageManager = shadowOf(packageManager);
  }

  /**
   * Simulates the typical Play Store installation with density, ABI, and language config splits
   * alongside the base module, matching what a real device would receive for the DynamicFeatures
   * app.
   */
  @Test
  public void playStoreInstall_configSplitsForDevice() throws Exception {
    String[] splitNames = {
      "base-xxhdpi", // density split
      "base-arm64_v8a", // ABI split
      "base-en" // language split (English, default for DynamicFeatures)
    };

    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = splitNames;

    Map<String, String> splits = new LinkedHashMap<>();
    for (String name : splitNames) {
      splits.put(
          name,
          ShadowPackageManager.createSplitApkWithAssets(
              name, Map.of(name + ".cfg", name.getBytes(StandardCharsets.UTF_8))));
    }
    shadowPackageManager.installPackageWithSplitApks(info, splits);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames)
        .asList()
        .containsExactly("base-xxhdpi", "base-arm64_v8a", "base-en");
    assertThat(installed.applicationInfo.splitSourceDirs).hasLength(3);
  }

  /**
   * Simulates language switching from English to Polish, as supported by the DynamicFeatures
   * sample's LanguageHelper. When a user selects Polish, the Play Store downloads the {@code
   * base-pl} config split via SplitInstallManager.startInstall() with addLanguage(Locale("pl")).
   */
  @Test
  public void languageConfigSplit_polishLanguageAdded() throws Exception {
    // Initial install with English
    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = new String[] {"base-en"};

    Map<String, String> splits = new LinkedHashMap<>();
    splits.put(
        "base-en",
        ShadowPackageManager.createSplitApkWithAssets(
            "base-en", Map.of("strings-en.dat", "English".getBytes(StandardCharsets.UTF_8))));
    shadowPackageManager.installPackageWithSplitApks(info, splits);

    // User selects Polish → SplitInstallManager downloads base-pl split
    String plSplitPath =
        ShadowPackageManager.createSplitApkWithAssets(
            "base-pl", Map.of("strings-pl.dat", "Polish".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, "base-pl", plSplitPath);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).asList().containsExactly("base-en", "base-pl");
  }

  /**
   * Verifies that config splits for a dynamic feature module (e.g., feature_kotlin-xxhdpi) can
   * coexist with base config splits and the feature module itself.
   */
  @Test
  public void featureConfigSplits_coexistWithBaseAndFeature() throws Exception {
    String[] splitNames = {
      "base-xxhdpi", // base density config
      "base-en", // base language config
      "feature_kotlin", // the feature module itself
      "feature_kotlin-xxhdpi" // feature's density config
    };

    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = splitNames;

    Map<String, String> splits = new LinkedHashMap<>();
    for (String name : splitNames) {
      splits.put(
          name,
          ShadowPackageManager.createSplitApkWithAssets(
              name, Map.of(name + ".cfg", name.getBytes(StandardCharsets.UTF_8))));
    }
    shadowPackageManager.installPackageWithSplitApks(info, splits);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).hasLength(4);
    assertThat(installed.applicationInfo.splitSourceDirs).hasLength(4);
  }

  /**
   * Verifies that multiple density config splits can be handled (e.g., during development or
   * testing with bundletool's --connected-device mode which includes all densities).
   */
  @Test
  public void multipleDensitySplits_allTracked() throws Exception {
    String[] densities = {"base-ldpi", "base-mdpi", "base-hdpi", "base-xhdpi", "base-xxhdpi"};

    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = densities;

    Map<String, String> splits = new LinkedHashMap<>();
    for (String name : densities) {
      splits.put(
          name,
          ShadowPackageManager.createSplitApkWithAssets(
              name, Map.of(name + ".cfg", name.getBytes(StandardCharsets.UTF_8))));
    }
    shadowPackageManager.installPackageWithSplitApks(info, splits);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).hasLength(5);
  }

  /**
   * Simulates the complete initial Play Store install with config splits, install-time features,
   * and later on-demand feature additions — the full DynamicFeatures sample lifecycle.
   */
  @Test
  public void fullLifecycle_configSplitsAndFeatures() throws Exception {
    // Step 1: Play Store install (base + config splits + install-time feature)
    String[] initialSplits = {"base-xxhdpi", "base-arm64_v8a", "base-en", "initialInstall"};

    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = initialSplits;

    Map<String, String> splits = new LinkedHashMap<>();
    for (String name : initialSplits) {
      splits.put(
          name,
          ShadowPackageManager.createSplitApkWithAssets(
              name, Map.of(name + ".dat", name.getBytes(StandardCharsets.UTF_8))));
    }
    shadowPackageManager.installPackageWithSplitApks(info, splits);

    // Step 2: User requests on-demand features
    for (String feature : new String[] {"feature_kotlin", "feature_java", "feature_assets"}) {
      String path =
          ShadowPackageManager.createSplitApkWithAssets(
              feature, Map.of(feature + ".dat", feature.getBytes(StandardCharsets.UTF_8)));
      shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, feature, path);
    }

    // Step 3: User switches language to Polish
    String plPath =
        ShadowPackageManager.createSplitApkWithAssets(
            "base-pl", Map.of("pl.dat", "pl".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, "base-pl", plPath);

    // Verify final state: 8 splits total
    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).hasLength(8);
    assertThat(installed.applicationInfo.splitNames)
        .asList()
        .containsExactly(
            "base-xxhdpi",
            "base-arm64_v8a",
            "base-en",
            "initialInstall",
            "feature_kotlin",
            "feature_java",
            "feature_assets",
            "base-pl")
        .inOrder();
  }
}
