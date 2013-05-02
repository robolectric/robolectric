package org.robolectric;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;

public class NonApkTestRunnerTest {

	public static final class FakeTestCase1 {
		@Test
		public void fakeTest() {}
	}
	
	@NonApkTestRunner.Manifest(packageName="org.robolectric.test",minSdkVersion=11,targetSdkVersion=14)
	public static final class FakeTestCase2 {
		@Test
		public void fakeTest() {}
	}
	
	@Test
	public void testGenApkDir1() throws InitializationError {
		NonApkTestRunner runner = new NonApkTestRunner(FakeTestCase1.class);
		AndroidManifest appManifest = runner.getAppManifest(new Config.Implementation(1,null,1,null));
		Assert.assertEquals(FakeTestCase1.class.getPackage().getName(), appManifest.getPackageName());
		Assert.assertEquals(1, appManifest.getMinSdkVersion());
		Assert.assertEquals(1, appManifest.getTargetSdkVersion());
		Assert.assertEquals(appManifest.getPackageName(), appManifest.getBaseDir().getName());
		runner.run(new RunNotifier());
	}
	
	@Test
	public void testGenApkDir2() throws InitializationError {
		NonApkTestRunner runner = new NonApkTestRunner(FakeTestCase2.class);
		AndroidManifest appManifest = runner.getAppManifest(new Config.Implementation(1,null,1,null));
		Assert.assertEquals(11, appManifest.getMinSdkVersion());
		Assert.assertEquals(14, appManifest.getTargetSdkVersion());
		Assert.assertEquals("org.robolectric.test", appManifest.getPackageName());
		Assert.assertEquals(appManifest.getPackageName(), appManifest.getBaseDir().getName());
		runner.run(new RunNotifier());
	}
}
