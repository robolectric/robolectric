package org.robolectric.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.PermissionItemData;
import org.robolectric.res.Fs;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowDrawable;
import org.robolectric.test.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 23)
public class DefaultPackageManagerTest {
  private static final String TEST_PACKAGE_NAME = "com.some.other.package";
  private static final String TEST_PACKAGE_LABEL = "My Little App";
  private static final String TEST_APP_PATH = "/values/app/application.apk";
  private final RobolectricPackageManager rpm = RuntimeEnvironment.getRobolectricPackageManager();
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private PackageManager packageManager;

  @Before
  public void setUp() throws Exception {
    packageManager = RuntimeEnvironment.application.getPackageManager();
  }

  @Test
  public void getPackageInstaller() {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    rpm.addPackage(packageInfo);

    List<PackageInstaller.SessionInfo> allSessions = packageManager.getPackageInstaller().getAllSessions();

    List<String> allPackageNames = new LinkedList<>();
    for (PackageInstaller.SessionInfo session : allSessions) {
      allPackageNames.add(session.appPackageName);
    }

    assertThat(allPackageNames).contains(TEST_PACKAGE_NAME);
  }

  @Test
  public void packageInstallerAndGetInstalledPackagesAreConsistent() {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    rpm.addPackage(packageInfo);

    List<PackageInstaller.SessionInfo> allSessions = packageManager.getPackageInstaller().getAllSessions();

    assertThat(allSessions).hasSameSizeAs(rpm.getInstalledPackages(0));
  }

  @Test
  public void packageInstallerAndGetPackageArchiveInfo() {
    ApplicationInfo appInfo = new ApplicationInfo();
    appInfo.flags = 0;
    appInfo.packageName = TEST_PACKAGE_NAME;
    appInfo.sourceDir = TEST_APP_PATH;
    appInfo.name = TEST_PACKAGE_LABEL;

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = appInfo;
    rpm.addPackage(packageInfo);

    PackageInfo packageInfoResult = rpm.getPackageArchiveInfo(TEST_APP_PATH, 0);
    assertThat(packageInfoResult).isNotNull();
    ApplicationInfo applicationInfo = packageInfoResult.applicationInfo;
    assertThat(applicationInfo).isInstanceOf(ApplicationInfo.class);
    assertThat(applicationInfo.packageName).isEqualTo(TEST_PACKAGE_NAME);
    assertThat(applicationInfo.sourceDir).isEqualTo(TEST_APP_PATH);

  }

  @Test @Config(manifest = "src/test/resources/TestAndroidManifestWithFlags.xml")
  public void applicationFlags() throws Exception {
    int flags = rpm.getApplicationInfo("org.robolectric", 0).flags;
    assertThat(flags).isEqualTo(
        FLAG_ALLOW_BACKUP
        | FLAG_ALLOW_CLEAR_USER_DATA
        | FLAG_ALLOW_TASK_REPARENTING
        | FLAG_DEBUGGABLE
        | FLAG_HAS_CODE
        | FLAG_KILL_AFTER_RESTORE
        | FLAG_PERSISTENT
        | FLAG_RESIZEABLE_FOR_SCREENS
        | FLAG_RESTORE_ANY_VERSION
        | FLAG_SUPPORTS_LARGE_SCREENS
        | FLAG_SUPPORTS_NORMAL_SCREENS
        | FLAG_SUPPORTS_SCREEN_DENSITIES
        | FLAG_SUPPORTS_SMALL_SCREENS
        | FLAG_TEST_ONLY
        | FLAG_VM_SAFE_MODE
    );
  }

  @Test
  public void getApplicationInfo_ThisApplication() throws Exception {
    ApplicationInfo info = rpm.getApplicationInfo(RuntimeEnvironment.application.getPackageName(), 0);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(RuntimeEnvironment.application.getPackageName());
  }

  @Test(expected = NameNotFoundException.class)
  public void getApplicationInfo_whenUnknown_shouldThrowNameNotFoundException() throws Exception {
    try {
      rpm.getApplicationInfo("unknown_package", 0);
      fail("should have thrown NameNotFoundException");
    } catch (NameNotFoundException e) {
      assertThat(e.getMessage()).contains("unknown_package");
      throw e;
    }
  }

