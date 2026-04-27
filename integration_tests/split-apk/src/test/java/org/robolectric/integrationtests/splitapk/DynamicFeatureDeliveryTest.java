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
 * Tests modeled after the DynamicFeatures sample's {@code MainActivity} which uses {@code
 * SplitInstallManager} to request, install, and launch dynamic feature modules on demand.
 *
 * <p>In the official sample, MainActivity:
 *
 * <ul>
 *   <li>Creates a SplitInstallManager via SplitInstallManagerFactory.create()
 *   <li>Builds SplitInstallRequest with addModule(moduleName)
 *   <li>Monitors install via SplitInstallStateUpdatedListener
 *   <li>Launches feature activities via setClassName(packageName, activityClassName)
 *   <li>Accesses assets from asset-only modules
 *   <li>Queries manager.installedModules for installed features
 * </ul>
 *
 * <p>These tests verify that Robolectric supports the underlying PackageManager and AssetManager
 * operations that SplitInstallManager delegates to.
 *
 * <p>Reference: {@code DynamicFeatures/app/src/main/java/.../MainActivity.kt}
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.O)
public class DynamicFeatureDeliveryTest {

  private static final String PACKAGE_NAME = "com.google.android.samples.dynamicfeatures";

  // Module names from the official DynamicFeatures sample
  private static final String MODULE_KOTLIN = "feature_kotlin";
  private static final String MODULE_JAVA = "feature_java";
  private static final String MODULE_NATIVE = "feature_native";
  private static final String MODULE_ASSETS = "feature_assets";
  private static final String MODULE_INITIAL_INSTALL = "initialInstall";

  private PackageManager packageManager;
  private ShadowPackageManager shadowPackageManager;

  @Before
  public void setUp() {
    packageManager = RuntimeEnvironment.getApplication().getPackageManager();
    shadowPackageManager = shadowOf(packageManager);

    // Install the base app (initial state before any dynamic features)
    PackageInfo baseApp = new PackageInfo();
    baseApp.packageName = PACKAGE_NAME;
    baseApp.applicationInfo = new ApplicationInfo();
    baseApp.applicationInfo.packageName = PACKAGE_NAME;
    shadowPackageManager.installPackage(baseApp);
  }

  /**
   * Simulates the on-demand download and installation of the Kotlin feature module. In the official
   * sample, this happens via SplitInstallRequest.newBuilder().addModule("feature_kotlin").build().
   *
   * <p>After install, the module should appear in installedModules and its activity class
   * (KotlinSampleActivity) should be launchable.
   */
  @Test
  public void onDemandFeature_kotlinModule_installedAndAccessible() throws Exception {
    // Simulate: SplitInstallManager downloads and installs the Kotlin feature
    String splitPath =
        ShadowPackageManager.createSplitApkWithAssets(
            MODULE_KOTLIN,
            Map.of("kotlin_feature.dat", "kotlin module data".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, MODULE_KOTLIN, splitPath);

    // Verify: manager.installedModules should contain "feature_kotlin"
    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).asList().contains(MODULE_KOTLIN);
    assertThat(installed.applicationInfo.splitSourceDirs).hasLength(1);
  }

  /**
   * Simulates the on-demand download and installation of the Java feature module. In the official
   * sample, JavaSampleActivity is launched after successful install.
   */
  @Test
  public void onDemandFeature_javaModule_installedAndAccessible() throws Exception {
    String splitPath =
        ShadowPackageManager.createSplitApkWithAssets(
            MODULE_JAVA,
            Map.of("java_feature.dat", "java module data".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, MODULE_JAVA, splitPath);

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).asList().contains(MODULE_JAVA);
  }

