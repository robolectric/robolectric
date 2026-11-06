package org.robolectric.integrationtests.splitapk;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build.VERSION_CODES;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.BundletoolSplitApkLoader;
import org.robolectric.shadows.ShadowAssetManager;
import org.robolectric.shadows.ShadowPackageManager;

/**
 * Tests for bundletool integration, verifying that Robolectric can load and install split APKs from
 * bundletool-generated archives and directories.
 *
 * <p>Bundletool (https://developer.android.com/tools/bundletool) is the tool that processes AAB
 * files to generate device-specific APK sets. The DynamicFeatures sample's AAB would be processed
 * by bundletool to produce a {@code .apks} archive with the following structure:
 *
 * <pre>
 * DynamicFeatures.apks
 * └── splits/
 *     ├── base-master.apk          (base module)
 *     ├── base-xxhdpi.apk          (density config)
 *     ├── base-arm64_v8a.apk       (ABI config)
 *     ├── base-en.apk              (language config)
 *     ├── initialInstall-master.apk (install-time feature)
 *     ├── feature_kotlin-master.apk (on-demand feature)
 *     ├── feature_java-master.apk   (on-demand feature)
 *     └── feature_assets-master.apk (asset-only feature)
 * </pre>
 *
 * <p>This test class verifies the complete workflow from bundletool output to installed package.
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.O)
public class BundletoolWorkflowTest {

  private static final String PACKAGE_NAME = "com.google.android.samples.dynamicfeatures";

  private PackageManager packageManager;
  private ShadowPackageManager shadowPackageManager;

  @Before
  public void setUp() {
    packageManager = RuntimeEnvironment.getApplication().getPackageManager();
    shadowPackageManager = shadowOf(packageManager);
  }

  /**
   * Simulates running {@code bundletool build-apks} on the DynamicFeatures AAB and then installing
   * via {@code bundletool install-apks}. The .apks archive contains splits for the device.
   */
  @Test
  public void bundletoolBuildApks_installFromArchive() throws Exception {
    // Create split APKs mimicking bundletool output for DynamicFeatures
    Map<String, String> apkPaths = new LinkedHashMap<>();
    apkPaths.put(
        "base-master",
        ShadowPackageManager.createSplitApkWithAssets(
            "base-master",
            Map.of("base_manifest.dat", "base module".getBytes(StandardCharsets.UTF_8))));
    apkPaths.put(
        "base-xxhdpi",
        ShadowPackageManager.createSplitApkWithAssets(
            "base-xxhdpi", Map.of("res/drawable-xxhdpi/icon.png", new byte[] {(byte) 0x89, 0x50})));
    apkPaths.put(
        "base-en",
        ShadowPackageManager.createSplitApkWithAssets(
            "base-en",
            Map.of(
                "res/values-en/strings.dat", "English strings".getBytes(StandardCharsets.UTF_8))));
    apkPaths.put(
        "initialInstall-master",
        ShadowPackageManager.createSplitApkWithAssets(
            "initialInstall-master",
            Map.of("initial.dat", "initial install".getBytes(StandardCharsets.UTF_8))));

    // Create .apks archive
    Path apksArchive = BundletoolSplitApkLoader.createApksArchive(apkPaths);
    assertThat(Files.exists(apksArchive)).isTrue();

    // Load and install
    Map<String, String> loaded = BundletoolSplitApkLoader.loadFromApksArchive(apksArchive);
    assertThat(loaded).hasSize(4);

    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = loaded.keySet().toArray(new String[0]);
    shadowPackageManager.installPackageWithSplitApks(info, loaded);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).hasLength(4);
    assertThat(installed.applicationInfo.splitSourceDirs).hasLength(4);
  }

  /**
   * Simulates loading split APKs from a directory, as produced by {@code bundletool build-apks
   * --output-format=DIRECTORY}. This is useful for CI/CD pipelines and local testing.
   */
  @Test
  public void bundletoolOutputDirectory_loadAndInstall() throws Exception {
    Path splitDir = Files.createTempDirectory("bundletool-splits");
    try {
      // Create splits matching DynamicFeatures bundletool output
      for (String splitName :
          new String[] {"base-master", "base-xxhdpi", "feature_kotlin-master"}) {
        String apk =
            ShadowPackageManager.createSplitApkWithAssets(
                splitName, Map.of(splitName + ".dat", splitName.getBytes(StandardCharsets.UTF_8)));
        Files.copy(Path.of(apk), splitDir.resolve(splitName + ".apk"));
      }

      Map<String, String> loaded = BundletoolSplitApkLoader.loadFromDirectory(splitDir);
      assertThat(loaded).hasSize(3);

      PackageInfo info = new PackageInfo();
      info.packageName = PACKAGE_NAME;
      info.applicationInfo = new ApplicationInfo();
      info.applicationInfo.packageName = PACKAGE_NAME;
      info.applicationInfo.splitNames = loaded.keySet().toArray(new String[0]);
      shadowPackageManager.installPackageWithSplitApks(info, loaded);

      PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
      assertThat(installed.applicationInfo.splitNames).hasLength(3);
    } finally {
      try (var stream = Files.list(splitDir)) {
        stream.forEach(
            p -> {
              try {
                Files.delete(p);
              } catch (Exception ignored) {
              }
            });
      }
      Files.delete(splitDir);
    }
  }

  /**
   * Simulates filtering bundletool output to load only base module splits (excluding feature
   * modules). This matches the scenario of testing the base app without any dynamic features.
   */
  @Test
  public void bundletoolPrefixFilter_baseModuleOnly() throws Exception {
    Map<String, String> allApks = new LinkedHashMap<>();
    for (String name :
        new String[] {
          "base-master",
          "base-xxhdpi",
          "base-en",
          "feature_kotlin-master",
          "feature_java-master",
          "feature_assets-master"
        }) {
      allApks.put(
          name,
          ShadowPackageManager.createSplitApkWithAssets(
              name, Map.of(name + ".dat", name.getBytes(StandardCharsets.UTF_8))));
    }

    Path archive = BundletoolSplitApkLoader.createApksArchive(allApks);

    // Load only base splits
    Map<String, String> baseSplits = BundletoolSplitApkLoader.loadFromApksArchive(archive, "base-");
    assertThat(baseSplits).hasSize(3);
    assertThat(baseSplits).containsKey("base-master");
    assertThat(baseSplits).containsKey("base-xxhdpi");
    assertThat(baseSplits).containsKey("base-en");
    assertThat(baseSplits).doesNotContainKey("feature_kotlin-master");
  }

  /**
   * End-to-end test: build .apks archive, load specific module, install base + feature, verify
   * assets are accessible. This mirrors the complete bundletool workflow for local testing.
   */
  @Test
  public void endToEnd_bundletoolArchiveToAssetAccess() throws Exception {
    // Create the full DynamicFeatures .apks archive
    Map<String, String> allApks = new LinkedHashMap<>();
    allApks.put(
        "base-master",
        ShadowPackageManager.createSplitApkWithAssets(
            "base-master", Map.of("app.dat", "base app".getBytes(StandardCharsets.UTF_8))));
    allApks.put(
        "feature_assets-master",
        ShadowPackageManager.createSplitApkWithAssets(
            "feature_assets-master",
            Map.of(
                "assets.txt",
                "This text originates from a dynamically loaded feature."
                    .getBytes(StandardCharsets.UTF_8))));

    Path archive = BundletoolSplitApkLoader.createApksArchive(allApks);

    // Load all splits and install
    Map<String, String> loaded = BundletoolSplitApkLoader.loadFromApksArchive(archive);

    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = loaded.keySet().toArray(new String[0]);
    shadowPackageManager.installPackageWithSplitApks(info, loaded);

    // Verify asset from the feature module is accessible
    String featurePath = loaded.get("feature_assets-master");
    AssetManager am = AssetManager.class.getDeclaredConstructor().newInstance();
    ShadowAssetManager.addSplitAssetPath(am, featurePath);

    try (InputStream is = am.open("assets.txt")) {
      String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(content).contains("dynamically loaded feature");
    }
  }

  /**
   * Verifies that bundletool's naming convention for config splits (module-config.apk) is correctly
   * handled. Each split name preserves the full bundletool filename.
   */
  @Test
  public void bundletoolNamingConvention_preservedInSplitNames() throws Exception {
    Map<String, String> apks = new LinkedHashMap<>();
    // Bundletool naming: module-dimension_value.apk
    for (String name :
        new String[] {
          "base-master",
          "base-xxhdpi",
          "base-arm64_v8a",
          "base-en",
          "feature_kotlin-master",
          "feature_kotlin-xxhdpi"
        }) {
      apks.put(
          name,
          ShadowPackageManager.createSplitApkWithAssets(
              name, Map.of("x.dat", "x".getBytes(StandardCharsets.UTF_8))));
    }

    Path archive = BundletoolSplitApkLoader.createApksArchive(apks);
    Map<String, String> loaded = BundletoolSplitApkLoader.loadFromApksArchive(archive);

    // All bundletool names should be preserved
    assertThat(loaded.keySet())
        .containsExactly(
            "base-master",
            "base-xxhdpi",
            "base-arm64_v8a",
            "base-en",
            "feature_kotlin-master",
            "feature_kotlin-xxhdpi");
  }
}
