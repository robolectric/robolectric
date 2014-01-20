package org.robolectric.res;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowDrawable;
import org.robolectric.test.TemporaryFolder;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.util.TestUtil.resourceFile;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RobolectricPackageManagerTest {

  private static final String TEST_PACKAGE_NAME = "com.some.other.package";
  private static final String TEST_PACKAGE_LABEL = "My Little App";

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  RobolectricPackageManager rpm;

  @Before
  public void setUp() throws Exception {
    rpm = (RobolectricPackageManager) Robolectric.application.getPackageManager();
  }

  @Test
  public void getApplicationInfo__ThisApplication() throws Exception {
    ApplicationInfo info = rpm.getApplicationInfo(Robolectric.application.getPackageName(), 0);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(Robolectric.application.getPackageName());
  }

  @Test
  public void getApplicationInfo__OtherApplication() throws Exception {
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
  public void queryIntentActivities__EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> activities = rpm.queryIntentActivities(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  public void queryIntentActivities__Match() throws Exception {
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
  public void resolveActivity__Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    rpm.addResolveInfoForIntent(i, info);

    assertThat(rpm.resolveActivity(i, 0)).isSameAs(info);
  }

  @Test
  public void resolveActivity__NoMatch() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName("foo.bar", "No Activity"));
    assertThat(rpm.resolveActivity(i, 0)).isNull();
  }

  @Test
  public void queryIntentServices__EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> activities = rpm.queryIntentServices(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  public void queryIntentServices__Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    rpm.addResolveInfoForIntent(i, info);

    List<ResolveInfo> activities = rpm.queryIntentServices(i, 0);
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).nonLocalizedLabel.toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void queryBroadcastReceivers__EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> broadCastReceivers = rpm.queryBroadcastReceivers(i, 0);
    assertThat(broadCastReceivers).isEmpty();
  }

  @Test
  public void queryBroadcastReceivers__Match() throws Exception {
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
  public void resolveService__Match() throws Exception {
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
  public void resolveService__NoMatch() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName("foo.bar", "No Activity"));
    assertThat(rpm.resolveService(i, 0)).isNull();
  }

  @Test
  public void queryActivityIcons__Match() throws Exception {
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
  @Config(manifest = "src/test/resources/TestAndroidManifestWithReceivers.xml")
  public void testReceiverInfo() throws Exception {
    rpm.addManifest(Robolectric.getShadowApplication().getAppManifest(), Robolectric.getShadowApplication().getResourceLoader());
    ActivityInfo info = rpm.getReceiverInfo(new ComponentName(Robolectric.getShadowApplication().getApplicationContext(), ".test.ConfigTestReceiver"), PackageManager.GET_META_DATA);
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

    metaValue = meta.get("org.robolectric.metaStringRes");
    assertTrue(Integer.class.isInstance(metaValue));
    assertEquals(R.string.app_name, metaValue);
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
    List<IntentFilter> filters = new ArrayList<IntentFilter>();
    filters.add(filter);

    List<ComponentName> activities = new ArrayList<ComponentName>();
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
    rpm.addManifest(appManifest, Robolectric.getShadowApplication().getResourceLoader());
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
    Robolectric.packageManager.addResolveInfoForIntent(launchIntent, resolveInfo);

    intent = rpm.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent).isNotNull();
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithAppMetaData.xml")
  public void shouldAssignTheAppMetaDataFromTheManifest() throws Exception {
    String packageName = Robolectric.getShadowApplication().getAppManifest().getPackageName();
    ApplicationInfo info = Robolectric.getShadowApplication().getPackageManager().getApplicationInfo(packageName, 0);
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
    Robolectric.packageManager.addResolveInfoForIntent(intent1, resolveInfo);
    
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
    Robolectric.packageManager.addResolveInfoForIntent(intent1, resolveInfo);

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
  public void shouldAssignLabelResFromTheManifest() throws Exception {
    rpm.addManifest(Robolectric.getShadowApplication().getAppManifest(), Robolectric.getShadowApplication().getResourceLoader());
    ApplicationInfo applicationInfo = rpm.getApplicationInfo("org.robolectric", 0);
    String appName = Robolectric.getShadowApplication().getApplicationContext().getString(applicationInfo.labelRes);
    assertThat(appName).isEqualTo("Testing App");
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
