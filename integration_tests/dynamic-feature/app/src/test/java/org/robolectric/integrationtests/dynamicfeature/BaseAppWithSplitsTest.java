package org.robolectric.integrationtests.dynamicfeature;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Robolectric tests that run inside the base app module with both dynamic feature splits installed.
 */
@RunWith(RobolectricTestRunner.class)
public class BaseAppWithSplitsTest {
  private static final String BASE_PACKAGE = "org.robolectric.integrationtests.dynamicfeature";
  private static final String FEATURE1_PACKAGE = BASE_PACKAGE + ".feature1";
  private static final String FEATURE2_PACKAGE = BASE_PACKAGE + ".feature2";

  @Test
  public void packageInfo_containsBothFeatureSplits() throws Exception {
    Application app = RuntimeEnvironment.getApplication();
    assertThat(app.getPackageName()).isEqualTo(BASE_PACKAGE);

    ApplicationInfo appInfo = app.getApplicationInfo();
    assertThat(appInfo.splitNames).asList().containsExactly("feature1", "feature2");
    assertThat(appInfo.splitSourceDirs).hasLength(2);
    assertThat(appInfo.splitPublicSourceDirs).isEqualTo(appInfo.splitSourceDirs);

    PackageInfo packageInfo = app.getPackageManager().getPackageInfo(BASE_PACKAGE, 0);
    assertThat(packageInfo.splitNames).asList().containsExactly("feature1", "feature2");
  }

  @Test
  public void packageInfo_mergesActivitiesFromBothFeatureSplits() throws Exception {
    Application app = RuntimeEnvironment.getApplication();
    PackageInfo packageInfo =
        app.getPackageManager().getPackageInfo(BASE_PACKAGE, PackageManager.GET_ACTIVITIES);

    Map<String, String> activitySplits = new HashMap<>();
    for (ActivityInfo activityInfo : packageInfo.activities) {
      activitySplits.put(activityInfo.name, activityInfo.splitName);
    }

    assertThat(activitySplits).containsEntry(FEATURE1_PACKAGE + ".FeatureActivity", "feature1");
    assertThat(activitySplits).containsEntry(FEATURE2_PACKAGE + ".FeatureActivity", "feature2");
  }

  @Test
  public void resources_resolveBaseAndBothFeatures() {
    Application app = RuntimeEnvironment.getApplication();
    Resources res = app.getResources();

    assertThat(app.getString(R.string.app_name)).isEqualTo("Dynamic feature");

    int feature1Id = res.getIdentifier("feature_greeting", "string", FEATURE1_PACKAGE);
    int feature2Id = res.getIdentifier("feature_greeting", "string", FEATURE2_PACKAGE);
    assertThat(feature1Id).isNotEqualTo(0);
    assertThat(feature2Id).isNotEqualTo(0);
    assertThat(res.getString(feature1Id)).isEqualTo("Hello from feature 1");
    assertThat(res.getString(feature2Id)).isEqualTo("Hello from feature 2");
  }

  @Test
  public void createContextForSplit_readsBothFeatures() throws Exception {
    Application app = RuntimeEnvironment.getApplication();

    Context split1 = app.createContextForSplit("feature1");
    int feature1Id =
        split1.getResources().getIdentifier("feature_greeting", "string", FEATURE1_PACKAGE);
    assertThat(split1.getString(feature1Id)).isEqualTo("Hello from feature 1");

    Context split2 = app.createContextForSplit("feature2");
    int feature2Id =
        split2.getResources().getIdentifier("feature_greeting", "string", FEATURE2_PACKAGE);
    assertThat(split2.getString(feature2Id)).isEqualTo("Hello from feature 2");
  }

  @Test
  public void loadedApk_getSplitClassLoader_validatesAgainstClusterSplits() throws Exception {
    Application app = RuntimeEnvironment.getApplication();
    Object loadedApk = reflector(ContextImplReflector.class, app.getBaseContext()).getPackageInfo();
    LoadedApkReflector loadedApkReflector = reflector(LoadedApkReflector.class, loadedApk);

    assertThat(loadedApkReflector.getSplitClassLoader("feature1")).isNotNull();
    assertThat(loadedApkReflector.getSplitClassLoader("feature2")).isNotNull();
    assertThrows(
        NameNotFoundException.class, () -> loadedApkReflector.getSplitClassLoader("nonexistent"));
  }

  @Test
  public void addSplit_loadsRealFeatureSplitsIncrementally() throws Exception {
    Application app = RuntimeEnvironment.getApplication();
    ApplicationInfo appInfo = app.getApplicationInfo();
    Map<String, String> splitPaths = new LinkedHashMap<>();
    for (int i = 0; i < appInfo.splitNames.length; i++) {
      splitPaths.put(appInfo.splitNames[i], appInfo.splitSourceDirs[i]);
    }

    PackageManager pm = app.getPackageManager();
    String pkgName = "com.example.dynamicdelivery";
    PackageInfo basePkg = new PackageInfo();
    basePkg.packageName = pkgName;
    shadowOf(pm).installPackage(basePkg);

    shadowOf(pm).addSplit(pkgName, "feature1", splitPaths.get("feature1"));
    Resources res = pm.getResourcesForApplication(pkgName);
    int feature1Id = res.getIdentifier("feature_greeting", "string", FEATURE1_PACKAGE);
    assertThat(res.getString(feature1Id)).isEqualTo("Hello from feature 1");

    shadowOf(pm).addSplit(pkgName, "feature2", splitPaths.get("feature2"));
    int feature2Id = res.getIdentifier("feature_greeting", "string", FEATURE2_PACKAGE);
    assertThat(res.getString(feature2Id)).isEqualTo("Hello from feature 2");
  }

  @ForType(className = "android.app.ContextImpl")
  interface ContextImplReflector {
    @Accessor("mPackageInfo")
    Object getPackageInfo();
  }

  @ForType(className = "android.app.LoadedApk")
  interface LoadedApkReflector {
    ClassLoader getSplitClassLoader(String splitName) throws NameNotFoundException;
  }
}
