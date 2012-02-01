package com.xtremelabs.robolectric.res;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

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
		assertThat( info, notNullValue() );
		assertThat( info.packageName, equalTo(Robolectric.application.getPackageName()));
	}
	
	@Test
	public void getApplicationInfo__OtherApplication() throws Exception {
		PackageInfo packageInfo = new PackageInfo();
		packageInfo.packageName = TEST_PACKAGE_NAME;
		packageInfo.applicationInfo = new ApplicationInfo();
		packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
		packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
		rpm.addPackage( packageInfo );
		
		ApplicationInfo info = rpm.getApplicationInfo(TEST_PACKAGE_NAME, 0);
		assertThat(info, notNullValue() );
		assertThat(info.packageName, equalTo(TEST_PACKAGE_NAME));
		assertThat(rpm.getApplicationLabel(info).toString(), equalTo(TEST_PACKAGE_LABEL));
	}
	
}
