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
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAssetManager;
import org.robolectric.shadows.ShadowPackageManager;

/**
 * Tests modeled after the DynamicFeatures sample's asset-only feature module.
 *
 * <p>In the official sample, the "assets" feature module (features/assets/) has:
 *
 * <ul>
 *   <li>{@code android:hasCode="false"} in its manifest - no DEX code, just assets
 *   <li>On-demand delivery: {@code <dist:on-demand />}
 *   <li>A single asset file: {@code assets/assets.txt}
 *   <li>After install, MainActivity.displayAssets() reads the file via context.assets.open()
 * </ul>
 *
 * <p>This test verifies that Robolectric can correctly handle asset-only split modules, including
 * creating APKs with just assets (no resources.arsc), loading them, and reading content.
 *
 * <p>Reference: {@code DynamicFeatures/features/assets/}
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.O)
public class AssetOnlyModuleTest {

  private static final String PACKAGE_NAME = "com.google.android.samples.dynamicfeatures";

  private PackageManager packageManager;
  private ShadowPackageManager shadowPackageManager;

  @Before
  public void setUp() {
    packageManager = RuntimeEnvironment.getApplication().getPackageManager();
    shadowPackageManager = shadowOf(packageManager);

    // Install base app
    PackageInfo baseApp = new PackageInfo();
    baseApp.packageName = PACKAGE_NAME;
    baseApp.applicationInfo = new ApplicationInfo();
    baseApp.applicationInfo.packageName = PACKAGE_NAME;
    shadowPackageManager.installPackage(baseApp);
  }

  /**
   * Reproduces the exact asset content from the official sample's features/assets/assets/assets.txt
   * and verifies it can be read after installing the asset-only module.
   */
  @Test
  public void assetsModule_officialSampleContent_readable() throws Exception {
    // Exact content from the official sample's assets.txt
    String officialContent =
        "This text originates from a dynamically loaded feature.\n"
            + "The source can be found in features/assets/assets/assets.txt.";

    String splitPath =
        ShadowPackageManager.createSplitApkWithAssets(
            "feature_assets",
            Map.of("assets.txt", officialContent.getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, "feature_assets", splitPath);

    // Read via AssetManager (mirroring context.assets.open("assets.txt") in the sample)
    AssetManager am = AssetManager.class.getDeclaredConstructor().newInstance();
    ShadowAssetManager.addSplitAssetPath(am, splitPath);

    try (InputStream is = am.open("assets.txt")) {
      String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(content).isEqualTo(officialContent);
    }
  }

  /**
   * Verifies that an asset-only module (hasCode=false) shows up in splitNames but doesn't need any
   * DEX or resources.arsc to work correctly.
   */
  @Test
  public void assetsModule_hasCodeFalse_splitRegistered() throws Exception {
    String splitPath =
        ShadowPackageManager.createSplitApkWithAssets(
            "feature_assets", Map.of("assets.txt", "content".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, "feature_assets", splitPath);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).asList().contains("feature_assets");
  }

  /**
   * Verifies multiple asset files within an asset-only module, simulating a module that contains
   * multiple data files (e.g., ML models, configuration files).
   */
  @Test
  public void assetsModule_multipleAssetFiles_allAccessible() throws Exception {
    Map<String, byte[]> assets = new LinkedHashMap<>();
    assets.put("assets.txt", "Main asset file".getBytes(StandardCharsets.UTF_8));
    assets.put("data/model.bin", new byte[] {0x01, 0x02, 0x03, 0x04});
    assets.put("config/settings.json", "{\"enabled\":true}".getBytes(StandardCharsets.UTF_8));

    String splitPath = ShadowPackageManager.createSplitApkWithAssets("feature_assets", assets);
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, "feature_assets", splitPath);

    AssetManager am = AssetManager.class.getDeclaredConstructor().newInstance();
    ShadowAssetManager.addSplitAssetPath(am, splitPath);

    try (InputStream is = am.open("assets.txt")) {
      assertThat(new String(is.readAllBytes(), StandardCharsets.UTF_8))
          .isEqualTo("Main asset file");
    }
    try (InputStream is = am.open("data/model.bin")) {
      assertThat(is.readAllBytes()).hasLength(4);
    }
    try (InputStream is = am.open("config/settings.json")) {
      assertThat(new String(is.readAllBytes(), StandardCharsets.UTF_8)).contains("enabled");
    }
  }

  /**
   * Verifies that assets from different split modules are isolated and accessible independently.
   * The official sample has multiple feature modules, each potentially with its own assets.
   */
  @Test
  public void multipleModules_assetsIsolatedPerSplit() throws Exception {
    // Asset-only module
    String assetSplitPath =
        ShadowPackageManager.createSplitApkWithAssets(
            "feature_assets",
            Map.of("assets.txt", "from assets module".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, "feature_assets", assetSplitPath);

    // Kotlin feature module with its own assets
    String kotlinSplitPath =
        ShadowPackageManager.createSplitApkWithAssets(
            "feature_kotlin",
            Map.of("kotlin_data.txt", "from kotlin module".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(
        PACKAGE_NAME, "feature_kotlin", kotlinSplitPath);

    // Each split's assets are accessible via its own path
    AssetManager am1 = AssetManager.class.getDeclaredConstructor().newInstance();
    ShadowAssetManager.addSplitAssetPath(am1, assetSplitPath);
    try (InputStream is = am1.open("assets.txt")) {
      assertThat(new String(is.readAllBytes(), StandardCharsets.UTF_8))
          .isEqualTo("from assets module");
    }

    AssetManager am2 = AssetManager.class.getDeclaredConstructor().newInstance();
    ShadowAssetManager.addSplitAssetPath(am2, kotlinSplitPath);
    try (InputStream is = am2.open("kotlin_data.txt")) {
      assertThat(new String(is.readAllBytes(), StandardCharsets.UTF_8))
          .isEqualTo("from kotlin module");
    }
  }
}
