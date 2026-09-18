package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build.VERSION_CODES;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Tests for split APK resource and asset loading support.
 *
 * <p>These tests verify that split APK files created by {@link
 * ShadowPackageManager#createSplitApkWithAssets} and installed via {@link
 * ShadowPackageManager#installPackageWithSplitApks} produce valid APK files that the framework's
 * resource loading pipeline can open.
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.O)
public class ShadowPackageManagerSplitResourceTest {

  private PackageManager packageManager;

  @Before
  public void setUp() {
    packageManager = RuntimeEnvironment.getApplication().getPackageManager();
  }

  @Test
  public void setUpSplitApkStorage_createsValidZipFiles() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.splits.ziptest";

    shadowOf(packageManager).installPackageWithSplits(packageInfo, "config.xxhdpi", "config.en");

    PackageInfo retrieved = packageManager.getPackageInfo("com.example.splits.ziptest", 0);
    ApplicationInfo appInfo = retrieved.applicationInfo;

    // Each splitSourceDir should be a valid ZIP file (not a directory)
    for (String splitDir : appInfo.splitSourceDirs) {
      File file = new File(splitDir);
      assertThat(file.isFile()).isTrue();
      assertThat(file.isDirectory()).isFalse();
      // Should be openable as a ZIP
      try (ZipFile zip = new ZipFile(file)) {
        // Valid ZIP, may be empty
        assertThat(zip).isNotNull();
      }
    }
  }

  @Test
  public void createSplitApkWithAssets_createsValidZipWithAssetEntries() throws Exception {
    Map<String, byte[]> assets = new LinkedHashMap<>();
    assets.put("feature_data.json", "{\"key\": \"value\"}".getBytes(StandardCharsets.UTF_8));
    assets.put("images/logo.png", new byte[] {0x00, 0x01, 0x02, 0x03});

    String apkPath = ShadowPackageManager.createSplitApkWithAssets("feature_camera", assets);

    File apkFile = new File(apkPath);
    assertThat(apkFile.exists()).isTrue();
    assertThat(apkFile.isFile()).isTrue();

    // Verify ZIP contents
    try (ZipFile zip = new ZipFile(apkFile)) {
      ZipEntry jsonEntry = zip.getEntry("assets/feature_data.json");
      assertThat(jsonEntry).isNotNull();

      ZipEntry imageEntry = zip.getEntry("assets/images/logo.png");
      assertThat(imageEntry).isNotNull();

      // Verify content
      try (InputStream is = zip.getInputStream(jsonEntry)) {
        String content = new String(readAllBytes(is), StandardCharsets.UTF_8);
        assertThat(content).isEqualTo("{\"key\": \"value\"}");
      }
    }
  }

  @Test
  public void installPackageWithSplitApks_setsCorrectPaths() throws Exception {
    Map<String, byte[]> featureAssets = new LinkedHashMap<>();
    featureAssets.put("config.txt", "feature config".getBytes(StandardCharsets.UTF_8));
    String featureApk =
        ShadowPackageManager.createSplitApkWithAssets("feature_maps", featureAssets);

    Map<String, byte[]> densityAssets = new LinkedHashMap<>();
    densityAssets.put("res_placeholder.txt", "xxhdpi".getBytes(StandardCharsets.UTF_8));
    String densityApk =
        ShadowPackageManager.createSplitApkWithAssets("config.xxhdpi", densityAssets);

    Map<String, String> splitPaths = new LinkedHashMap<>();
    splitPaths.put("feature_maps", featureApk);
    splitPaths.put("config.xxhdpi", densityApk);

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.splits.apkpaths";
    shadowOf(packageManager).installPackageWithSplitApks(packageInfo, splitPaths);

    PackageInfo retrieved = packageManager.getPackageInfo("com.example.splits.apkpaths", 0);
    ApplicationInfo appInfo = retrieved.applicationInfo;

    assertThat(appInfo.splitNames).asList().containsExactly("feature_maps", "config.xxhdpi");
    assertThat(appInfo.splitSourceDirs).hasLength(2);
    assertThat(appInfo.splitPublicSourceDirs).hasLength(2);

    // Paths should be the actual APK file paths, not temp directories
    for (String path : appInfo.splitSourceDirs) {
      assertThat(new File(path).isFile()).isTrue();
    }
  }

  @Test
  public void installPackageWithSplitApks_preservesCustomPaths() throws Exception {
    String customPath =
        ShadowPackageManager.createSplitApkWithAssets("custom_split", new LinkedHashMap<>());

    Map<String, String> splitPaths = new LinkedHashMap<>();
    splitPaths.put("custom_split", customPath);

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.splits.custom";
    shadowOf(packageManager).installPackageWithSplitApks(packageInfo, splitPaths);

    PackageInfo retrieved = packageManager.getPackageInfo("com.example.splits.custom", 0);
    // The custom path should be preserved exactly
    assertThat(retrieved.applicationInfo.splitSourceDirs[0]).isEqualTo(customPath);
  }

  @Test
  public void addSplitAssetPath_loadsAssetsFromSplitApk() throws Exception {
    // Create a split APK with an asset
    Map<String, byte[]> assets = new LinkedHashMap<>();
    assets.put("split_data.txt", "hello from split".getBytes(StandardCharsets.UTF_8));
    String splitApkPath = ShadowPackageManager.createSplitApkWithAssets("dynamic_feature", assets);

    // Add the split APK to the app's AssetManager
    AssetManager assetManager = RuntimeEnvironment.getApplication().getAssets();
    int cookie = ShadowAssetManager.addSplitAssetPath(assetManager, splitApkPath);
    assertThat(cookie).isGreaterThan(0);

    // Verify the asset is now accessible
    try (InputStream is = assetManager.open("split_data.txt")) {
      String content = new String(readAllBytes(is), StandardCharsets.UTF_8);
      assertThat(content).isEqualTo("hello from split");
    }
  }

  @Test
  public void addSplitAssetPath_multipleAssets_allAccessible() throws Exception {
    Map<String, byte[]> assets = new LinkedHashMap<>();
    assets.put("data/config.json", "{\"version\": 1}".getBytes(StandardCharsets.UTF_8));
    assets.put("data/strings.txt", "hello world".getBytes(StandardCharsets.UTF_8));
    assets.put("images/icon.bin", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47});
    String splitApkPath = ShadowPackageManager.createSplitApkWithAssets("feature_module", assets);

    AssetManager assetManager = RuntimeEnvironment.getApplication().getAssets();
    ShadowAssetManager.addSplitAssetPath(assetManager, splitApkPath);

    try (InputStream is = assetManager.open("data/config.json")) {
      String content = new String(readAllBytes(is), StandardCharsets.UTF_8);
      assertThat(content).isEqualTo("{\"version\": 1}");
    }
    try (InputStream is = assetManager.open("data/strings.txt")) {
      String content = new String(readAllBytes(is), StandardCharsets.UTF_8);
      assertThat(content).isEqualTo("hello world");
    }
    try (InputStream is = assetManager.open("images/icon.bin")) {
      byte[] bytes = readAllBytes(is);
      assertThat(bytes).isEqualTo(new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47});
    }
  }

  @Test
  public void addSplitAssetPath_multipleSplits_allAccessible() throws Exception {
    Map<String, byte[]> split1Assets = new LinkedHashMap<>();
    split1Assets.put("split1.txt", "from split 1".getBytes(StandardCharsets.UTF_8));
    String split1Path = ShadowPackageManager.createSplitApkWithAssets("split_one", split1Assets);

    Map<String, byte[]> split2Assets = new LinkedHashMap<>();
    split2Assets.put("split2.txt", "from split 2".getBytes(StandardCharsets.UTF_8));
    String split2Path = ShadowPackageManager.createSplitApkWithAssets("split_two", split2Assets);

    AssetManager assetManager = RuntimeEnvironment.getApplication().getAssets();
    ShadowAssetManager.addSplitAssetPath(assetManager, split1Path);
    ShadowAssetManager.addSplitAssetPath(assetManager, split2Path);

    try (InputStream is = assetManager.open("split1.txt")) {
      assertThat(new String(readAllBytes(is), StandardCharsets.UTF_8)).isEqualTo("from split 1");
    }
    try (InputStream is = assetManager.open("split2.txt")) {
      assertThat(new String(readAllBytes(is), StandardCharsets.UTF_8)).isEqualTo("from split 2");
    }
  }

  @Test
  public void emptySplitApk_doesNotCauseErrors() throws Exception {
    // Empty splits (no assets, no resources) should be valid and cause no errors
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.splits.empty";

    shadowOf(packageManager)
        .installPackageWithSplits(packageInfo, "config.xxhdpi", "config.arm64_v8a");

    PackageInfo retrieved = packageManager.getPackageInfo("com.example.splits.empty", 0);
    assertThat(retrieved.applicationInfo.splitNames).hasLength(2);
    assertThat(retrieved.applicationInfo.splitSourceDirs).hasLength(2);

    // All split paths should be valid ZIP files
    for (String dir : retrieved.applicationInfo.splitSourceDirs) {
      try (ZipFile zip = new ZipFile(new File(dir))) {
        assertThat(zip).isNotNull();
      }
    }
  }

  @Test
  public void installPackageWithSplitApks_requiresApiO() {
    // This test verifies the API guard, actual API level check is done at runtime
    // We're already on O+ due to @Config, so just verify it doesn't throw
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.splits.apiguard";
    Map<String, String> splitPaths = new LinkedHashMap<>();
    splitPaths.put(
        "test_split",
        ShadowPackageManager.createSplitApkWithAssets("test_split", new LinkedHashMap<>()));
    shadowOf(packageManager).installPackageWithSplitApks(packageInfo, splitPaths);

    // Should succeed without exception on O+
    assertThat(packageInfo.applicationInfo.splitNames).asList().containsExactly("test_split");
  }

  private static byte[] readAllBytes(InputStream is) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte[] data = new byte[1024];
    int bytesRead;
    while ((bytesRead = is.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, bytesRead);
    }
    return buffer.toByteArray();
  }
}