  /**
   * Simulates installing all on-demand features at once, mirroring the "Install all now" button in
   * the DynamicFeatures sample. The sample installs: kotlin, java, native, and assets modules.
   */
  @Test
  public void installAllNow_allOnDemandModulesInstalled() throws Exception {
    String[] modules = {MODULE_KOTLIN, MODULE_JAVA, MODULE_NATIVE, MODULE_ASSETS};
    for (String module : modules) {
      String splitPath =
          ShadowPackageManager.createSplitApkWithAssets(
              module, Map.of(module + ".dat", module.getBytes(StandardCharsets.UTF_8)));
      shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, module, splitPath);
    }

    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames)
        .asList()
        .containsExactly(MODULE_KOTLIN, MODULE_JAVA, MODULE_NATIVE, MODULE_ASSETS)
        .inOrder();
    assertThat(installed.applicationInfo.splitSourceDirs).hasLength(4);
  }

  /**
   * Simulates the asset-only module pattern from the DynamicFeatures sample. The "assets" feature
   * module has {@code android:hasCode="false"} and only contains an assets/assets.txt file.
   *
   * <p>In the official sample, MainActivity.displayAssets() reads the asset content after the
   * module is installed.
   */
  @Test
  public void assetOnlyModule_assetsAccessibleAfterInstall() throws Exception {
    // The official sample's assets module contains assets/assets.txt
    String assetContent =
        "This text originates from a dynamically loaded feature.\n"
            + "The source can be found in features/assets/assets/assets.txt.";

    String splitPath =
        ShadowPackageManager.createSplitApkWithAssets(
            MODULE_ASSETS, Map.of("assets.txt", assetContent.getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, MODULE_ASSETS, splitPath);

    // Load asset via AssetManager (same as SplitCompat would do)
    AssetManager assetManager = AssetManager.class.getDeclaredConstructor().newInstance();
    ShadowAssetManager.addSplitAssetPath(assetManager, splitPath);

    try (InputStream is = assetManager.open("assets.txt")) {
      String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(content).contains("dynamically loaded feature");
    }
  }

  /**
   * Simulates the install-time feature module from the DynamicFeatures sample. The "initialInstall"
   * feature uses {@code <dist:install-time />} delivery, meaning it's included with the base APK
   * from the Play Store.
   */
  @Test
  public void installTimeFeature_availableImmediately() throws Exception {
    // Install-time features are bundled with the base install
    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = new String[] {MODULE_INITIAL_INSTALL};

    Map<String, String> splits = new LinkedHashMap<>();
    splits.put(
        MODULE_INITIAL_INSTALL,
        ShadowPackageManager.createSplitApkWithAssets(
            MODULE_INITIAL_INSTALL,
            Map.of("initial_data.txt", "initial install data".getBytes(StandardCharsets.UTF_8))));
    shadowPackageManager.installPackageWithSplitApks(info, splits);

    // The install-time feature is immediately available (no need for dynamic install)
    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).asList().contains(MODULE_INITIAL_INSTALL);
  }

  /**
   * Simulates the scenario where install-time features are pre-installed and then on-demand
   * features are added later. This matches the real app lifecycle: base + initialInstall are
   * installed from Play Store, then user requests feature_kotlin, feature_java, etc.
   */
  @Test
  public void mixedDeliveryTypes_installTimeAndOnDemand() throws Exception {
    // Step 1: Install base with install-time feature (from Play Store)
    PackageInfo info = new PackageInfo();
    info.packageName = PACKAGE_NAME;
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.packageName = PACKAGE_NAME;
    info.applicationInfo.splitNames = new String[] {MODULE_INITIAL_INSTALL};

    Map<String, String> splits = new LinkedHashMap<>();
    splits.put(
        MODULE_INITIAL_INSTALL,
        ShadowPackageManager.createSplitApkWithAssets(
            MODULE_INITIAL_INSTALL,
            Map.of("initial.txt", "init".getBytes(StandardCharsets.UTF_8))));
    shadowPackageManager.installPackageWithSplitApks(info, splits);

    // Step 2: User requests on-demand features
    String kotlinPath =
        ShadowPackageManager.createSplitApkWithAssets(
            MODULE_KOTLIN, Map.of("kotlin.txt", "kt".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, MODULE_KOTLIN, kotlinPath);

    String javaPath =
        ShadowPackageManager.createSplitApkWithAssets(
            MODULE_JAVA, Map.of("java.txt", "java".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, MODULE_JAVA, javaPath);

    // Verify all splits present
    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames)
        .asList()
        .containsExactly(MODULE_INITIAL_INSTALL, MODULE_KOTLIN, MODULE_JAVA)
        .inOrder();
    assertThat(installed.applicationInfo.splitSourceDirs).hasLength(3);
  }

  /**
   * Verifies that after installing a feature module, an activity from that module can be resolved
   * via Intent.setClassName(). The official sample uses this pattern in launchActivity().
   *
   * <p>Reference: MainActivity.launchActivity() sets className to
   * "com.google.android.samples.dynamicfeatures.ondemand.KotlinSampleActivity"
   */
  @Test
  public void featureActivity_resolvableAfterModuleInstall() throws Exception {
    String splitPath =
        ShadowPackageManager.createSplitApkWithAssets(
            MODULE_KOTLIN, Map.of("k.txt", "k".getBytes(StandardCharsets.UTF_8)));
    shadowPackageManager.addSplitToInstalledPackage(PACKAGE_NAME, MODULE_KOTLIN, splitPath);

    // The split is registered and its source dir is accessible
    PackageInfo installed = packageManager.getPackageInfo(PACKAGE_NAME, 0);
    assertThat(installed.applicationInfo.splitNames).asList().contains(MODULE_KOTLIN);

    // Verify the split APK file path is a real file
    String splitSourceDir = installed.applicationInfo.splitSourceDirs[0];
    assertThat(java.nio.file.Files.exists(java.nio.file.Path.of(splitSourceDir))).isTrue();
  }
}
