package org.robolectric;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.res.ActivityData;
import org.robolectric.res.Fs;
import org.robolectric.res.IntentFilterData;
import org.robolectric.res.ResourcePath;
import org.robolectric.test.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP;
import static android.content.pm.ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA;
import static android.content.pm.ApplicationInfo.FLAG_ALLOW_TASK_REPARENTING;
import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.content.pm.ApplicationInfo.FLAG_HAS_CODE;
import static android.content.pm.ApplicationInfo.FLAG_KILL_AFTER_RESTORE;
import static android.content.pm.ApplicationInfo.FLAG_PERSISTENT;
import static android.content.pm.ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_RESTORE_ANY_VERSION;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_TEST_ONLY;
import static android.content.pm.ApplicationInfo.FLAG_VM_SAFE_MODE;
import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.util.TestUtil.joinPath;
import static org.robolectric.util.TestUtil.newConfig;
import static org.robolectric.util.TestUtil.resourceFile;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AndroidManifestTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void shouldReadContentProviders() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithContentProviders.xml");
    assertThat(config.getContentProviders()).hasSize(2);

    assertThat(config.getContentProviders().get(0).getClassName()).isEqualTo("org.robolectric.tester.FullyQualifiedClassName");
    assertThat(config.getContentProviders().get(0).getAuthority()).isEqualTo("org.robolectric");

    assertThat(config.getContentProviders().get(1).getClassName()).isEqualTo("org.robolectric.tester.PartiallyQualifiedClassName");
    assertThat(config.getContentProviders().get(1).getAuthority()).isEqualTo("org.robolectric");
  }

  @Test
  public void shouldReadBroadcastReceivers() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithReceivers.xml");

    assertEquals(7, config.getReceiverCount());

    assertEquals("org.robolectric.AndroidManifestTest.ConfigTestReceiver", config.getReceiverClassName(0));
    assertEquals("org.robolectric.ACTION1", config.getReceiverIntentFilterActions(0).get(0));

    assertEquals("org.robolectric.AndroidManifestTest.ConfigTestReceiver", config.getReceiverClassName(1));
    assertEquals("org.robolectric.ACTION2", config.getReceiverIntentFilterActions(1).get(0));

    assertEquals("org.robolectric.tester.ConfigTestReceiver", config.getReceiverClassName(2));
    assertEquals("org.robolectric.ACTION_SUPERSET_PACKAGE", config.getReceiverIntentFilterActions(2).get(0));

    assertEquals("org.robolectric.ConfigTestReceiver", config.getReceiverClassName(3));
    assertEquals("org.robolectric.ACTION_SUBSET_PACKAGE", config.getReceiverIntentFilterActions(3).get(0));

    assertEquals("org.robolectric.DotConfigTestReceiver", config.getReceiverClassName(4));
    assertEquals("org.robolectric.ACTION_DOT_PACKAGE", config.getReceiverIntentFilterActions(4).get(0));

    assertEquals("org.robolectric.test.ConfigTestReceiver", config.getReceiverClassName(5));
    assertEquals("org.robolectric.ACTION_DOT_SUBPACKAGE", config.getReceiverIntentFilterActions(5).get(0));

    assertEquals("com.foo.Receiver", config.getReceiverClassName(6));
    assertEquals("org.robolectric.ACTION_DIFFERENT_PACKAGE", config.getReceiverIntentFilterActions(6).get(0));
  }

  @Test
  public void testBroadcastReceiversWithMetaData() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithReceivers.xml");

    assertEquals("org.robolectric.test.ConfigTestReceiver", config.getReceiverClassName(5));
    assertEquals("org.robolectric.ACTION_DOT_SUBPACKAGE", config.getReceiverIntentFilterActions(5).get(0));

    Map<String, String> meta = config.getReceiverMetaData(5);
    Object metaValue = meta.get("org.robolectric.metaName1");
    assertEquals("metaValue1", metaValue);

    metaValue = meta.get("org.robolectric.metaName2");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals("metaValue2", metaValue);

    metaValue = meta.get("org.robolectric.metaFalse");
    assertEquals("false", metaValue);

    metaValue = meta.get("org.robolectric.metaTrue");
    assertEquals("true", metaValue);

    metaValue = meta.get("org.robolectric.metaInt");
    assertEquals("123", metaValue);

    metaValue = meta.get("org.robolectric.metaFloat");
    assertEquals("1.23", metaValue);

    metaValue = meta.get("org.robolectric.metaStringRes");
    assertEquals("@string/app_name", metaValue);
  }

  @Test
  public void shouldReadTargetSdkVersionFromAndroidManifestOrDefaultToMin() throws Exception {
    assertEquals(42, newConfigWith("android:targetSdkVersion=\"42\" android:minSdkVersion=\"7\"").getTargetSdkVersion());
    assertEquals(7, newConfigWith("android:minSdkVersion=\"7\"").getTargetSdkVersion());
    assertEquals(1, newConfigWith("").getTargetSdkVersion());
  }

  @Test
  public void shouldReadMinSdkVersionFromAndroidManifestOrDefaultToOne() throws Exception {
    assertEquals(17, newConfigWith("android:minSdkVersion=\"17\"").getMinSdkVersion());
    assertEquals(1, newConfigWith("").getMinSdkVersion());
  }

  @Test
  public void shouldReadProcessFromAndroidManifest() throws Exception {
    assertEquals("robolectricprocess", newConfig("TestAndroidManifestWithProcess.xml").getProcessName());
  }

  @Test
  public void shouldReturnPackageNameWhenNoProcessIsSpecifiedInTheManifest() {
    assertEquals("org.robolectric", newConfig("TestAndroidManifestWithNoProcess.xml").getProcessName());
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithAppMetaData.xml")
  public void shouldReturnApplicationMetaData() throws PackageManager.NameNotFoundException {
    Map<String, String> meta = newConfig("TestAndroidManifestWithAppMetaData.xml").getApplicationMetaData();

    Object metaValue = meta.get("org.robolectric.metaName1");
    assertEquals("metaValue1", metaValue);

    metaValue = meta.get("org.robolectric.metaName2");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals("metaValue2", metaValue);

    metaValue = meta.get("org.robolectric.metaFalse");
    assertEquals("false", metaValue);

    metaValue = meta.get("org.robolectric.metaTrue");
    assertEquals("true", metaValue);

    metaValue = meta.get("org.robolectric.metaInt");
    assertEquals("123", metaValue);

    metaValue = meta.get("org.robolectric.metaFloat");
    assertEquals("1.23", metaValue);

    metaValue = meta.get("org.robolectric.metaStringRes");
    assertEquals("@string/app_name", metaValue);
  }
  
  @Test public void shouldLoadAllResourcesForExistingLibraries() {
    AndroidManifest appManifest = new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"));

    // This intentionally loads from the non standard resources/project.properties
    List<String> resourcePaths = stringify(appManifest.getIncludedResourcePaths());
    assertEquals(asList(
        joinPath(".", "src", "test", "resources", "res"),
        joinPath(".", "src", "test", "resources", "lib1", "res"),
        joinPath(".", "src", "test", "resources", "lib1", "..", "lib3", "res"),
        joinPath(".", "src", "test", "resources", "lib2", "res")),
        resourcePaths);
  }

  @Test
  public void shouldTolerateMissingRFile() throws Exception {
    AndroidManifest appManifest = new AndroidManifest(resourceFile("TestAndroidManifestWithNoRFile.xml"), resourceFile("res"));
    assertEquals(appManifest.getPackageName(), "org.no.resources.for.me");
    assertThat(appManifest.getRClass()).isNull();
    assertEquals(appManifest.getResourcePath().getPackageName(), "org.no.resources.for.me");
  }

  @Test
  public void shouldRead1IntentFilter() {
    AndroidManifest appManifest = newConfig("TestAndroidManifestForActivitiesWithIntentFilter.xml");
    appManifest.getMinSdkVersion(); // Force parsing

    ActivityData activityData = appManifest.getActivityData("org.robolectric.shadows.TestActivity");
    final List<IntentFilterData> ifd = activityData.getIntentFilters();
    assertThat(ifd).isNotNull();
    assertThat(ifd.size()).isEqualTo(1);

    final IntentFilterData data = ifd.get(0);
    assertThat(data.getActions().size()).isEqualTo(1);
    assertThat(data.getActions().get(0)).isEqualTo(Intent.ACTION_MAIN);
    assertThat(data.getCategories().size()).isEqualTo(1);
    assertThat(data.getCategories().get(0)).isEqualTo(Intent.CATEGORY_LAUNCHER);
  }

  @Test
  public void shouldReadMultipleIntentFilters() {
    AndroidManifest appManifest = newConfig("TestAndroidManifestForActivitiesWithMultipleIntentFilters.xml");
    appManifest.getMinSdkVersion(); // Force parsing

    ActivityData activityData = appManifest.getActivityData("org.robolectric.shadows.TestActivity");
    final List<IntentFilterData> ifd = activityData.getIntentFilters();
    assertThat(ifd).isNotNull();
    assertThat(ifd.size()).isEqualTo(2);

    IntentFilterData data = ifd.get(0);
    assertThat(data.getActions().size()).isEqualTo(1);
    assertThat(data.getActions().get(0)).isEqualTo(Intent.ACTION_MAIN);
    assertThat(data.getCategories().size()).isEqualTo(1);
    assertThat(data.getCategories().get(0)).isEqualTo(Intent.CATEGORY_LAUNCHER);

    data = ifd.get(1);
    assertThat(data.getActions().size()).isEqualTo(3);
    assertThat(data.getActions().get(0)).isEqualTo(Intent.ACTION_VIEW);
    assertThat(data.getActions().get(1)).isEqualTo(Intent.ACTION_EDIT);
    assertThat(data.getActions().get(2)).isEqualTo(Intent.ACTION_PICK);

    assertThat(data.getCategories().size()).isEqualTo(3);
    assertThat(data.getCategories().get(0)).isEqualTo(Intent.CATEGORY_DEFAULT);
    assertThat(data.getCategories().get(1)).isEqualTo(Intent.CATEGORY_ALTERNATIVE);
    assertThat(data.getCategories().get(2)).isEqualTo(Intent.CATEGORY_SELECTED_ALTERNATIVE);
  }

  @Test
  public void shouldReadTaskAffinity() {
    AndroidManifest appManifest = newConfig("TestAndroidManifestForActivitiesWithTaskAffinity.xml");
    assertThat(appManifest.getTargetSdkVersion()).isEqualTo(16);

    ActivityData activityData = appManifest.getActivityData("org.robolectric.shadows.TestTaskAffinityActivity");
    assertThat(activityData).isNotNull();
    assertThat(activityData.getTaskAffinity()).isEqualTo("org.robolectric.shadows.TestTaskAffinity");
  }

  /////////////////////////////

  public AndroidManifest newConfigWith(String usesSdkAttrs) throws IOException {
    File f = temporaryFolder.newFile("whatever.xml",
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "          package=\"org.robolectric\">\n" +
            "    <uses-sdk " + usesSdkAttrs + "/>\n" +
            "</manifest>\n");
    return new AndroidManifest(Fs.newFile(f), null, null);
  }

  private List<String> stringify(List<ResourcePath> resourcePaths) {
    List<String> resourcePathBases = new ArrayList<String>();
    for (ResourcePath resourcePath : resourcePaths) {
      resourcePathBases.add(resourcePath.resourceBase.toString());
    }
    return resourcePathBases;
  }

  @Test
  public void shouldReadFlagsFromAndroidManifest() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithFlags.xml");
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_ALLOW_BACKUP));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_ALLOW_CLEAR_USER_DATA));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_ALLOW_TASK_REPARENTING));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_DEBUGGABLE));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_HAS_CODE));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_KILL_AFTER_RESTORE));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_PERSISTENT));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_RESIZEABLE_FOR_SCREENS));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_RESTORE_ANY_VERSION));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_LARGE_SCREENS));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_NORMAL_SCREENS));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_SCREEN_DENSITIES));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_SMALL_SCREENS));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_TEST_ONLY));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_VM_SAFE_MODE));
  }

  private boolean hasFlag(final int flags, final int flag) {
    return (flags & flag) != 0;
  }

  @SuppressWarnings("unused")
  public static class ConfigTestReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    }
  }
}