  @Test
  public void getApplicationInfo_OtherApplication() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    rpm.addPackage(packageInfo);

    ApplicationInfo info = rpm.getApplicationInfo(TEST_PACKAGE_NAME, 0);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(TEST_PACKAGE_NAME);
    assertThat(rpm.getApplicationLabel(info).toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test(expected = PackageManager.NameNotFoundException.class)
  public void removePackage_shouldHideItFromGetApplicationInfo() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    rpm.addPackage(packageInfo);
    rpm.removePackage(TEST_PACKAGE_NAME);

    rpm.getApplicationInfo(TEST_PACKAGE_NAME, 0);
  }

  @Test
  public void queryIntentActivities_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> activities = rpm.queryIntentActivities(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  public void queryIntentActivities_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    rpm.addResolveInfoForIntent(i, info);

    List<ResolveInfo> activities = rpm.queryIntentActivities(i, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).nonLocalizedLabel.toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestForActivitiesWithIntentFilterWithData.xml")
  public void queryIntentActivities_EmptyResultWithNoMatchingImplicitIntents() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    rpm.setQueryIntentImplicitly(true);
    List<ResolveInfo> activities = rpm.queryIntentActivities(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestForActivitiesWithIntentFilterWithData.xml")
  public void queryIntentActivities_MatchWithImplicitIntents() throws Exception {
    Uri uri = Uri.parse("content://testhost1.com:1/testPath/test.jpeg");
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.addCategory(Intent.CATEGORY_DEFAULT);
    i.setDataAndType(uri, "image/jpeg");

    rpm.setQueryIntentImplicitly(true);
    List<ResolveInfo> activities = rpm.queryIntentActivities(i, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).resolvePackageName.toString()).isEqualTo("org.robolectric");
    assertThat(activities.get(0).activityInfo.targetActivity.toString()).isEqualTo("org.robolectric.shadows.TestActivity");
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestForActivityAliases.xml")
  public void queryIntentActivities_MatchWithAliasIntents() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    rpm.setQueryIntentImplicitly(true);
    List<ResolveInfo> activities = rpm.queryIntentActivities(i, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).resolvePackageName.toString()).isEqualTo("org.robolectric");
    assertThat(activities.get(0).activityInfo.targetActivity.toString()).isEqualTo("org.robolectric.shadows.TestActivity");
  }

  @Test
  public void resolveActivity_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    rpm.addResolveInfoForIntent(i, info);

