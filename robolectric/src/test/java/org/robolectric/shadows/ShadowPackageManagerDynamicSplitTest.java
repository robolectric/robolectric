package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import androidx.test.core.app.ApplicationProvider;
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
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

/**
 * Tests for dynamic split installation, compiled resource support, and bundletool integration
 * features in {@link ShadowPackageManager}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = android.os.Build.VERSION_CODES.O)
public class ShadowPackageManagerDynamicSplitTest {

  private PackageManager packageManager;
  private ShadowPackageManager shadowPackageManager;

  @Before
  public void setUp() {
    packageManager = ApplicationProvider.getApplicationContext().getPackageManager();
    shadowPackageManager = Shadows.shadowOf(packageManager);
  }

  // === Dynamic Split Installation Tests ===

  @Test
  public void addSplitToInstalledPackage_addsFirstSplit() throws Exception {
    // Install a package with no splits
    PackageInfo info = new PackageInfo();
    info.packageName = "com.example.dynamic";
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = "com.example.dynamic";
    shadowPackageManager.installPackage(info);

    // Create a split APK
    Map<String, byte[]> assets = new LinkedHashMap<>();
    assets.put("feature.txt", "dynamic content".getBytes(StandardCharsets.UTF_8));
    String splitPath = ShadowPackageManager.createSplitApkWithAssets("feature_one", assets);

    // Add the split dynamically
    shadowPackageManager.addSplitToInstalledPackage(
        "com.example.dynamic", "feature_one", splitPath);

    // Verify the split was added
    PackageInfo updated = packageManager.getPackageInfo("com.example.dynamic", 0);
    assertThat(updated.applicationInfo.splitNames).asList().containsExactly("feature_one");
    assertThat(updated.applicationInfo.splitSourceDirs).hasLength(1);
    assertThat(updated.applicationInfo.splitSourceDirs[0]).isEqualTo(splitPath);
    assertThat(updated.applicationInfo.splitPublicSourceDirs)
        .isEqualTo(updated.applicationInfo.splitSourceDirs);
    assertThat(updated.splitNames).asList().containsExactly("feature_one");
  }

  @Test
  public void addSplitToInstalledPackage_addsMultipleSplitsIncrementally() throws Exception {
    PackageInfo info = new PackageInfo();
    info.packageName = "com.example.multi";
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = "com.example.multi";
    shadowPackageManager.installPackage(info);

    String split1 =
        ShadowPackageManager.createSplitApkWithAssets(
            "split_a", Map.of("a.txt", "A".getBytes(StandardCharsets.UTF_8)));
    String split2 =
        ShadowPackageManager.createSplitApkWithAssets(
            "split_b", Map.of("b.txt", "B".getBytes(StandardCharsets.UTF_8)));

    shadowPackageManager.addSplitToInstalledPackage("com.example.multi", "split_a", split1);
    shadowPackageManager.addSplitToInstalledPackage("com.example.multi", "split_b", split2);

    PackageInfo updated = packageManager.getPackageInfo("com.example.multi", 0);
    assertThat(updated.applicationInfo.splitNames)
        .asList()
        .containsExactly("split_a", "split_b")
        .inOrder();
    assertThat(updated.applicationInfo.splitSourceDirs).hasLength(2);
  }

  @Test
  public void addSplitToInstalledPackage_throwsForUninstalledPackage() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            shadowPackageManager.addSplitToInstalledPackage(
                "com.example.nonexistent", "split", "/fake/path.apk"));
  }

  @Test
  public void addSplitToInstalledPackage_preservesExistingSplits() throws Exception {
    // Install with pre-existing splits
    PackageInfo info = new PackageInfo();
    info.packageName = "com.example.presplit";
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = "com.example.presplit";
    info.applicationInfo.splitNames = new String[] {"existing_split"};

    Map<String, String> existingSplits = new LinkedHashMap<>();
    String existingPath =
        ShadowPackageManager.createSplitApkWithAssets(
            "existing_split", Map.of("old.txt", "old".getBytes(StandardCharsets.UTF_8)));
    existingSplits.put("existing_split", existingPath);
    shadowPackageManager.installPackageWithSplitApks(info, existingSplits);

    // Add a new split
    String newPath =
        ShadowPackageManager.createSplitApkWithAssets(
            "new_split", Map.of("new.txt", "new".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage("com.example.presplit", "new_split", newPath);

    PackageInfo updated = packageManager.getPackageInfo("com.example.presplit", 0);
    assertThat(updated.applicationInfo.splitNames)
        .asList()
        .containsExactly("existing_split", "new_split")
        .inOrder();
    assertThat(updated.applicationInfo.splitSourceDirs).hasLength(2);
  }

  // === Compiled Resource Support Tests ===

  @Test
  public void createSplitApkWithResources_createsValidApk() throws Exception {
    Map<String, String> resources = new LinkedHashMap<>();
    resources.put("app_name", "My Feature");
    resources.put("greeting", "Hello World");

    String apkPath =
        ShadowPackageManager.createSplitApkWithResources(
            "feature_res", "com.example.feature", 0x7f, resources, null);

    // Verify file exists and is a valid ZIP
    Path path = Path.of(apkPath);
    assertThat(Files.exists(path)).isTrue();
    assertThat(Files.size(path)).isGreaterThan(0);

    // Verify it contains resources.arsc
    try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(path.toFile())) {
      assertThat(zf.getEntry("resources.arsc")).isNotNull();
    }
  }

  @Test
  public void createSplitApkWithResources_includesAssets() throws Exception {
    Map<String, String> resources = new LinkedHashMap<>();
    resources.put("label", "Test");

    Map<String, byte[]> assets = new LinkedHashMap<>();
    assets.put("config.json", "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8));

    String apkPath =
        ShadowPackageManager.createSplitApkWithResources(
            "feature_both", "com.example.feature", 0x7f, resources, assets);

    try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(Path.of(apkPath).toFile())) {
      assertThat(zf.getEntry("resources.arsc")).isNotNull();
      assertThat(zf.getEntry("assets/config.json")).isNotNull();

      // Verify asset content
      try (InputStream is = zf.getInputStream(zf.getEntry("assets/config.json"))) {
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(content).isEqualTo("{\"key\":\"value\"}");
      }
    }
  }

  @Test
  public void createSplitApkWithResources_resourcesArscIsStored() throws Exception {
    Map<String, String> resources = new LinkedHashMap<>();
    resources.put("test", "value");

    String apkPath =
        ShadowPackageManager.createSplitApkWithResources(
            "stored_check", "com.example.app", 0x7f, resources, null);

    try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(Path.of(apkPath).toFile())) {
      java.util.zip.ZipEntry arscEntry = zf.getEntry("resources.arsc");
      // STORED method means size == compressedSize
      assertThat(arscEntry.getSize()).isEqualTo(arscEntry.getCompressedSize());
    }
  }

  // === ArscResourceTableBuilder Tests ===

  @Test
  public void arscBuilder_createsNonEmptyTable() {
    Map<String, String> strings = new LinkedHashMap<>();
    strings.put("hello", "Hello World");
    strings.put("bye", "Goodbye");

    byte[] arsc =
        ArscResourceTableBuilder.buildStringResourceTable("com.example.test", 0x7f, strings);

    assertThat(arsc).isNotNull();
    assertThat(arsc.length).isGreaterThan(12); // At least header size
    // Verify magic: first 2 bytes should be RES_TABLE_TYPE (0x0002) in little-endian
    assertThat(arsc[0]).isEqualTo((byte) 0x02);
    assertThat(arsc[1]).isEqualTo((byte) 0x00);
  }

  @Test
  public void arscBuilder_singleStringResource() {
    Map<String, String> strings = new LinkedHashMap<>();
    strings.put("single", "Only One");

    byte[] arsc =
        ArscResourceTableBuilder.buildStringResourceTable("com.example.single", 0x7f, strings);

    assertThat(arsc).isNotNull();
    // Header size is 12, so total must be larger
    assertThat(arsc.length).isGreaterThan(12);
  }

  // === Bundletool Integration Tests ===

  @Test
  public void bundletoolLoader_loadFromDirectory() throws Exception {
    // Create a temp directory with split APKs
    Path tempDir = Files.createTempDirectory("bundletool-test");
    try {
      // Create some split APK files
      String split1 =
          ShadowPackageManager.createSplitApkWithAssets(
              "base-master", Map.of("base.txt", "base".getBytes(StandardCharsets.UTF_8)));
      String split2 =
          ShadowPackageManager.createSplitApkWithAssets(
              "base-xxhdpi", Map.of("density.txt", "xxhdpi".getBytes(StandardCharsets.UTF_8)));

      Files.copy(Path.of(split1), tempDir.resolve("base-master.apk"));
      Files.copy(Path.of(split2), tempDir.resolve("base-xxhdpi.apk"));

      Map<String, String> splits = BundletoolSplitApkLoader.loadFromDirectory(tempDir);

      assertThat(splits).hasSize(2);
      assertThat(splits).containsKey("base-master");
      assertThat(splits).containsKey("base-xxhdpi");
      // Verify paths are absolute
      assertThat(splits.get("base-master")).startsWith("/");
    } finally {
      // Cleanup
      Files.list(tempDir)
          .forEach(
              p -> {
                try {
                  Files.delete(p);
                } catch (Exception e) {
                }
              });
      Files.delete(tempDir);
    }
  }

  @Test
  public void bundletoolLoader_loadFromDirectoryWithPrefix() throws Exception {
    Path tempDir = Files.createTempDirectory("bundletool-prefix");
    try {
      String split1 =
          ShadowPackageManager.createSplitApkWithAssets(
              "base-master", Map.of("x.txt", "x".getBytes(StandardCharsets.UTF_8)));
      String split2 =
          ShadowPackageManager.createSplitApkWithAssets(
              "feature-camera", Map.of("y.txt", "y".getBytes(StandardCharsets.UTF_8)));

      Files.copy(Path.of(split1), tempDir.resolve("base-master.apk"));
      Files.copy(Path.of(split2), tempDir.resolve("feature-camera.apk"));

      Map<String, String> splits = BundletoolSplitApkLoader.loadFromDirectory(tempDir, "base-");

      assertThat(splits).hasSize(1);
      assertThat(splits).containsKey("base-master");
      assertThat(splits).doesNotContainKey("feature-camera");
    } finally {
      Files.list(tempDir)
          .forEach(
              p -> {
                try {
                  Files.delete(p);
                } catch (Exception e) {
                }
              });
      Files.delete(tempDir);
    }
  }

  @Test
  public void bundletoolLoader_loadFromApksArchive() throws Exception {
    // Create split APK files
    String baseSplit =
        ShadowPackageManager.createSplitApkWithAssets(
            "base-master", Map.of("app.txt", "base content".getBytes(StandardCharsets.UTF_8)));
    String densitySplit =
        ShadowPackageManager.createSplitApkWithAssets(
            "base-xxhdpi", Map.of("res.txt", "density content".getBytes(StandardCharsets.UTF_8)));

    // Create a .apks archive from them
    Map<String, String> splitPaths = new LinkedHashMap<>();
    splitPaths.put("base-master", baseSplit);
    splitPaths.put("base-xxhdpi", densitySplit);
    Path apksArchive = BundletoolSplitApkLoader.createApksArchive(splitPaths);

    // Load from the archive
    Map<String, String> loaded = BundletoolSplitApkLoader.loadFromApksArchive(apksArchive);

    assertThat(loaded).hasSize(2);
    assertThat(loaded).containsKey("base-master");
    assertThat(loaded).containsKey("base-xxhdpi");

    // Verify extracted files are valid ZIP files that can be read
    for (String path : loaded.values()) {
      assertThat(Files.exists(Path.of(path))).isTrue();
      assertThat(Files.size(Path.of(path))).isGreaterThan(0);
    }
  }

  @Test
  public void bundletoolLoader_loadFromApksArchiveWithPrefix() throws Exception {
    String baseSplit =
        ShadowPackageManager.createSplitApkWithAssets(
            "base-master", Map.of("a.txt", "a".getBytes(StandardCharsets.UTF_8)));
    String featureSplit =
        ShadowPackageManager.createSplitApkWithAssets(
            "feature-camera", Map.of("b.txt", "b".getBytes(StandardCharsets.UTF_8)));

    Map<String, String> splitPaths = new LinkedHashMap<>();
    splitPaths.put("base-master", baseSplit);
    splitPaths.put("feature-camera", featureSplit);
    Path apksArchive = BundletoolSplitApkLoader.createApksArchive(splitPaths);

    Map<String, String> loaded =
        BundletoolSplitApkLoader.loadFromApksArchive(apksArchive, "feature-");

    assertThat(loaded).hasSize(1);
    assertThat(loaded).containsKey("feature-camera");
  }

  @Test
  public void bundletoolLoader_endToEndInstallFromArchive() throws Exception {
    // Create split APK files with assets
    String baseSplit =
        ShadowPackageManager.createSplitApkWithAssets(
            "base-master", Map.of("base_asset.txt", "base data".getBytes(StandardCharsets.UTF_8)));
    String featureSplit =
        ShadowPackageManager.createSplitApkWithAssets(
            "feature-maps", Map.of("map_data.txt", "map data".getBytes(StandardCharsets.UTF_8)));

    // Create archive
    Map<String, String> splitPaths = new LinkedHashMap<>();
    splitPaths.put("base-master", baseSplit);
    splitPaths.put("feature-maps", featureSplit);
    Path apksArchive = BundletoolSplitApkLoader.createApksArchive(splitPaths);

    // Load and install
    Map<String, String> loaded = BundletoolSplitApkLoader.loadFromApksArchive(apksArchive);

    PackageInfo info = new PackageInfo();
    info.packageName = "com.example.bundletool";
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = "com.example.bundletool";
    info.applicationInfo.splitNames = loaded.keySet().toArray(new String[0]);
    shadowPackageManager.installPackageWithSplitApks(info, loaded);

    // Verify installation
    PackageInfo installed = packageManager.getPackageInfo("com.example.bundletool", 0);
    assertThat(installed.applicationInfo.splitNames)
        .asList()
        .containsExactly("base-master", "feature-maps");
    assertThat(installed.applicationInfo.splitSourceDirs).hasLength(2);
  }

  // === End-to-end: Dynamic split with resources ===

  @Test
  public void endToEnd_dynamicSplitWithAssetsAccessible() throws Exception {
    // Install base package
    PackageInfo info = new PackageInfo();
    info.packageName = "com.example.e2e";
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = "com.example.e2e";
    shadowPackageManager.installPackage(info);

    // Create and add a dynamic split with an asset
    Map<String, byte[]> assets = new LinkedHashMap<>();
    assets.put("feature_data.json", "{\"feature\":\"camera\"}".getBytes(StandardCharsets.UTF_8));
    String splitPath = ShadowPackageManager.createSplitApkWithAssets("feature_camera", assets);
    shadowPackageManager.addSplitToInstalledPackage("com.example.e2e", "feature_camera", splitPath);

    // Load the split's assets via AssetManager
    AssetManager assetManager = AssetManager.class.getDeclaredConstructor().newInstance();
    ShadowAssetManager.addSplitAssetPath(assetManager, splitPath);

    try (InputStream is = assetManager.open("feature_data.json")) {
      String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(content).isEqualTo("{\"feature\":\"camera\"}");
    }
  }
}
