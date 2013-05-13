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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowDrawable;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class RobolectricPackageManagerTest {

  private static final String TEST_PACKAGE_NAME = "com.some.other.package";
  private static final String TEST_PACKAGE_LABEL = "My Little App";

  RobolectricPackageManager rpm;

  @Before
  public void setUp() throws Exception {
    rpm = (RobolectricPackageManager) Robolectric.application.getPackageManager();
  }

  @After
  public void tearDown() throws Exception {
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
    Intent i = rpm.getLaunchIntentForPackage(TEST_PACKAGE_NAME);
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
}
