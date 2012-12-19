package com.xtremelabs.robolectric.res;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.ShadowDrawable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
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
        assertThat(info, notNullValue());
        assertThat(info.packageName, equalTo(Robolectric.application.getPackageName()));
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
        assertThat(info, notNullValue());
        assertThat(info.packageName, equalTo(TEST_PACKAGE_NAME));
        assertThat(rpm.getApplicationLabel(info).toString(), equalTo(TEST_PACKAGE_LABEL));
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
        assertThat(activities, notNullValue());        // empty list, not null
        assertThat(activities.size(), equalTo(0));
    }

    @Test
    public void queryIntentActivities__Match() throws Exception {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        ResolveInfo info = new ResolveInfo();
        info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

        rpm.addResolveInfoForIntent(i, info);

        List<ResolveInfo> activities = rpm.queryIntentActivities(i, 0);
        assertThat(activities, notNullValue());
        assertThat(activities.size(), equalTo(1));
        assertThat(activities.get(0).nonLocalizedLabel.toString(), equalTo(TEST_PACKAGE_LABEL));
    }

    @Test
    public void resolveActivity__Match() throws Exception {
        Intent i = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        ResolveInfo info = new ResolveInfo();
        info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
        rpm.addResolveInfoForIntent(i, info);

        assertThat(rpm.resolveActivity(i, 0), sameInstance(info));
    }

    @Test
    public void resolveActivity__NoMatch() throws Exception {
        Intent i = new Intent();
        i.setComponent(new ComponentName("foo.bar", "No Activity"));
        assertThat(rpm.resolveActivity(i, 0), nullValue());
    }

    @Test
    public void resolveService__Match() throws Exception {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        ResolveInfo info = new ResolveInfo();
        info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
        rpm.addResolveInfoForIntent(i, info);

        assertThat(rpm.resolveService(i, 0), sameInstance(info));
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

        assertThat(rpm.resolveActivity(intent, 0), nullValue());
    }

    @Test
    public void resolveService__NoMatch() throws Exception {
        Intent i = new Intent();
        i.setComponent(new ComponentName("foo.bar", "No Activity"));
        assertThat(rpm.resolveService(i, 0), nullValue());
    }

    @Test
    public void queryActivityIcons__Match() throws Exception {
        Intent i = rpm.getLaunchIntentForPackage(TEST_PACKAGE_NAME);
        Drawable d = new BitmapDrawable();

        rpm.addActivityIcon(i, d);

        assertThat(rpm.getActivityIcon(i), sameInstance(d));
        assertThat(rpm.getActivityIcon(i.getComponent()), sameInstance(d));
    }

    @Test
    public void hasSystemFeature() throws Exception {
        // uninitialized
        assertThat(rpm.hasSystemFeature(PackageManager.FEATURE_CAMERA), equalTo(false));

        // positive
        rpm.setSystemFeature(PackageManager.FEATURE_CAMERA, true);
        assertThat(rpm.hasSystemFeature(PackageManager.FEATURE_CAMERA), equalTo(true));

        // negative
        rpm.setSystemFeature(PackageManager.FEATURE_CAMERA, false);
        assertThat(rpm.hasSystemFeature(PackageManager.FEATURE_CAMERA), equalTo(false));
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

        assertThat(activities.size(), equalTo(1));
        assertThat(activities.get(0).getPackageName(), equalTo(packageName));

        // Test not match
        IntentFilter filter1 = new IntentFilter(Intent.ACTION_VIEW);
        filters.add(filter1);
        filters.clear();
        activities.clear();
        filters.add(filter1);

        rpm.getPreferredActivities(filters, activities, null);

        assertThat(activities.size(), equalTo(0));
    }

    @Test
    public void canResolveDrawableGivenPackageAndResourceId() throws Exception {
        Drawable drawable = ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), "my_source");
        rpm.addDrawableResolution("com.example.foo", 4334, drawable);
        Drawable actual = rpm.getDrawable("com.example.foo", 4334, null);
        assertThat(actual, sameInstance(drawable));
    }
}
