package org.robolectric.res;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowDrawable;
import org.robolectric.test.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DefaultRobolectricPackageManagerTest {
  private static final String TEST_PACKAGE_NAME = "com.some.other.package";
  private static final String TEST_PACKAGE_LABEL = "My Little App";
  private final RobolectricPackageManager rpm = RuntimeEnvironment.getRobolectricPackageManager();
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void getApplicationInfo_ThisApplication() throws Exception {
    ApplicationInfo info = rpm.getApplicationInfo(RuntimeEnvironment.application.getPackageName(), 0);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(RuntimeEnvironment.application.getPackageName());
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
    rpm.addManifest(ShadowApplication.getInstance().getAppManifest(), ShadowApplication.getInstance().getResourceLoader());
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    rpm.setQueryIntentImplicitly(true);
    List<ResolveInfo> activities = rpm.queryIntentActivities(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestForActivitiesWithIntentFilterWithData.xml")
  public void queryIntentActivities_MatchWithImplicitIntents() throws Exception {
    rpm.addManifest(ShadowApplication.getInstance().getAppManifest(), ShadowApplication.getInstance().getResourceLoader());
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
    rpm.addManifest(ShadowApplication.getInstance().getAppManifest(), ShadowApplication.getInstance().getResourceLoader());
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
    assertThat(providers.length).isEqualTo(2);
    assertThat(providers[0].packageName).isEqualTo("org.robolectric");
    assertThat(providers[1].packageName).isEqualTo("org.robolectric");
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
    ShadowApplication app = ShadowApplication.getInstance();
    rpm.addManifest(app.getAppManifest(), ShadowApplication.getInstance().getResourceLoader());
    ActivityInfo info = rpm.getReceiverInfo(new ComponentName(app.getApplicationContext(), ".test.ConfigTestReceiver"), PackageManager.GET_META_DATA);
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
    assertEquals(app.getResources().getBoolean(R.bool.false_bool_value), metaValue);

    metaValue = meta.get("org.robolectric.metaIntFromRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(app.getResources().getInteger(R.integer.test_integer1), metaValue);

    metaValue = meta.get("org.robolectric.metaColorFromRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(app.getResources().getColor(R.color.clear), metaValue);

    metaValue = meta.get("org.robolectric.metaStringFromRes");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals(app.getString(R.string.app_name), metaValue);

    metaValue = meta.get("org.robolectric.metaStringOfIntFromRes");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals(app.getString(R.string.str_int), metaValue);

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
    PackageInfo receiverInfos = rpm.getPackageInfo("unknown_package", PackageManager.GET_RECEIVERS);
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
  public void shouldAssignTheApplicationNameFromTheManifest() throws Exception {
    AndroidManifest appManifest = newConfigWith("<application android:name=\"org.robolectric.TestApplication\"/>");
    rpm.addManifest(appManifest, ShadowApplication.getInstance().getResourceLoader());
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
    String appName = app.getString(R.string.app_name);
    String packageName = app.getAppManifest().getPackageName();
    ApplicationInfo info = app.getPackageManager().getApplicationInfo(packageName, 0);
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
    assertEquals(app.getResources().getBoolean(R.bool.false_bool_value), metaValue);

    metaValue = meta.get("org.robolectric.metaIntFromRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(app.getResources().getInteger(R.integer.test_integer1), metaValue);

    metaValue = meta.get("org.robolectric.metaColorFromRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(app.getResources().getColor(R.color.clear), metaValue);

    metaValue = meta.get("org.robolectric.metaStringFromRes");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals(app.getString(R.string.app_name), metaValue);

    metaValue = meta.get("org.robolectric.metaStringOfIntFromRes");
    assertTrue(String.class.isInstance(metaValue));
    assertEquals(app.getString(R.string.str_int), metaValue);

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
    PackageManager packageManager = RuntimeEnvironment.getPackageManager();

    assertThat(packageManager.getApplicationEnabledSetting("org.robolectric")).isEqualTo(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

    packageManager.setApplicationEnabledSetting("org.robolectric", PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

    assertThat(packageManager.getApplicationEnabledSetting("org.robolectric")).isEqualTo(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
  }

  public static class ActivityWithMetadata extends Activity { }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void getActivityMetaData() throws Exception {
    Activity activity = setupActivity(ActivityWithMetadata.class);

    ActivityInfo activityInfo = RuntimeEnvironment.getPackageManager().getActivityInfo(activity.getComponentName(), PackageManager.GET_ACTIVITIES|PackageManager.GET_META_DATA);
    assertThat(activityInfo.metaData.get("someName")).isEqualTo("someValue");
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifest.xml")
  public void shouldAssignLabelResFromTheManifest() throws Exception {
    rpm.addManifest(ShadowApplication.getInstance().getAppManifest(), ShadowApplication.getInstance().getResourceLoader());
    ApplicationInfo applicationInfo = rpm.getApplicationInfo("org.robolectric", 0);
    String appName = ShadowApplication.getInstance().getApplicationContext().getString(applicationInfo.labelRes);
    assertThat(appName).isEqualTo("Testing App");
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
  
  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldThrowNameNotFoundExceptionIfNotExist() throws Exception {
    ComponentName nonExistComponent = new ComponentName("org.robolectric", "com.foo.NonExistService");
    try {
      rpm.getServiceInfo(nonExistComponent, PackageManager.GET_SERVICES);
    } catch (NameNotFoundException e) {
      return;
    }
    fail("NameNotFoundException is expected.");
  }

  /////////////////////////////

  public AndroidManifest newConfigWith(String contents) throws IOException {
    return newConfigWith("org.robolectric", contents);
  }

  private AndroidManifest newConfigWith(String packageName, String contents) throws IOException {
    File f = temporaryFolder.newFile("whatever.xml",
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "          package=\"" + packageName + "\">\n" +
            "    " + contents + "\n" +
            "</manifest>\n");
    return new AndroidManifest(Fs.newFile(f), null, null);
  }
}
