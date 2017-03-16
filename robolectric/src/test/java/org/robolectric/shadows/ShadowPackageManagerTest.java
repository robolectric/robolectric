package org.robolectric.shadows;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.*;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.test.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.*;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Robolectric.setupActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowPackageManagerTest {

  private static final String TEST_PACKAGE_NAME = "com.some.other.package";
  private static final String TEST_PACKAGE_LABEL = "My Little App";
  private static final String TEST_APP_PATH = "/values/app/application.apk";
  private ShadowPackageManager shadowPackageManager;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private PackageManager packageManager;

  private final IPackageStatsObserver packageStatsObserver = mock(IPackageStatsObserver.class);
  private final ArgumentCaptor<PackageStats> packageStatsCaptor = ArgumentCaptor.forClass(PackageStats.class);

  @Before
  public void setUp() {
    shadowPackageManager = shadowOf(RuntimeEnvironment.application.getPackageManager());
    packageManager = RuntimeEnvironment.application.getPackageManager();
  }

  @Test
  public void getPackageArchiveInfo() {
    ApplicationInfo appInfo = new ApplicationInfo();
    appInfo.flags = 0;
    appInfo.packageName = TEST_PACKAGE_NAME;
    appInfo.sourceDir = TEST_APP_PATH;
    appInfo.name = TEST_PACKAGE_LABEL;

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = appInfo;
    shadowPackageManager.addPackage(packageInfo);

    PackageInfo packageInfoResult = shadowPackageManager.getPackageArchiveInfo(TEST_APP_PATH, 0);
    assertThat(packageInfoResult).isNotNull();
    ApplicationInfo applicationInfo = packageInfoResult.applicationInfo;
    assertThat(applicationInfo).isInstanceOf(ApplicationInfo.class);
    assertThat(applicationInfo.packageName).isEqualTo(TEST_PACKAGE_NAME);
    assertThat(applicationInfo.sourceDir).isEqualTo(TEST_APP_PATH);

  }

  @Test
  public void getApplicationInfo_ThisApplication() throws Exception {
    ApplicationInfo info = shadowPackageManager.getApplicationInfo(RuntimeEnvironment.application.getPackageName(), 0);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(RuntimeEnvironment.application.getPackageName());
  }

  @Test(expected = PackageManager.NameNotFoundException.class)
  public void getApplicationInfo_whenUnknown_shouldThrowNameNotFoundException() throws Exception {
    try {
      shadowPackageManager.getApplicationInfo("unknown_package", 0);
      fail("should have thrown NameNotFoundException");
    } catch (PackageManager.NameNotFoundException e) {
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
    shadowPackageManager.addPackage(packageInfo);

    ApplicationInfo info = shadowPackageManager.getApplicationInfo(TEST_PACKAGE_NAME, 0);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(TEST_PACKAGE_NAME);
    assertThat(shadowPackageManager.getApplicationLabel(info).toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test(expected = PackageManager.NameNotFoundException.class)
  public void removePackage_shouldHideItFromGetApplicationInfo() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    shadowPackageManager.addPackage(packageInfo);
    shadowPackageManager.removePackage(TEST_PACKAGE_NAME);

    shadowPackageManager.getApplicationInfo(TEST_PACKAGE_NAME, 0);
  }

  @Test
  public void queryIntentActivities_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> activities = shadowPackageManager.queryIntentActivities(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  public void queryIntentActivities_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    shadowPackageManager.addResolveInfoForIntent(i, info);

    List<ResolveInfo> activities = shadowPackageManager.queryIntentActivities(i, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).nonLocalizedLabel.toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestForActivitiesWithIntentFilterWithData.xml")
  public void queryIntentActivities_EmptyResultWithNoMatchingImplicitIntents() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    shadowPackageManager.setQueryIntentImplicitly(true);
    List<ResolveInfo> activities = shadowPackageManager.queryIntentActivities(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestForActivitiesWithIntentFilterWithData.xml")
  public void queryIntentActivities_MatchWithImplicitIntents() throws Exception {
    Uri uri = Uri.parse("content://testhost1.com:1/testPath/test.jpeg");
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.addCategory(Intent.CATEGORY_DEFAULT);
    i.setDataAndType(uri, "image/jpeg");

    shadowPackageManager.setQueryIntentImplicitly(true);
    List<ResolveInfo> activities = shadowPackageManager.queryIntentActivities(i, 0);
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

    shadowPackageManager.setQueryIntentImplicitly(true);
    List<ResolveInfo> activities = shadowPackageManager.queryIntentActivities(i, 0);
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
    shadowPackageManager.addResolveInfoForIntent(i, info);

    assertThat(shadowPackageManager.resolveActivity(i, 0)).isSameAs(info);
  }

  @Test
  public void resolveActivity_NoMatch() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName("foo.bar", "No Activity"));
    assertThat(shadowPackageManager.resolveActivity(i, 0)).isNull();
  }

  @Test
  public void queryIntentServices_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> activities = shadowPackageManager.queryIntentServices(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  public void queryIntentServices_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    shadowPackageManager.addResolveInfoForIntent(i, info);

    List<ResolveInfo> activities = shadowPackageManager.queryIntentServices(i, 0);
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).nonLocalizedLabel.toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void queryBroadcastReceivers_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> broadCastReceivers = shadowPackageManager.queryBroadcastReceivers(i, 0);
    assertThat(broadCastReceivers).isEmpty();
  }

  @Test
  public void queryBroadcastReceivers_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    shadowPackageManager.addResolveInfoForIntent(i, info);

    List<ResolveInfo> broadCastReceivers = shadowPackageManager.queryBroadcastReceivers(i, 0);
    assertThat(broadCastReceivers).hasSize(1);
    assertThat(broadCastReceivers.get(0).nonLocalizedLabel.toString())
        .isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void resolveService_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    ResolveInfo info = new ResolveInfo();
    shadowPackageManager.addResolveInfoForIntent(i, info);
    assertThat(shadowPackageManager.resolveService(i, 0)).isSameAs(info);
  }

  @Test
  public void removeResolveInfosForIntent_shouldCauseResolveActivityToReturnNull() throws Exception {
    Intent intent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    info.activityInfo = new ActivityInfo();
    info.activityInfo.packageName = "com.org";
    shadowPackageManager.addResolveInfoForIntent(intent, info);

    shadowPackageManager.removeResolveInfosForIntent(intent, "com.org");

    assertThat(shadowPackageManager.resolveActivity(intent, 0)).isNull();
  }

  @Test
  public void resolveService_NoMatch() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName("foo.bar", "No Activity"));
    assertThat(shadowPackageManager.resolveService(i, 0)).isNull();
  }

  @Test
  public void queryActivityIcons_Match() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName(TEST_PACKAGE_NAME, ""));
    Drawable d = new BitmapDrawable();

    shadowPackageManager.addActivityIcon(i, d);

    assertThat(shadowPackageManager.getActivityIcon(i)).isSameAs(d);
    assertThat(shadowPackageManager.getActivityIcon(i.getComponent())).isSameAs(d);
  }

  @Test
  public void hasSystemFeature() throws Exception {
    // uninitialized
    assertThat(shadowPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)).isFalse();

    // positive
    shadowPackageManager.setSystemFeature(PackageManager.FEATURE_CAMERA, true);
    assertThat(shadowPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)).isTrue();

    // negative
    shadowPackageManager.setSystemFeature(PackageManager.FEATURE_CAMERA, false);
    assertThat(shadowPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)).isFalse();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithContentProviders.xml")
  public void getPackageInfo_getProvidersShouldReturnProviderInfos() throws Exception {
    PackageInfo packageInfo = shadowPackageManager.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_PROVIDERS);
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
    ProviderInfo providerInfo1 = packageManager.getProviderInfo(new ComponentName(RuntimeEnvironment.application, ".tester.FullyQualifiedClassName"), 0);
    assertThat(providerInfo1.packageName).isEqualTo("org.robolectric");
    assertThat(providerInfo1.authority).isEqualTo("org.robolectric.authority1");

    ProviderInfo providerInfo2 = packageManager.getProviderInfo(new ComponentName(RuntimeEnvironment.application, "org.robolectric.tester.PartiallyQualifiedClassName"), 0);
    assertThat(providerInfo2.packageName).isEqualTo("org.robolectric");
    assertThat(providerInfo2.authority).isEqualTo("org.robolectric.authority2");
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
  @Config(manifest = "src/test/resources/TestAndroidManifestWithContentProviders.xml")
  public void resolveContentProvider_shouldResolveByPackageName() throws Exception {
    ProviderInfo providerInfo = packageManager.resolveContentProvider("org.robolectric.authority1", 0);
    assertThat(providerInfo.packageName).isEqualTo("org.robolectric");
    assertThat(providerInfo.authority).isEqualTo("org.robolectric.authority1");
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithNoContentProviders.xml")
  public void getPackageInfo_getProvidersShouldReturnNullOnNoProviders() throws Exception {
    PackageInfo packageInfo = shadowPackageManager.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_PROVIDERS);
    ProviderInfo[] providers = packageInfo.providers;
    assertThat(providers).isNull();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void testReceiverInfo() throws Exception {
    ActivityInfo info = shadowPackageManager.getReceiverInfo(new ComponentName(RuntimeEnvironment.application, ".test.ConfigTestReceiver"), PackageManager.GET_META_DATA);
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
    assertEquals(PackageManager.PERMISSION_GRANTED, shadowPackageManager.checkPermission("android.permission.INTERNET", RuntimeEnvironment.application.getPackageName()));
    assertEquals(PackageManager.PERMISSION_GRANTED, shadowPackageManager.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", RuntimeEnvironment.application.getPackageName()));
    assertEquals(PackageManager.PERMISSION_GRANTED, shadowPackageManager.checkPermission("android.permission.GET_TASKS", RuntimeEnvironment.application.getPackageName()));

    assertEquals(PackageManager.PERMISSION_DENIED, shadowPackageManager.checkPermission("android.permission.ACCESS_FINE_LOCATION", RuntimeEnvironment.application.getPackageName()));
    assertEquals(PackageManager.PERMISSION_DENIED, shadowPackageManager.checkPermission("android.permission.ACCESS_FINE_LOCATION", "random-package"));
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void testQueryBroadcastReceiverSucceeds() {
    Intent intent = new Intent("org.robolectric.ACTION_RECEIVER_PERMISSION_PACKAGE");
    intent.setPackage(RuntimeEnvironment.application.getPackageName());

    List<ResolveInfo> receiverInfos = shadowPackageManager.queryBroadcastReceivers(intent, PackageManager.GET_INTENT_FILTERS);
    assertTrue(receiverInfos.size() == 1);
    assertEquals("org.robolectric.ConfigTestReceiverPermissionsAndActions", receiverInfos.get(0).activityInfo.name);
    assertEquals("org.robolectric.CUSTOM_PERM", receiverInfos.get(0).activityInfo.permission);
    assertEquals("org.robolectric.ACTION_RECEIVER_PERMISSION_PACKAGE", receiverInfos.get(0).filter.getAction(0));
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void testQueryBroadcastReceiverFailsForMissingPackageName() {
    Intent intent = new Intent("org.robolectric.ACTION_ONE_MORE_PACKAGE");
    List<ResolveInfo> receiverInfos = shadowPackageManager.queryBroadcastReceivers(intent, PackageManager.GET_INTENT_FILTERS);
    assertTrue(receiverInfos.size() == 0);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void testQueryBroadcastReceiverFailsForMissingAction() {
    Intent intent = new Intent();
    intent.setPackage(RuntimeEnvironment.application.getPackageName());
    List<ResolveInfo> receiverInfos = shadowPackageManager.queryBroadcastReceivers(intent, PackageManager.GET_INTENT_FILTERS);
    assertTrue(receiverInfos.size() == 0);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceiversCustomPackage.xml")
  public void testGetPackageInfo_ForReceiversSucceeds() throws Exception {
    PackageInfo receiverInfos = shadowPackageManager.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_RECEIVERS);

    assertEquals(1, receiverInfos.receivers.length);
    assertEquals("org.robolectric.ConfigTestReceiverPermissionsAndActions", receiverInfos.receivers[0].name);
    assertEquals("org.robolectric.CUSTOM_PERM", receiverInfos.receivers[0].permission);
  }

  @Test(expected = PackageManager.NameNotFoundException.class)
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceiversCustomPackage.xml")
  public void testGetPackageInfo_ForReceiversIncorrectPackage() throws Exception {
    try {
      shadowPackageManager.getPackageInfo("unknown_package", PackageManager.GET_RECEIVERS);
      fail("should have thrown NameNotFoundException");
    } catch (PackageManager.NameNotFoundException e) {
      assertThat(e.getMessage()).contains("unknown_package");
      throw e;
    }
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithPermissions.xml")
  public void getPackageInfo_shouldReturnRequestedPermissions() throws Exception {
    PackageInfo packageInfo = shadowPackageManager.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_PERMISSIONS);
    String[] permissions = packageInfo.requestedPermissions;
    assertThat(permissions).isNotNull();
    assertThat(permissions.length).isEqualTo(3);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithoutPermissions.xml")
  public void getPackageInfo_shouldReturnNullOnNoRequestedPermissions() throws Exception {
    PackageInfo packageInfo = shadowPackageManager.getPackageInfo(RuntimeEnvironment.application.getPackageName(), PackageManager.GET_PERMISSIONS);
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
    shadowPackageManager.addPreferredActivity(filter, 0, null, name);

    // Test match
    List<IntentFilter> filters = new ArrayList<>();
    filters.add(filter);

    List<ComponentName> activities = new ArrayList<>();
    shadowPackageManager.getPreferredActivities(filters, activities, null);

    assertThat(activities.size()).isEqualTo(1);
    assertThat(activities.get(0).getPackageName()).isEqualTo(packageName);

    // Test not match
    IntentFilter filter1 = new IntentFilter(Intent.ACTION_VIEW);
    filters.add(filter1);
    filters.clear();
    activities.clear();
    filters.add(filter1);

    shadowPackageManager.getPreferredActivities(filters, activities, null);

    assertThat(activities.size()).isEqualTo(0);
  }

  @Test
  public void canResolveDrawableGivenPackageAndResourceId() throws Exception {
    Drawable drawable = ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), "my_source");
    shadowPackageManager.addDrawableResolution("com.example.foo", 4334, drawable);
    Drawable actual = shadowPackageManager.getDrawable("com.example.foo", 4334, null);
    assertThat(actual).isSameAs(drawable);
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void shouldAssignTheApplicationNameFromTheManifest() throws Exception {
    ApplicationInfo applicationInfo = shadowPackageManager.getApplicationInfo("org.robolectric", 0);
    assertThat(applicationInfo.name).isEqualTo("org.robolectric.TestApplication");
  }

  @Test
  public void testLaunchIntentForPackage() {
    Intent intent = shadowPackageManager.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent).isNull();

    Intent launchIntent = new Intent(Intent.ACTION_MAIN);
    launchIntent.setPackage(TEST_PACKAGE_LABEL);
    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "LauncherActivity";
    shadowPackageManager.addResolveInfoForIntent(launchIntent, resolveInfo);

    intent = shadowPackageManager.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
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
    Intent intent1 = shadowPackageManager.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent1).isNull();

    intent1 = new Intent(Intent.ACTION_MAIN);
    intent1.setPackage(TEST_PACKAGE_LABEL);
    intent1.addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "LauncherActivity";
    shadowPackageManager.addResolveInfoForIntent(intent1, resolveInfo);

    // the original intent object should yield a result
    ResolveInfo result  = shadowPackageManager.resolveActivity(intent1, -1);
    assertThat(result).isNotNull();

    // AND a new, functionally equivalent intent should also yield a result
    Intent intent2 = new Intent(Intent.ACTION_MAIN);
    intent2.setPackage(TEST_PACKAGE_LABEL);
    intent2.addCategory(Intent.CATEGORY_LAUNCHER);
    result = shadowPackageManager.resolveActivity(intent2, -1);
    assertThat(result).isNotNull();
  }

  @Test
  public void testResolvePartiallySimilarIntents() {
    Intent intent1 = shadowPackageManager.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent1).isNull();

    intent1 = new Intent(Intent.ACTION_MAIN);
    intent1.setPackage(TEST_PACKAGE_LABEL);
    intent1.addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "LauncherActivity";
    shadowPackageManager.addResolveInfoForIntent(intent1, resolveInfo);

    // the original intent object should yield a result
    ResolveInfo result  = shadowPackageManager.resolveActivity(intent1, -1);
    assertThat(result).isNotNull();

    // an intent with just the same action should not be considered the same
    Intent intent2 = new Intent(Intent.ACTION_MAIN);
    result = shadowPackageManager.resolveActivity(intent2, -1);
    assertThat(result).isNull();

    // an intent with just the same category should not be considered the same
    Intent intent3 = new Intent();
    intent3.addCategory(Intent.CATEGORY_LAUNCHER);
    result = shadowPackageManager.resolveActivity(intent3, -1);
    assertThat(result).isNull();

    // an intent without the correct package restriction should not be the same
    Intent intent4 = new Intent(Intent.ACTION_MAIN);
    intent4.addCategory(Intent.CATEGORY_LAUNCHER);
    result = shadowPackageManager.resolveActivity(intent4, -1);
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

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void shouldAssignLabelResFromTheManifest() throws Exception {
    ApplicationInfo applicationInfo = shadowPackageManager.getApplicationInfo("org.robolectric", 0);
    assertThat(applicationInfo.labelRes).isEqualTo(R.string.app_name);
    assertThat(applicationInfo.nonLocalizedLabel).isNull();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithAppMetaData.xml")
  public void shouldAssignNonLocalizedLabelFromTheManifest() throws Exception {
    ApplicationInfo applicationInfo = shadowPackageManager.getApplicationInfo("org.robolectric", 0);
    assertThat(applicationInfo.labelRes).isEqualTo(0);
    assertThat(applicationInfo.nonLocalizedLabel).isEqualTo("App Label");
  }

  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldReturnServiceInfoIfExists() throws Exception {
    ServiceInfo serviceInfo = shadowPackageManager.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_SERVICES);
    assertEquals(serviceInfo.packageName, "org.robolectric");
    assertEquals(serviceInfo.name, "com.foo.Service");
    assertEquals(serviceInfo.permission, "com.foo.MY_PERMISSION");
    assertNotNull(serviceInfo.applicationInfo);
  }

  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldReturnServiceInfoWithMetaDataWhenFlagsSet() throws Exception {
    ServiceInfo serviceInfo = shadowPackageManager.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_META_DATA);
    assertNotNull(serviceInfo.metaData);
  }

  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldReturnServiceInfoWithoutMetaDataWhenFlagsNotSet() throws Exception {
    ServiceInfo serviceInfo = shadowPackageManager.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_SERVICES);
    assertNull(serviceInfo.metaData);
  }

  @Test(expected = PackageManager.NameNotFoundException.class)
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldThrowNameNotFoundExceptionIfNotExist() throws Exception {
    ComponentName nonExistComponent = new ComponentName("org.robolectric", "com.foo.NonExistService");
    try {
      shadowPackageManager.getServiceInfo(nonExistComponent, PackageManager.GET_SERVICES);
      fail("should have thrown NameNotFoundException");
    } catch (PackageManager.NameNotFoundException e) {
      assertThat(e.getMessage()).contains("com.foo.NonExistService");
      throw e;
    }
  }

  @Test
  public void getNameForUid() {
    assertThat(packageManager.getNameForUid(10)).isNull();

    shadowPackageManager.setNameForUid(10, "a_name");

    assertThat(packageManager.getNameForUid(10)).isEqualTo("a_name");
  }

  @Test
  public void getPackagesForUid() {
    assertThat(packageManager.getPackagesForUid(10)).isNull();

    shadowPackageManager.setPackagesForUid(10, new String[] {"a_name"});

    assertThat(packageManager.getPackagesForUid(10)).containsExactly("a_name");
  }

  @Test
  public void getResourcesForApplication_currentApplication() throws Exception {
    assertThat(packageManager.getResourcesForApplication("org.robolectric").getString(R.string.app_name))
        .isEqualTo(RuntimeEnvironment.application.getString(R.string.app_name));
  }

  @Test(expected = PackageManager.NameNotFoundException.class)
  public void getResourcesForApplication_unknownPackage() throws Exception {
    packageManager.getResourcesForApplication("non.existent.package");
  }

  @Test @Config(minSdk = M)
  public void shouldShowRequestPermissionRationale() {
    assertThat(packageManager.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)).isFalse();

    shadowPackageManager.setShouldShowRequestPermissionRationale(Manifest.permission.CAMERA, true);

    assertThat(packageManager.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)).isTrue();
  }

  @Test
  public void getSystemAvailableFeatures() {
    assertThat(packageManager.getSystemAvailableFeatures()).isNull();

    FeatureInfo feature = new FeatureInfo();
    feature.reqGlEsVersion = 0x20000;
    feature.flags = FeatureInfo.FLAG_REQUIRED;
    shadowPackageManager.addSystemAvailableFeature(feature);

    assertThat(packageManager.getSystemAvailableFeatures()).contains(feature);

    shadowPackageManager.clearSystemAvailableFeatures();

    assertThat(packageManager.getSystemAvailableFeatures()).isNull();
  }

  @Test
  public void verifyPendingInstall() {
    packageManager.verifyPendingInstall(1234, VERIFICATION_ALLOW);

    assertThat(shadowPackageManager.getVerificationResult(1234)).isEqualTo(VERIFICATION_ALLOW);
  }

  @Test
  @Config(minSdk = N)
  public void whenPackageNotPresent_getPackageSizeInfo_callsBackWithFailure() throws Exception {
    packageManager.getPackageSizeInfo("nonexistant.package", packageStatsObserver);

    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(false));
    assertThat(packageStatsCaptor.getValue()).isNull();
  }

  @Test
  @Config(minSdk = N)
  public void whenPackageNotPresentAndPaused_getPackageSizeInfo_callsBackWithFailure() throws Exception {
    Robolectric.getForegroundThreadScheduler().pause();

    packageManager.getPackageSizeInfo("nonexistant.package", packageStatsObserver);

    verifyZeroInteractions(packageStatsObserver);

    Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(false));
    assertThat(packageStatsCaptor.getValue()).isNull();
  }

  @Test
  @Config(minSdk = N)
  public void whenNotPreconfigured_getPackageSizeInfo_callsBackWithDefaults() throws Exception {
    packageManager.getPackageSizeInfo("org.robolectric", packageStatsObserver);

    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(true));
    assertThat(packageStatsCaptor.getValue().packageName).isEqualTo("org.robolectric");
  }

  @Test
  @Config(minSdk = N)
  public void whenPreconfigured_getPackageSizeInfo_callsBackWithConfiguredValues() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "org.robolectric";
    PackageStats packageStats = new PackageStats("org.robolectric");
    shadowPackageManager.addPackage(packageInfo, packageStats);

    packageManager.getPackageSizeInfo("org.robolectric", packageStatsObserver);

    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(true));
    assertThat(packageStatsCaptor.getValue().packageName).isEqualTo("org.robolectric");
    assertThat(packageStatsCaptor.getValue().toString()).isEqualTo(packageStats.toString());
  }

  @Test
  @Config(minSdk = N)
  public void whenPreconfiguredForAnotherPackage_getPackageSizeInfo_callsBackWithConfiguredValues() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "org.other";
    PackageStats packageStats = new PackageStats("org.other");
    shadowPackageManager.addPackage(packageInfo, packageStats);

    packageManager.getPackageSizeInfo("org.other", packageStatsObserver);

    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(true));
    assertThat(packageStatsCaptor.getValue().packageName).isEqualTo("org.other");
    assertThat(packageStatsCaptor.getValue().toString()).isEqualTo(packageStats.toString());
  }

  @Test
  @Config(minSdk = N)
  public void whenPaused_getPackageSizeInfo_callsBackWithConfiguredValuesAfterIdle() throws Exception {
    Robolectric.getForegroundThreadScheduler().pause();

    packageManager.getPackageSizeInfo("org.robolectric", packageStatsObserver);

    verifyZeroInteractions(packageStatsObserver);

    Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(true));
    assertThat(packageStatsCaptor.getValue().packageName).isEqualTo("org.robolectric");
  }

  @Test
  public void currentToCanonicalPackageNames() {
    shadowPackageManager.addCurrentToCannonicalName("current_name_1", "cannonical_name_1");
    shadowPackageManager.addCurrentToCannonicalName("current_name_2", "cannonical_name_2");

    packageManager.currentToCanonicalPackageNames(new String[] {"current_name_1", "current_name_2"});
  }

  @Test
  public void getInstalledApplications() {
    List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);

    // Default should include the application under test
    assertThat(installedApplications).hasSize(1);
    assertThat(installedApplications.get(0).packageName).isEqualTo("org.robolectric");

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "org.other";
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = "org.other";
    shadowPackageManager.addPackage(packageInfo);

    installedApplications = packageManager.getInstalledApplications(0);
    assertThat(installedApplications).hasSize(2);
    assertThat(installedApplications.get(1).packageName).isEqualTo("org.other");
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithPermissions.xml")
  public void getPermissionInfo() throws Exception {
    PermissionInfo permission = RuntimeEnvironment.application.getPackageManager().getPermissionInfo("some_permission", 0);
    assertThat(permission.labelRes).isEqualTo(R.string.test_permission_label);
    assertThat(permission.descriptionRes).isEqualTo(R.string.test_permission_description);
    assertThat(permission.name).isEqualTo("some_permission");
  }

  @Test
  public void checkSignatures_same() throws Exception {
    shadowPackageManager.addPackage(newPackageInfo("first.package", new Signature("00000000")));
    shadowPackageManager.addPackage(newPackageInfo("second.package", new Signature("00000000")));
    assertThat(packageManager.checkSignatures("first.package", "second.package")).isEqualTo(SIGNATURE_MATCH);
  }

  @Test
  public void checkSignatures_firstNotSigned() throws Exception {
    shadowPackageManager.addPackage(newPackageInfo("first.package", (Signature[]) null));
    shadowPackageManager.addPackage(newPackageInfo("second.package", new Signature("00000000")));
    assertThat(packageManager.checkSignatures("first.package", "second.package")).isEqualTo(SIGNATURE_FIRST_NOT_SIGNED);
  }

  @Test
  public void checkSignatures_secondNotSigned() throws Exception {
    shadowPackageManager.addPackage(newPackageInfo("first.package", new Signature("00000000")));
    shadowPackageManager.addPackage(newPackageInfo("second.package", (Signature[]) null));
    assertThat(packageManager.checkSignatures("first.package", "second.package")).isEqualTo(SIGNATURE_SECOND_NOT_SIGNED);
  }

  @Test
  public void checkSignatures_neitherSigned() throws Exception {
    shadowPackageManager.addPackage(newPackageInfo("first.package", (Signature[]) null));
    shadowPackageManager.addPackage(newPackageInfo("second.package", (Signature[]) null));
    assertThat(packageManager.checkSignatures("first.package", "second.package")).isEqualTo(SIGNATURE_NEITHER_SIGNED);
  }

  @Test
  public void checkSignatures_noMatch() throws Exception {
    shadowPackageManager.addPackage(newPackageInfo("first.package", new Signature("00000000")));
    shadowPackageManager.addPackage(newPackageInfo("second.package", new Signature("FFFFFFFF")));
    assertThat(packageManager.checkSignatures("first.package", "second.package")).isEqualTo(SIGNATURE_NO_MATCH);
  }

  @Test
  public void checkSignatures_noMatch_mustBeExact() throws Exception {
    shadowPackageManager.addPackage(newPackageInfo("first.package", new Signature("00000000")));
    shadowPackageManager.addPackage(newPackageInfo("second.package", new Signature("00000000"), new Signature("FFFFFFFF")));
    assertThat(packageManager.checkSignatures("first.package", "second.package")).isEqualTo(SIGNATURE_NO_MATCH);
  }

  @Test
  public void checkSignatures_unknownPackage() throws Exception {
    assertThat(packageManager.checkSignatures("first.package", "second.package")).isEqualTo(SIGNATURE_UNKNOWN_PACKAGE);
  }

  private static PackageInfo newPackageInfo(String packageName, Signature... signatures) {
    PackageInfo firstPackageInfo = new PackageInfo();
    firstPackageInfo.packageName = packageName;
    firstPackageInfo.signatures = signatures;
    return firstPackageInfo;
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
  public void getDefaultActivityIcon() {
    assertThat(packageManager.getDefaultActivityIcon()).isNotNull();
  }

  @Test
  public void addPackageShouldUseUidToProvidePackageName() throws Exception {
    PackageInfo packageInfoOne = new PackageInfo();
    packageInfoOne.packageName = "package.one";
    packageInfoOne.applicationInfo = new ApplicationInfo();
    packageInfoOne.applicationInfo.uid = 1234;
    packageInfoOne.applicationInfo.packageName = packageInfoOne.packageName;
    shadowPackageManager.addPackage(packageInfoOne);

    PackageInfo packageInfoTwo = new PackageInfo();
    packageInfoTwo.packageName = "package.two";
    packageInfoTwo.applicationInfo = new ApplicationInfo();
    packageInfoTwo.applicationInfo.uid = 1234;
    packageInfoTwo.applicationInfo.packageName = packageInfoTwo.packageName;
    shadowPackageManager.addPackage(packageInfoTwo);

    assertThat(packageManager.getPackagesForUid(1234)).containsExactlyInAnyOrder("package.one", "package.two");
  }

  @Test
  public void installerPackageName() throws Exception {
    packageManager.setInstallerPackageName("target.package", "installer.package");

    assertThat(packageManager.getInstallerPackageName("target.package")).isEqualTo("installer.package");
  }

  @Test
  public void getXml() throws Exception {
    XmlResourceParser in = packageManager.getXml(RuntimeEnvironment.application.getPackageName(),
        R.xml.dialog_preferences,
        RuntimeEnvironment.application.getApplicationInfo());
    assertThat(in).isNotNull();
  }

  @Test
  public void addPackageShouldNotCreateSessions() {

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "test.package";
    shadowPackageManager.addPackage(packageInfo);

    assertThat(packageManager.getPackageInstaller().getAllSessions()).isEmpty();
  }
}
