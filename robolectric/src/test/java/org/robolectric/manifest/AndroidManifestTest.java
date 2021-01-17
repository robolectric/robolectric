package org.robolectric.manifest;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.util.TestUtil.resourceFile;

import android.Manifest;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;

@RunWith(JUnit4.class)
public class AndroidManifestTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void parseManifest_shouldReadContentProviders() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithContentProviders.xml");

    assertThat(config.getContentProviders().get(0).getName())
        .isEqualTo("org.robolectric.tester.FullyQualifiedClassName");
    assertThat(config.getContentProviders().get(0).getAuthorities())
        .isEqualTo("org.robolectric.authority1");
    assertThat(config.getContentProviders().get(0).isEnabled()).isTrue();

    assertThat(config.getContentProviders().get(1).getName())
        .isEqualTo("org.robolectric.tester.PartiallyQualifiedClassName");
    assertThat(config.getContentProviders().get(1).getAuthorities())
        .isEqualTo("org.robolectric.authority2");
    assertThat(config.getContentProviders().get(1).isEnabled()).isFalse();
  }

  @Test
  public void parseManifest_shouldReadPermissions() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithPermissions.xml");

    assertThat(config.getPermissions().keySet())
        .containsExactly("some_permission",
            "permission_with_literal_label",
            "permission_with_minimal_fields");
    PermissionItemData permissionItemData = config.getPermissions().get("some_permission");
    assertThat(permissionItemData.getMetaData().getValueMap())
        .containsEntry("meta_data_name", "meta_data_value");
    assertThat(permissionItemData.getName()).isEqualTo("some_permission");
    assertThat(permissionItemData.getPermissionGroup()).isEqualTo("my_permission_group");
    assertThat(permissionItemData.getDescription())
        .isEqualTo("@string/test_permission_description");
    assertThat(permissionItemData.getProtectionLevel()).isEqualTo("dangerous");
  }

  @Test
  public void parseManifest_shouldReadPermissionGroups() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithPermissions.xml");

    assertThat(config.getPermissionGroups().keySet())
        .contains("permission_group");
    PermissionGroupItemData permissionGroupItemData =
        config.getPermissionGroups().get("permission_group");
    assertThat(permissionGroupItemData.getName()).isEqualTo("permission_group");
    assertThat(permissionGroupItemData.getDescription())
        .isEqualTo("@string/test_permission_description");
  }

  @Test
  public void parseManifest_shouldReadBroadcastReceivers() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithReceivers.xml");
    assertThat(config.getBroadcastReceivers()).hasSize(8);

    assertThat(config.getBroadcastReceivers().get(0).getName())
        .isEqualTo("org.robolectric.ConfigTestReceiver.InnerReceiver");
    assertThat(config.getBroadcastReceivers().get(0).getActions())
        .containsExactly("org.robolectric.ACTION1", "org.robolectric.ACTION2");

    assertThat(config.getBroadcastReceivers().get(1).getName())
        .isEqualTo("org.robolectric.fakes.ConfigTestReceiver");
    assertThat(config.getBroadcastReceivers().get(1).getActions())
        .contains("org.robolectric.ACTION_SUPERSET_PACKAGE");

    assertThat(config.getBroadcastReceivers().get(2).getName())
        .isEqualTo("org.robolectric.ConfigTestReceiver");
    assertThat(config.getBroadcastReceivers().get(2).getActions())
        .contains("org.robolectric.ACTION_SUBSET_PACKAGE");

    assertThat(config.getBroadcastReceivers().get(3).getName())
        .isEqualTo("org.robolectric.DotConfigTestReceiver");
    assertThat(config.getBroadcastReceivers().get(3).getActions())
        .contains("org.robolectric.ACTION_DOT_PACKAGE");

    assertThat(config.getBroadcastReceivers().get(4).getName())
        .isEqualTo("org.robolectric.test.ConfigTestReceiver");
    assertThat(config.getBroadcastReceivers().get(4).getActions())
        .contains("org.robolectric.ACTION_DOT_SUBPACKAGE");
    assertThat(config.getBroadcastReceivers().get(4).isEnabled()).isFalse();

    assertThat(config.getBroadcastReceivers().get(5).getName()).isEqualTo("com.foo.Receiver");
    assertThat(config.getBroadcastReceivers().get(5).getActions())
        .contains("org.robolectric.ACTION_DIFFERENT_PACKAGE");
    assertThat(config.getBroadcastReceivers().get(5).getIntentFilters()).hasSize(1);
    IntentFilterData filter = config.getBroadcastReceivers().get(5).getIntentFilters().get(0);
    assertThat(filter.getActions()).containsExactly("org.robolectric.ACTION_DIFFERENT_PACKAGE");

    assertThat(config.getBroadcastReceivers().get(6).getName())
        .isEqualTo("com.bar.ReceiverWithoutIntentFilter");
    assertThat(config.getBroadcastReceivers().get(6).getActions()).isEmpty();

    assertThat(config.getBroadcastReceivers().get(7).getName())
        .isEqualTo("org.robolectric.ConfigTestReceiverPermissionsAndActions");
    assertThat(config.getBroadcastReceivers().get(7).getActions())
        .contains("org.robolectric.ACTION_RECEIVER_PERMISSION_PACKAGE");
  }

  @Test
  public void parseManifest_shouldReadServices() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithServices.xml");
    assertThat(config.getServices()).hasSize(2);

    assertThat(config.getServices().get(0).getClassName()).isEqualTo("com.foo.Service");
    assertThat(config.getServices().get(0).getActions())
        .contains("org.robolectric.ACTION_DIFFERENT_PACKAGE");
    assertThat(config.getServices().get(0).getIntentFilters()).isNotEmpty();
    assertThat(config.getServices().get(0).getIntentFilters().get(0).getMimeTypes())
        .containsExactly("image/jpeg");

    assertThat(config.getServices().get(1).getClassName())
        .isEqualTo("com.bar.ServiceWithoutIntentFilter");
    assertThat(config.getServices().get(1).getActions()).isEmpty();
    assertThat(config.getServices().get(1).getIntentFilters()).isEmpty();

    assertThat(config.getServiceData("com.foo.Service").getClassName())
        .isEqualTo("com.foo.Service");
    assertThat(config.getServiceData("com.bar.ServiceWithoutIntentFilter").getClassName())
        .isEqualTo("com.bar.ServiceWithoutIntentFilter");
    assertThat(config.getServiceData("com.foo.Service").getPermission())
        .isEqualTo("com.foo.Permission");

    assertThat(config.getServiceData("com.foo.Service").isEnabled()).isTrue();
    assertThat(config.getServiceData("com.bar.ServiceWithoutIntentFilter").isEnabled()).isFalse();
  }

  @Test
  public void testManifestWithNoApplicationElement() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestNoApplicationElement.xml");
    assertThat(config.getPackageName()).isEqualTo("org.robolectric");
  }

  @Test
  public void parseManifest_shouldReadBroadcastReceiversWithMetaData() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithReceivers.xml");

    assertThat(config.getBroadcastReceivers().get(4).getName())
        .isEqualTo("org.robolectric.test.ConfigTestReceiver");
    assertThat(config.getBroadcastReceivers().get(4).getActions())
        .contains("org.robolectric.ACTION_DOT_SUBPACKAGE");

    Map<String, Object> meta = config.getBroadcastReceivers().get(4).getMetaData().getValueMap();
    Object metaValue = meta.get("org.robolectric.metaName1");
    assertThat(metaValue).isEqualTo("metaValue1");

    metaValue = meta.get("org.robolectric.metaName2");
    assertThat(metaValue).isEqualTo("metaValue2");

    metaValue = meta.get("org.robolectric.metaFalse");
    assertThat(metaValue).isEqualTo("false");

    metaValue = meta.get("org.robolectric.metaTrue");
    assertThat(metaValue).isEqualTo("true");

    metaValue = meta.get("org.robolectric.metaInt");
    assertThat(metaValue).isEqualTo("123");

    metaValue = meta.get("org.robolectric.metaFloat");
    assertThat(metaValue).isEqualTo("1.23");

    metaValue = meta.get("org.robolectric.metaColor");
    assertThat(metaValue).isEqualTo("#FFFFFF");

    metaValue = meta.get("org.robolectric.metaBooleanFromRes");
    assertThat(metaValue).isEqualTo("@bool/false_bool_value");

    metaValue = meta.get("org.robolectric.metaIntFromRes");
    assertThat(metaValue).isEqualTo("@integer/test_integer1");

    metaValue = meta.get("org.robolectric.metaColorFromRes");
    assertThat(metaValue).isEqualTo("@color/clear");

    metaValue = meta.get("org.robolectric.metaStringFromRes");
    assertThat(metaValue).isEqualTo("@string/app_name");

    metaValue = meta.get("org.robolectric.metaStringOfIntFromRes");
    assertThat(metaValue).isEqualTo("@string/str_int");

    metaValue = meta.get("org.robolectric.metaStringRes");
    assertThat(metaValue).isEqualTo("@string/app_name");
  }

  @Test
  public void shouldReadBroadcastReceiverPermissions() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithReceivers.xml");

    assertThat(config.getBroadcastReceivers().get(7).getName())
        .isEqualTo("org.robolectric.ConfigTestReceiverPermissionsAndActions");
    assertThat(config.getBroadcastReceivers().get(7).getActions())
        .contains("org.robolectric.ACTION_RECEIVER_PERMISSION_PACKAGE");

    assertThat(config.getBroadcastReceivers().get(7).getPermission())
        .isEqualTo("org.robolectric.CUSTOM_PERM");
  }

  @Test
  public void shouldReadTargetSdkVersionFromAndroidManifestOrDefaultToMin() throws Exception {
    assertThat(
            newConfigWith(
                    "targetsdk42minsdk6.xml",
                    "android:targetSdkVersion=\"42\" android:minSdkVersion=\"7\"")
                .getTargetSdkVersion())
        .isEqualTo(42);
    assertThat(newConfigWith("minsdk7.xml", "android:minSdkVersion=\"7\"").getTargetSdkVersion())
        .isEqualTo(7);
    assertThat(newConfigWith("noattributes.xml", "").getTargetSdkVersion())
        .isEqualTo(VERSION_CODES.JELLY_BEAN);
  }

  @Test
  public void shouldReadMinSdkVersionFromAndroidManifestOrDefaultToJellyBean() throws Exception {
    assertThat(newConfigWith("minsdk17.xml", "android:minSdkVersion=\"17\"").getMinSdkVersion())
        .isEqualTo(17);
    assertThat(newConfigWith("noattributes.xml", "").getMinSdkVersion())
        .isEqualTo(VERSION_CODES.JELLY_BEAN);
  }

  /**
   * For Android O preview, apps are encouraged to use targetSdkVersion="O".
   *
   * @see <a href="http://google.com">https://developer.android.com/preview/migration.html</a>
   */
  @Test
  public void shouldReadTargetSDKVersionOPreview() throws Exception {
    assertThat(
            newConfigWith("TestAndroidManifestForPreview.xml", "android:targetSdkVersion=\"O\"")
                .getTargetSdkVersion())
        .isEqualTo(26);
  }

  @Test
  public void shouldReadProcessFromAndroidManifest() throws Exception {
    assertThat(newConfig("TestAndroidManifestWithProcess.xml").getProcessName())
        .isEqualTo("robolectricprocess");
  }

  @Test
  public void shouldReturnPackageNameWhenNoProcessIsSpecifiedInTheManifest() {
    assertThat(newConfig("TestAndroidManifestWithNoProcess.xml").getProcessName())
        .isEqualTo("org.robolectric");
  }

  @Test
  @Config(manifest = "TestAndroidManifestWithAppMetaData.xml")
  public void shouldReturnApplicationMetaData() throws Exception {
    Map<String, Object> meta =
        newConfig("TestAndroidManifestWithAppMetaData.xml").getApplicationMetaData();

    Object metaValue = meta.get("org.robolectric.metaName1");
    assertThat(metaValue).isEqualTo("metaValue1");

    metaValue = meta.get("org.robolectric.metaName2");
    assertThat(metaValue).isEqualTo("metaValue2");

    metaValue = meta.get("org.robolectric.metaFalse");
    assertThat(metaValue).isEqualTo("false");

    metaValue = meta.get("org.robolectric.metaTrue");
    assertThat(metaValue).isEqualTo("true");

    metaValue = meta.get("org.robolectric.metaInt");
    assertThat(metaValue).isEqualTo("123");

    metaValue = meta.get("org.robolectric.metaFloat");
    assertThat(metaValue).isEqualTo("1.23");

    metaValue = meta.get("org.robolectric.metaColor");
    assertThat(metaValue).isEqualTo("#FFFFFF");

    metaValue = meta.get("org.robolectric.metaBooleanFromRes");
    assertThat(metaValue).isEqualTo("@bool/false_bool_value");

    metaValue = meta.get("org.robolectric.metaIntFromRes");
    assertThat(metaValue).isEqualTo("@integer/test_integer1");

    metaValue = meta.get("org.robolectric.metaColorFromRes");
    assertThat(metaValue).isEqualTo("@color/clear");

    metaValue = meta.get("org.robolectric.metaStringFromRes");
    assertThat(metaValue).isEqualTo("@string/app_name");

    metaValue = meta.get("org.robolectric.metaStringOfIntFromRes");
    assertThat(metaValue).isEqualTo("@string/str_int");

    metaValue = meta.get("org.robolectric.metaStringRes");
    assertThat(metaValue).isEqualTo("@string/app_name");
  }

  @Test
  public void shouldTolerateMissingRFile() throws Exception {
    AndroidManifest appManifest =
        new AndroidManifest(
            resourceFile("TestAndroidManifestWithNoRFile.xml"),
            resourceFile("res"),
            resourceFile("assets"));
    assertThat(appManifest.getPackageName()).isEqualTo("org.no.resources.for.me");
    assertThat(appManifest.getRClass()).isNull();
  }

  @Test
  public void whenNullManifestFile_getRClass_shouldComeFromPackageName() throws Exception {
    AndroidManifest appManifest =
        new AndroidManifest(null, resourceFile("res"), resourceFile("assets"), "org.robolectric");
    assertThat(appManifest.getRClass()).isEqualTo(org.robolectric.R.class);
    assertThat(appManifest.getPackageName()).isEqualTo("org.robolectric");
  }

  @Test
  public void whenMissingManifestFile_getRClass_shouldComeFromPackageName() throws Exception {
    AndroidManifest appManifest =
        new AndroidManifest(
            resourceFile("none.xml"),
            resourceFile("res"),
            resourceFile("assets"),
            "org.robolectric");
    assertThat(appManifest.getRClass()).isEqualTo(org.robolectric.R.class);
    assertThat(appManifest.getPackageName()).isEqualTo("org.robolectric");
  }

  @Test
  public void whenMissingManifestFile_getPackageName_shouldBeDefault() throws Exception {
    AndroidManifest appManifest =
        new AndroidManifest(null, resourceFile("res"), resourceFile("assets"), null);
    assertThat(appManifest.getPackageName()).isEqualTo("org.robolectric.default");
    assertThat(appManifest.getRClass()).isEqualTo(null);
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
    AndroidManifest appManifest =
        newConfig("TestAndroidManifestForActivitiesWithMultipleIntentFilters.xml");
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

    ActivityData activityData =
        appManifest.getActivityData("org.robolectric.shadows.TestTaskAffinityActivity");
    assertThat(activityData).isNotNull();
    assertThat(activityData.getTaskAffinity())
        .isEqualTo("org.robolectric.shadows.TestTaskAffinity");
  }

  @Test
  public void shouldReadPermissions() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithPermissions.xml");

    assertThat(config.getUsedPermissions()).hasSize(3);
    assertThat(config.getUsedPermissions().get(0)).isEqualTo(Manifest.permission.INTERNET);
    assertThat(config.getUsedPermissions().get(1)).isEqualTo(Manifest.permission.SYSTEM_ALERT_WINDOW);
    assertThat(config.getUsedPermissions().get(2)).isEqualTo(Manifest.permission.GET_TASKS);
  }

  @Test
  public void shouldReadPartiallyQualifiedActivities() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestForActivities.xml");
    assertThat(config.getActivityDatas()).hasSize(2);
    assertThat(config.getActivityDatas()).containsKey("org.robolectric.shadows.TestActivity");
    assertThat(config.getActivityDatas()).containsKey("org.robolectric.shadows.TestActivity2");
  }

  @Test
  public void shouldReadActivityAliases() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestForActivityAliases.xml");
    assertThat(config.getActivityDatas()).hasSize(2);
    assertThat(config.getActivityDatas()).containsKey("org.robolectric.shadows.TestActivity");
    assertThat(config.getActivityDatas()).containsKey("org.robolectric.shadows.TestActivityAlias");
  }

  @Test
  public void shouldReadIntentFilterWithData() {
    AndroidManifest appManifest =
        newConfig("TestAndroidManifestForActivitiesWithIntentFilterWithData.xml");
    appManifest.getMinSdkVersion(); // Force parsing

    ActivityData activityData = appManifest.getActivityData("org.robolectric.shadows.TestActivity");
    final List<IntentFilterData> ifd = activityData.getIntentFilters();
    assertThat(ifd).isNotNull();
    assertThat(ifd.size()).isEqualTo(1);

    final IntentFilterData intentFilterData = ifd.get(0);
    assertThat(intentFilterData.getActions().size()).isEqualTo(1);
    assertThat(intentFilterData.getActions().get(0)).isEqualTo(Intent.ACTION_VIEW);
    assertThat(intentFilterData.getCategories().size()).isEqualTo(1);
    assertThat(intentFilterData.getCategories().get(0)).isEqualTo(Intent.CATEGORY_DEFAULT);

    assertThat(intentFilterData.getSchemes().size()).isEqualTo(3);
    assertThat(intentFilterData.getAuthorities().size()).isEqualTo(3);
    assertThat(intentFilterData.getMimeTypes().size()).isEqualTo(3);
    assertThat(intentFilterData.getPaths().size()).isEqualTo(1);
    assertThat(intentFilterData.getPathPatterns().size()).isEqualTo(1);
    assertThat(intentFilterData.getPathPrefixes().size()).isEqualTo(1);


    assertThat(intentFilterData.getSchemes().get(0)).isEqualTo("content");
    assertThat(intentFilterData.getPaths().get(0)).isEqualTo("/testPath/test.jpeg");
    assertThat(intentFilterData.getMimeTypes().get(0)).isEqualTo("video/mpeg");
    assertThat(intentFilterData.getAuthorities().get(0).getHost()).isEqualTo("testhost1.com");
    assertThat(intentFilterData.getAuthorities().get(0).getPort()).isEqualTo("1");

    assertThat(intentFilterData.getSchemes().get(1)).isEqualTo("http");
    assertThat(intentFilterData.getPathPrefixes().get(0)).isEqualTo("/testPrefix");
    assertThat(intentFilterData.getMimeTypes().get(1)).isEqualTo("image/jpeg");
    assertThat(intentFilterData.getAuthorities().get(1).getHost()).isEqualTo("testhost2.com");
    assertThat(intentFilterData.getAuthorities().get(1).getPort()).isEqualTo("2");

    assertThat(intentFilterData.getSchemes().get(2)).isEqualTo("https");
    assertThat(intentFilterData.getPathPatterns().get(0)).isEqualTo("/.*testPattern");
    assertThat(intentFilterData.getMimeTypes().get(2)).isEqualTo("image/*");
    assertThat(intentFilterData.getAuthorities().get(2).getHost()).isEqualTo("testhost3.com");
    assertThat(intentFilterData.getAuthorities().get(2).getPort()).isEqualTo("3");
  }

  @Test
  public void shouldHaveStableHashCode() throws Exception {
    AndroidManifest manifest = newConfig("TestAndroidManifestWithContentProviders.xml");
    int hashCode1 = manifest.hashCode();
    manifest.getServices();
    int hashCode2 = manifest.hashCode();
    assertThat(hashCode2).isEqualTo(hashCode1);
  }

  @Test
  public void shouldReadApplicationAttrsFromAndroidManifest() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithFlags.xml");
    assertThat(config.getApplicationAttributes().get("android:allowBackup")).isEqualTo("true");
  }

  @Test
  public void allFieldsShouldBePrimitivesOrJavaLangOrRobolectric() throws Exception {
    List<Field> wrongFields = new ArrayList<>();
    for (Field field : AndroidManifest.class.getDeclaredFields()) {
      Class<?> type = field.getType();
      if (type.isPrimitive()) continue;

      String packageName = type.getPackage().getName();
      if (packageName.startsWith("java.")
          || packageName.equals("org.robolectric.res")
          || packageName.equals("org.robolectric.manifest")
          ) continue;

      wrongFields.add(field);
    }

    assertThat(wrongFields).isEmpty();
  }

  @Test
  public void activitiesWithoutIntentFiltersNotExportedByDefault() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestForActivities.xml");
    ActivityData activityData = config.getActivityData("org.robolectric.shadows.TestActivity");
    assertThat(activityData.isExported()).isFalse();
  }

  @Test
  public void activitiesWithIntentFiltersExportedByDefault() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestForActivitiesWithIntentFilter.xml");
    ActivityData activityData = config.getActivityData("org.robolectric.shadows.TestActivity");
    assertThat(activityData.isExported()).isTrue();
  }

  @Test
  public void servicesWithoutIntentFiltersNotExportedByDefault() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithServices.xml");
    ServiceData serviceData = config.getServiceData("com.bar.ServiceWithoutIntentFilter");
    assertThat(serviceData.isExported()).isFalse();
  }

  @Test
  public void servicesWithIntentFiltersExportedByDefault() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithServices.xml");
    ServiceData serviceData = config.getServiceData("com.foo.Service");
    assertThat(serviceData.isExported()).isTrue();
  }

  @Test
  public void receiversWithoutIntentFiltersNotExportedByDefault() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithReceivers.xml");
    BroadcastReceiverData receiverData =
        config.getBroadcastReceiver("com.bar.ReceiverWithoutIntentFilter");
    assertThat(receiverData).isNotNull();
    assertThat(receiverData.isExported()).isFalse();
  }

  @Test
  public void receiversWithIntentFiltersExportedByDefault() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithReceivers.xml");
    BroadcastReceiverData receiverData = config.getBroadcastReceiver("com.foo.Receiver");
    assertThat(receiverData).isNotNull();
    assertThat(receiverData.isExported()).isTrue();
  }

  @Test
  public void getTransitiveManifests() throws Exception {
    AndroidManifest lib1 =
        new AndroidManifest(resourceFile("lib1/AndroidManifest.xml"), null, null);
    AndroidManifest lib2 = new AndroidManifest(resourceFile("lib2/AndroidManifest.xml"), null, null,
        Collections.singletonList(lib1), null);
    AndroidManifest app = new AndroidManifest(
        resourceFile("TestAndroidManifestWithReceivers.xml"), null, null,
        Arrays.asList(lib1, lib2), null);
    assertThat(app.getAllManifests()).containsExactly(app, lib1, lib2);
  }

  /////////////////////////////

  private AndroidManifest newConfigWith(String fileName, String usesSdkAttrs) throws IOException {
    String contents = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
        "          package=\"org.robolectric\">\n" +
        "    <uses-sdk " + usesSdkAttrs + "/>\n" +
        "</manifest>\n";
    File f = temporaryFolder.newFile(fileName);
    Files.asCharSink(f, UTF_8).write(contents);
    return new AndroidManifest(f.toPath(), null, null);
  }

  private static AndroidManifest newConfig(String androidManifestFile) {
    return new AndroidManifest(resourceFile(androidManifestFile), null, null);
  }
}
