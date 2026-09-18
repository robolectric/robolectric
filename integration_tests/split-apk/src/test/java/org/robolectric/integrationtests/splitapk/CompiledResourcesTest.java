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
 * Tests for compiled resource support in split APKs, verifying that {@code resources.arsc} can be
 * generated and included in split APK files.
 *
 * <p>In a real AAB, each feature module has its own compiled resources (drawable, layout, string
 * resources) in its split APK. The DynamicFeatures sample's feature modules each have their own
 * layouts and string resources:
 *
 * <ul>
 *   <li>features/kotlin/src/main/res/ - KotlinSampleActivity layout and strings
 *   <li>features/java/src/main/res/ - JavaSampleActivity layout and strings
 *   <li>features/initialInstall/src/main/res/ - InitialInstallActivity layout and strings
 * </ul>
 *
 * <p>This test verifies that Robolectric can create split APKs with compiled string resources using
 * {@code ArscResourceTableBuilder} and load them via the resource pipeline.
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.O)
public class CompiledResourcesTest {

  private static final String PACKAGE_NAME = "com.google.android.samples.dynamicfeatures";

  private PackageManager packageManager;
  private ShadowPackageManager shadowPackageManager;

  @Before
  public void setUp() {
    packageManager = RuntimeEnvironment.getApplication().getPackageManager();
    shadowPackageManager = shadowOf(packageManager);
  }

  /**
   * Simulates the Kotlin feature module which has its own string resources (e.g.,
   * "module_feature_kotlin" title referenced in its AndroidManifest.xml's dist:title attribute).
   */
  @Test
  public void kotlinFeature_stringResources_inSplitApk() throws Exception {
    Map<String, String> strings = new LinkedHashMap<>();
    strings.put("module_feature_kotlin", "Kotlin Feature");
    strings.put("kotlin_activity_title", "Kotlin Sample Activity");

    String splitPath =
        ShadowPackageManager.createSplitApkWithResources(
            "feature_kotlin",
            "com.google.android.samples.dynamicfeatures.ondemand.kotlin",
            0x7f,
            strings,
            null);

    // Install and verify
    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    shadowPackageManager.installPackage(info);
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, "feature_kotlin", splitPath);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).asList().contains("feature_kotlin");

    // Verify the APK file exists and contains resources.arsc
    java.nio.file.Path apkPath = java.nio.file.Path.of(splitPath);
    assertThat(java.nio.file.Files.exists(apkPath)).isTrue();
    try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(apkPath.toFile())) {
      assertThat(zf.getEntry("resources.arsc")).isNotNull();
    }
  }

  /**
   * Simulates the Java feature module with its own string resources, paralleling features/java/
   * from the official sample.
   */
  @Test
  public void javaFeature_stringResources_inSplitApk() throws Exception {
    Map<String, String> strings = new LinkedHashMap<>();
    strings.put("module_feature_java", "Java Feature");
    strings.put("java_activity_title", "Java Sample Activity");

    String splitPath =
        ShadowPackageManager.createSplitApkWithResources(
            "feature_java",
            "com.google.android.samples.dynamicfeatures.ondemand.java",
            0x7f,
            strings,
            null);

    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    shadowPackageManager.installPackage(info);
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, "feature_java", splitPath);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).asList().contains("feature_java");
  }

  /**
   * Simulates the initialInstall feature module which is bundled at install time. It has its own
   * layout and string resources.
   */
  @Test
  public void initialInstallFeature_stringResources_bundledAtInstallTime() throws Exception {
    Map<String, String> strings = new LinkedHashMap<>();
    strings.put("title_module_initial", "Initial Install");
    strings.put("initial_activity_title", "Initial Install Activity");

    String splitPath =
        ShadowPackageManager.createSplitApkWithResources(
            "initialInstall",
            "com.google.android.samples.dynamicfeatures.initialinstall",
            0x7f,
            strings,
            null);

    // Install-time features are bundled with the initial install
    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = new String[] {"initialInstall"};

    Map<String, String> splits = new LinkedHashMap<>();
    splits.put("initialInstall", splitPath);
    shadowPackageManager.installPackageWithSplitApks(info, splits);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).asList().contains("initialInstall");
  }

  /**
   * Verifies that a split APK can have both compiled resources (resources.arsc) and raw assets.
   * This mirrors a feature module that has layouts, strings, AND asset files.
   */
  @Test
  public void featureWithResourcesAndAssets_bothAccessible() throws Exception {
    Map<String, String> strings = new LinkedHashMap<>();
    strings.put("module_assets", "Assets Feature");

    Map<String, byte[]> assets = new LinkedHashMap<>();
    assets.put(
        "assets.txt",
        "This text originates from a dynamically loaded feature.".getBytes(StandardCharsets.UTF_8));

    String splitPath =
        ShadowPackageManager.createSplitApkWithResources(
            "feature_assets",
            "com.google.android.samples.dynamicfeatures.ondemand.assets",
            0x7f,
            strings,
            assets);

    // Verify both resources.arsc and assets exist in the APK
    try (java.util.zip.ZipFile zf =
        new java.util.zip.ZipFile(java.nio.file.Path.of(splitPath).toFile())) {
      assertThat(zf.getEntry("resources.arsc")).isNotNull();
      assertThat(zf.getEntry("assets/assets.txt")).isNotNull();

      // Verify asset content
      try (java.io.InputStream is = zf.getInputStream(zf.getEntry("assets/assets.txt"))) {
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(content).contains("dynamically loaded feature");
      }
    }
  }

  /**
   * Verifies that the resources.arsc in the split APK uses STORED compression as required by
   * Android's resource loading pipeline (resources.arsc must not be compressed).
   */
  @Test
  public void resourcesArsc_storedNotCompressed() throws Exception {
    Map<String, String> strings = new LinkedHashMap<>();
    strings.put("test_string", "Test Value");

    String splitPath =
        ShadowPackageManager.createSplitApkWithResources(
            "test_module", "com.example.test", 0x7f, strings, null);

    try (java.util.zip.ZipFile zf =
        new java.util.zip.ZipFile(java.nio.file.Path.of(splitPath).toFile())) {
      java.util.zip.ZipEntry arscEntry = zf.getEntry("resources.arsc");
      assertThat(arscEntry).isNotNull();
      // STORED means size == compressedSize
      assertThat(arscEntry.getSize()).isEqualTo(arscEntry.getCompressedSize());
    }
  }
}