    assertThat(rpm.resolveActivity(i, 0)).isSameAs(info);
  }

  @Test
  public void resolveActivity_NoMatch() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName("foo.bar", "No Activity"));
    assertThat(rpm.resolveActivity(i, 0)).isNull();
  }

  @Test
  public void queryIntentServices_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> activities = rpm.queryIntentServices(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  public void queryIntentServices_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    rpm.addResolveInfoForIntent(i, info);

    List<ResolveInfo> activities = rpm.queryIntentServices(i, 0);
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).nonLocalizedLabel.toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void queryBroadcastReceivers_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> broadCastReceivers = rpm.queryBroadcastReceivers(i, 0);
    assertThat(broadCastReceivers).isEmpty();
  }

  @Test
  public void queryBroadcastReceivers_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    rpm.addResolveInfoForIntent(i, info);

    List<ResolveInfo> broadCastReceivers = rpm.queryBroadcastReceivers(i, 0);
    assertThat(broadCastReceivers).hasSize(1);
    assertThat(broadCastReceivers.get(0).nonLocalizedLabel.toString())
        .isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void resolveService_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    ResolveInfo info = new ResolveInfo();
    rpm.addResolveInfoForIntent(i, info);
    assertThat(rpm.resolveService(i, 0)).isSameAs(info);
  }

  @Test
  public void removeResolveInfosForIntent_shouldCauseResolveActivityToReturnNull() throws Exception {
    Intent intent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    info.activityInfo = new ActivityInfo();
    info.activityInfo.packageName = "com.org";
    rpm.addResolveInfoForIntent(intent, info);

    rpm.removeResolveInfosForIntent(intent, "com.org");

    assertThat(rpm.resolveActivity(intent, 0)).isNull();
  }

  @Test
  public void resolveService_NoMatch() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName("foo.bar", "No Activity"));
    assertThat(rpm.resolveService(i, 0)).isNull();
  }

  @Test
  public void queryActivityIcons_Match() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName(TEST_PACKAGE_NAME, ""));
    Drawable d = new BitmapDrawable();

    rpm.addActivityIcon(i, d);

    assertThat(rpm.getActivityIcon(i)).isSameAs(d);
    assertThat(rpm.getActivityIcon(i.getComponent())).isSameAs(d);
  }

  @Test
  public void hasSystemFeature() throws Exception {
    // uninitialized
    assertThat(rpm.hasSystemFeature(PackageManager.FEATURE_CAMERA)).isFalse();

    // positive
    rpm.setSystemFeature(PackageManager.FEATURE_CAMERA, true);
    assertThat(rpm.hasSystemFeature(PackageManager.FEATURE_CAMERA)).isTrue();

    // negative
    rpm.setSystemFeature(PackageManager.FEATURE_CAMERA, false);
    assertThat(rpm.hasSystemFeature(PackageManager.FEATURE_CAMERA)).isFalse();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithContentProviders.xml")
  public void getPackageInfo_getProvidersShouldReturnProviderInfos() throws Exception {
    PackageInfo packageInfo = rpm.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_PROVIDERS);
    ProviderInfo[] providers = packageInfo.providers;
    assertThat(providers).isNotEmpty();
    assertThat(providers.length).isEqualTo(3);
    assertThat(providers[0].packageName).isEqualTo("org.robolectric");
    assertThat(providers[1].packageName).isEqualTo("org.robolectric");
    assertThat(providers[2].packageName).isEqualTo("org.robolectric");
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithContentProviders.xml")
  public void getProviderInfo_shouldReturnProviderInfos() throws Exception {
    ProviderInfo packageInfo1 = packageManager.getProviderInfo(new ComponentName(RuntimeEnvironment.application, ".tester.FullyQualifiedClassName"), 0);
    assertThat(packageInfo1.packageName).isEqualTo("org.robolectric");
    assertThat(packageInfo1.authority).isEqualTo("org.robolectric.authority1");

    ProviderInfo packageInfo2 = packageManager.getProviderInfo(new ComponentName(RuntimeEnvironment.application, "org.robolectric.tester.PartiallyQualifiedClassName"), 0);
    assertThat(packageInfo2.packageName).isEqualTo("org.robolectric");
    assertThat(packageInfo2.authority).isEqualTo("org.robolectric.authority2");
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithContentProviders.xml")
  public void getProviderInfo_shouldPopulatePermissionsInProviderInfos() throws Exception {
    ProviderInfo providerInfo = packageManager.getProviderInfo(new ComponentName(RuntimeEnvironment.application, "org.robolectric.android.controller.ContentProviderControllerTest$MyContentProvider"), 0);
    assertThat(providerInfo.authority).isEqualTo("org.robolectric.authority2");

    assertThat(providerInfo.readPermission).isEqualTo("READ_PERMISSION");
    assertThat(providerInfo.writePermission).isEqualTo("WRITE_PERMISSION");

    assertThat(providerInfo.pathPermissions).hasSize(1);
    assertThat(providerInfo.pathPermissions[0].getPath()).isEqualTo("/path/*");
    assertThat(providerInfo.pathPermissions[0].getType()).isEqualTo(PathPermission.PATTERN_SIMPLE_GLOB);
    assertThat(providerInfo.pathPermissions[0].getReadPermission()).isEqualTo("PATH_READ_PERMISSION");
    assertThat(providerInfo.pathPermissions[0].getWritePermission()).isEqualTo("PATH_WRITE_PERMISSION");
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithNoContentProviders.xml")
  public void getPackageInfo_getProvidersShouldReturnNullOnNoProviders() throws Exception {
    PackageInfo packageInfo = rpm.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_PROVIDERS);
    ProviderInfo[] providers = packageInfo.providers;
    assertThat(providers).isNull();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void testReceiverInfo() throws Exception {
    ActivityInfo info = rpm.getReceiverInfo(new ComponentName(RuntimeEnvironment.application, ".test.ConfigTestReceiver"), PackageManager.GET_META_DATA);
    Bundle meta = info.metaData;
    Object metaValue = meta.get("org.robolectric.metaName1");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals("metaValue1", metaValue);

    metaValue = meta.get("org.robolectric.metaName2");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals("metaValue2", metaValue);

    metaValue = meta.get("org.robolectric.metaFalse");
    assertTrue(Boolean.class.isInstance(metaValue));
    assertEquals(false, metaValue);

    metaValue = meta.get("org.robolectric.metaTrue");
    assertTrue(Boolean.class.isInstance(metaValue));
    assertEquals(true, metaValue);

    metaValue = meta.get("org.robolectric.metaInt");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(123, metaValue);

    metaValue = meta.get("org.robolectric.metaFloat");
    assertTrue(Float.class.isInstance(metaValue));
    assertEquals(new Float(1.23), metaValue);

    metaValue = meta.get("org.robolectric.metaColor");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(Color.WHITE, metaValue);

    metaValue = meta.get("org.robolectric.metaBooleanFromRes");
    assertTrue(Boolean.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getResources().getBoolean(R.bool.false_bool_value), metaValue);

    metaValue = meta.get("org.robolectric.metaIntFromRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getResources().getInteger(R.integer.test_integer1), metaValue);

    metaValue = meta.get("org.robolectric.metaColorFromRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getResources().getColor(R.color.clear), metaValue);

    metaValue = meta.get("org.robolectric.metaStringFromRes");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getString(R.string.app_name), metaValue);

    metaValue = meta.get("org.robolectric.metaStringOfIntFromRes");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getString(R.string.str_int), metaValue);

    metaValue = meta.get("org.robolectric.metaStringRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(R.string.app_name, metaValue);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithPermissions.xml")
  public void testCheckPermissions() throws Exception {
    assertEquals(PackageManager.PERMISSION_GRANTED, rpm.checkPermission("android.permission.INTERNET", RuntimeEnvironment.application.getPackageName()));
    assertEquals(PackageManager.PERMISSION_GRANTED, rpm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", RuntimeEnvironment.application.getPackageName()));
    assertEquals(PackageManager.PERMISSION_GRANTED, rpm.checkPermission("android.permission.GET_TASKS", RuntimeEnvironment.application.getPackageName()));

    assertEquals(PackageManager.PERMISSION_DENIED, rpm.checkPermission("android.permission.ACCESS_FINE_LOCATION", RuntimeEnvironment.application.getPackageName()));
    assertEquals(PackageManager.PERMISSION_DENIED, rpm.checkPermission("android.permission.ACCESS_FINE_LOCATION", "random-package"));
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void testQueryBroadcastReceiverSucceeds() {
    Intent intent = new Intent("org.robolectric.ACTION_RECEIVER_PERMISSION_PACKAGE");
    intent.setPackage(RuntimeEnvironment.application.getPackageName());

    List<ResolveInfo> receiverInfos = rpm.queryBroadcastReceivers(intent, PackageManager.GET_INTENT_FILTERS);
    assertTrue(receiverInfos.size() == 1);
    assertEquals("org.robolectric.ConfigTestReceiverPermissionsAndActions", receiverInfos.get(0).activityInfo.name);
    assertEquals("org.robolectric.CUSTOM_PERM", receiverInfos.get(0).activityInfo.permission);
    assertEquals("org.robolectric.ACTION_RECEIVER_PERMISSION_PACKAGE", receiverInfos.get(0).filter.getAction(0));
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void testQueryBroadcastReceiverFailsForMissingPackageName() {
    Intent intent = new Intent("org.robolectric.ACTION_ONE_MORE_PACKAGE");
    List<ResolveInfo> receiverInfos = rpm.queryBroadcastReceivers(intent, PackageManager.GET_INTENT_FILTERS);
    assertTrue(receiverInfos.size() == 0);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void testQueryBroadcastReceiverFailsForMissingAction() {
    Intent intent = new Intent();
    intent.setPackage(RuntimeEnvironment.application.getPackageName());
    List<ResolveInfo> receiverInfos = rpm.queryBroadcastReceivers(intent, PackageManager.GET_INTENT_FILTERS);
    assertTrue(receiverInfos.size() == 0);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceiversCustomPackage.xml")
  public void testGetPackageInfo_ForReceiversSucceeds() throws Exception {
    PackageInfo receiverInfos = rpm.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_RECEIVERS);

    assertEquals(1, receiverInfos.receivers.length);
    assertEquals("org.robolectric.ConfigTestReceiverPermissionsAndActions", receiverInfos.receivers[0].name);
    assertEquals("org.robolectric.CUSTOM_PERM", receiverInfos.receivers[0].permission);
  }

  @Test(expected = PackageManager.NameNotFoundException.class)
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceiversCustomPackage.xml")
  public void testGetPackageInfo_ForReceiversIncorrectPackage() throws Exception {
    try {
      rpm.getPackageInfo("unknown_package", PackageManager.GET_RECEIVERS);
      fail("should have thrown NameNotFoundException");
    } catch (NameNotFoundException e) {
      assertThat(e.getMessage()).contains("unknown_package");
      throw e;
    }
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithPermissions.xml")
  public void getPackageInfo_shouldReturnRequestedPermissions() throws Exception {
    PackageInfo packageInfo = rpm.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_PERMISSIONS);
    String[] permissions = packageInfo.requestedPermissions;
    assertThat(permissions).isNotNull();
    assertThat(permissions.length).isEqualTo(3);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithoutPermissions.xml")
  public void getPackageInfo_shouldReturnNullOnNoRequestedPermissions() throws Exception {
    PackageInfo packageInfo = rpm.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_PERMISSIONS);
    String[] permissions = packageInfo.requestedPermissions;
    assertThat(permissions).isNull();
  }

  @Test
  public void testGetPreferredActivities() throws Exception {
    // Setup an intentfilter and add to packagemanager
    IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
    filter.addCategory(Intent.CATEGORY_HOME);
    final String packageName = "com.example.dummy";
    ComponentName name = new ComponentName(packageName, "LauncherActivity");
    rpm.addPreferredActivity(filter, 0, null, name);

    // Test match
    List<IntentFilter> filters = new ArrayList<>();
    filters.add(filter);

    List<ComponentName> activities = new ArrayList<>();
    rpm.getPreferredActivities(filters, activities, null);

    assertThat(activities.size()).isEqualTo(1);
    assertThat(activities.get(0).getPackageName()).isEqualTo(packageName);

    // Test not match
    IntentFilter filter1 = new IntentFilter(Intent.ACTION_VIEW);
    filters.add(filter1);
    filters.clear();
    activities.clear();
    filters.add(filter1);

    rpm.getPreferredActivities(filters, activities, null);

    assertThat(activities.size()).isEqualTo(0);
  }

  @Test
  public void canResolveDrawableGivenPackageAndResourceId() throws Exception {
    Drawable drawable = ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), "my_source");
    rpm.addDrawableResolution("com.example.foo", 4334, drawable);
    Drawable actual = rpm.getDrawable("com.example.foo", 4334, null);
    assertThat(actual).isSameAs(drawable);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void shouldAssignTheApplicationNameFromTheManifest() throws Exception {
    ApplicationInfo applicationInfo = rpm.getApplicationInfo("org.robolectric", 0);
    assertThat(applicationInfo.name).isEqualTo("org.robolectric.TestApplication");
  }

  @Test
  public void testLaunchIntentForPackage() {
    Intent intent = rpm.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent).isNull();

    Intent launchIntent = new Intent(Intent.ACTION_MAIN);
    launchIntent.setPackage(TEST_PACKAGE_LABEL);
    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "LauncherActivity";
    RuntimeEnvironment.getRobolectricPackageManager().addResolveInfoForIntent(launchIntent, resolveInfo);

    intent = rpm.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent).isNotNull();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithAppMetaData.xml")
  public void shouldAssignTheAppMetaDataFromTheManifest() throws Exception {
    ShadowApplication app = ShadowApplication.getInstance();
    String packageName = app.getAppManifest().getPackageName();
    ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
    Bundle meta = info.metaData;

    Object metaValue = meta.get("org.robolectric.metaName1");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals("metaValue1", metaValue);

    metaValue = meta.get("org.robolectric.metaName2");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals("metaValue2", metaValue);

    metaValue = meta.get("org.robolectric.metaFalse");
    assertTrue(Boolean.class.isInstance(metaValue));
    assertEquals(false, metaValue);

    metaValue = meta.get("org.robolectric.metaTrue");
    assertTrue(Boolean.class.isInstance(metaValue));
    assertEquals(true, metaValue);

    metaValue = meta.get("org.robolectric.metaInt");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(123, metaValue);

    metaValue = meta.get("org.robolectric.metaFloat");
    assertTrue(Float.class.isInstance(metaValue));
    assertEquals(new Float(1.23), metaValue);

    metaValue = meta.get("org.robolectric.metaColor");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(Color.WHITE, metaValue);

    metaValue = meta.get("org.robolectric.metaBooleanFromRes");
    assertTrue(Boolean.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getResources().getBoolean(R.bool.false_bool_value), metaValue);

    metaValue = meta.get("org.robolectric.metaIntFromRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getResources().getInteger(R.integer.test_integer1), metaValue);

    metaValue = meta.get("org.robolectric.metaColorFromRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getResources().getColor(R.color.clear), metaValue);

    metaValue = meta.get("org.robolectric.metaStringFromRes");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getString(R.string.app_name), metaValue);

    metaValue = meta.get("org.robolectric.metaStringOfIntFromRes");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals(RuntimeEnvironment.application.getString(R.string.str_int), metaValue);

    metaValue = meta.get("org.robolectric.metaStringRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(R.string.app_name, metaValue);
  }

  @Test
  public void testResolveDifferentIntentObjects() {
    Intent intent1 = rpm.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent1).isNull();

    intent1 = new Intent(Intent.ACTION_MAIN);
    intent1.setPackage(TEST_PACKAGE_LABEL);
    intent1.addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "LauncherActivity";
    RuntimeEnvironment.getRobolectricPackageManager().addResolveInfoForIntent(intent1, resolveInfo);
    
    // the original intent object should yield a result
    ResolveInfo result  = rpm.resolveActivity(intent1, -1);
    assertThat(result).isNotNull();

    // AND a new, functionally equivalent intent should also yield a result
    Intent intent2 = new Intent(Intent.ACTION_MAIN);
    intent2.setPackage(TEST_PACKAGE_LABEL);
    intent2.addCategory(Intent.CATEGORY_LAUNCHER);
    result = rpm.resolveActivity(intent2, -1);
    assertThat(result).isNotNull();
  }

  @Test
  public void testResolvePartiallySimilarIntents() {
    Intent intent1 = rpm.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent1).isNull();

    intent1 = new Intent(Intent.ACTION_MAIN);
    intent1.setPackage(TEST_PACKAGE_LABEL);
    intent1.addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "LauncherActivity";
    RuntimeEnvironment.getRobolectricPackageManager().addResolveInfoForIntent(intent1, resolveInfo);

    // the original intent object should yield a result
    ResolveInfo result  = rpm.resolveActivity(intent1, -1);
    assertThat(result).isNotNull();

    // an intent with just the same action should not be considered the same
    Intent intent2 = new Intent(Intent.ACTION_MAIN);
    result = rpm.resolveActivity(intent2, -1);
    assertThat(result).isNull();

    // an intent with just the same category should not be considered the same 
    Intent intent3 = new Intent();
    intent3.addCategory(Intent.CATEGORY_LAUNCHER);
    result = rpm.resolveActivity(intent3, -1);
    assertThat(result).isNull();

    // an intent without the correct package restriction should not be the same
    Intent intent4 = new Intent(Intent.ACTION_MAIN);
    intent4.addCategory(Intent.CATEGORY_LAUNCHER);
    result = rpm.resolveActivity(intent4, -1);
    assertThat(result).isNull();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void testSetApplicationEnabledSetting() {
    assertThat(packageManager.getApplicationEnabledSetting("org.robolectric")).isEqualTo(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

    packageManager.setApplicationEnabledSetting("org.robolectric", PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

    assertThat(packageManager.getApplicationEnabledSetting("org.robolectric")).isEqualTo(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void testSetComponentEnabledSetting() {
    ComponentName componentName = new ComponentName(RuntimeEnvironment.application, "org.robolectric.component");
    assertThat(packageManager.getComponentEnabledSetting(componentName)).isEqualTo(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

    assertThat(packageManager.getComponentEnabledSetting(componentName)).isEqualTo(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
  }

  public static class ActivityWithMetadata extends Activity { }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void getActivityMetaData() throws Exception {
    Activity activity = setupActivity(ActivityWithMetadata.class);

    ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), PackageManager.GET_ACTIVITIES|PackageManager.GET_META_DATA);
    assertThat(activityInfo.metaData.get("someName")).isEqualTo("someValue");
  }

  public static class ActivityWithConfigChanges extends Activity { }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void getActivityMetaData_configChanges() throws Exception {
    Activity activity = setupActivity(ActivityWithConfigChanges.class);

    ActivityInfo activityInfo = RuntimeEnvironment.getPackageManager().getActivityInfo(activity.getComponentName(), 0);

    int configChanges = activityInfo.configChanges;
    assertThat(configChanges & ActivityInfo.CONFIG_MCC).isEqualTo(ActivityInfo.CONFIG_MCC);
    assertThat(configChanges & ActivityInfo.CONFIG_SCREEN_LAYOUT).isEqualTo(ActivityInfo.CONFIG_SCREEN_LAYOUT);
    assertThat(configChanges & ActivityInfo.CONFIG_ORIENTATION).isEqualTo(ActivityInfo.CONFIG_ORIENTATION);

    // Spot check a few other possible values that shouldn't be in the flags.
    assertThat(configChanges & ActivityInfo.CONFIG_MNC).isZero();
    assertThat(configChanges & ActivityInfo.CONFIG_FONT_SCALE).isZero();
    assertThat(configChanges & ActivityInfo.CONFIG_SCREEN_SIZE).isZero();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void shouldAssignLabelResFromTheManifest() throws Exception {
    ApplicationInfo applicationInfo = rpm.getApplicationInfo("org.robolectric", 0);
    assertThat(applicationInfo.labelRes).isEqualTo(R.string.app_name);
    assertThat(applicationInfo.nonLocalizedLabel).isNull();
  }
  
  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithAppMetaData.xml")
  public void shouldAssignNonLocalizedLabelFromTheManifest() throws Exception {
    ApplicationInfo applicationInfo = rpm.getApplicationInfo("org.robolectric", 0);
    assertThat(applicationInfo.labelRes).isEqualTo(0);
    assertThat(applicationInfo.nonLocalizedLabel).isEqualTo("App Label");
  }

  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldReturnServiceInfoIfExists() throws Exception {
    ServiceInfo serviceInfo = rpm.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_SERVICES);
    assertEquals(serviceInfo.packageName, "org.robolectric");
    assertEquals(serviceInfo.name, "com.foo.Service");
    assertEquals(serviceInfo.permission, "com.foo.MY_PERMISSION");
    assertNotNull(serviceInfo.applicationInfo);  
  }
  
  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldReturnServiceInfoWithMetaDataWhenFlagsSet() throws Exception {
    ServiceInfo serviceInfo = rpm.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_META_DATA);
    assertNotNull(serviceInfo.metaData);
  }
  
  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldReturnServiceInfoWithoutMetaDataWhenFlagsNotSet() throws Exception {
    ServiceInfo serviceInfo = rpm.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_SERVICES);
    assertNull(serviceInfo.metaData);
  }
  
  @Test(expected = NameNotFoundException.class)
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldThrowNameNotFoundExceptionIfNotExist() throws Exception {
    ComponentName nonExistComponent = new ComponentName("org.robolectric", "com.foo.NonExistService");
    try {
      rpm.getServiceInfo(nonExistComponent, PackageManager.GET_SERVICES);
      fail("should have thrown NameNotFoundException");
    } catch (NameNotFoundException e) {
      assertThat(e.getMessage()).contains("com.foo.NonExistService");
      throw e;
    }
  }

  @Test
  public void getNameForUid() {
    assertThat(packageManager.getNameForUid(10)).isNull();

    rpm.setNameForUid(10, "a_name");

    assertThat(packageManager.getNameForUid(10)).isEqualTo("a_name");
  }

  @Test
  public void getPackagesForUid() {
    assertThat(packageManager.getPackagesForUid(10)).isNull();

    rpm.setPackagesForUid(10, new String[] {"a_name"});

    assertThat(packageManager.getPackagesForUid(10)).containsExactly("a_name");
  }

  @Test(expected = PackageManager.NameNotFoundException.class)
  @Config(manifest = "src/test/resources/TestAndroidManifestWithPermissions.xml")
  public void getPermissionInfo_notFound() throws Exception {
    packageManager.getPermissionInfo("non_existant_permission", 0);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithPermissions.xml")
  public void getPermissionInfo_noMetaData() throws Exception {
    PermissionInfo permission = packageManager.getPermissionInfo("some_permission", 0);
    assertThat(permission.metaData).isNull();
    assertThat(permission.name).isEqualTo("some_permission");
    assertThat(permission.descriptionRes).isEqualTo(R.string.test_permission_description);
    assertThat(permission.labelRes).isEqualTo(R.string.test_permission_label);
    assertThat(permission.nonLocalizedLabel).isNullOrEmpty();
    assertThat(permission.group).isEqualTo("my_permission_group");
    assertThat(permission.protectionLevel).isEqualTo(PermissionInfo.PROTECTION_DANGEROUS);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithPermissions.xml")
  public void getPermissionInfo_withMetaData() throws Exception {
    PermissionInfo permission = packageManager.getPermissionInfo("some_permission", PackageManager.GET_META_DATA);
    assertThat(permission.metaData).isNotNull();
    assertThat(permission.metaData.getString("meta_data_name")).isEqualTo("meta_data_value");
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithPermissions.xml")
  public void getPermissionInfo_withLiteralLabel() throws Exception {
    PermissionInfo permission = packageManager.getPermissionInfo("permission_with_literal_label", 0);
    assertThat(permission.labelRes).isEqualTo(0);
    assertThat(permission.nonLocalizedLabel).isEqualTo("Literal label");
    assertThat(permission.protectionLevel).isEqualTo(PermissionInfo.PROTECTION_NORMAL);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithPermissions.xml")
  public void getPermissionInfo_withMinimalFields() throws Exception {
    PermissionInfo permission = packageManager.getPermissionInfo("permission_with_minimal_fields", 0);
    assertThat(permission.labelRes).isEqualTo(0);
    assertThat(permission.descriptionRes).isEqualTo(0);
    assertThat(permission.protectionLevel).isEqualTo(PermissionInfo.PROTECTION_NORMAL);
  }

  @Test
  public void getPermissionInfo_addedPermissions() throws Exception {
    PermissionInfo permissionInfo = new PermissionInfo();
    permissionInfo.name = "manually_added_permission";
    rpm.addPermissionInfo(permissionInfo);
    PermissionInfo permission = packageManager.getPermissionInfo("manually_added_permission", 0);
    assertThat(permission.name).isEqualTo("manually_added_permission");
  }
}
