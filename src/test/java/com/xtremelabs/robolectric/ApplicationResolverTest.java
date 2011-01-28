package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.util.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ApplicationResolverTest {

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
        new ApplicationResolver(TestUtil.newConfig("TestAndroidManifestWithBadAppName.xml")).resolveApplication();
    }

    @Test
    public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName() throws Exception {
        assertEquals(Application.class,
                new ApplicationResolver(TestUtil.newConfig("TestAndroidManifest.xml")).resolveApplication().getClass());
    }

    @Test
    public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
        assertEquals(TestApplication.class,
                new ApplicationResolver(TestUtil.newConfig("TestAndroidManifestWithAppName.xml")).resolveApplication().getClass());
    }

    @Test
    public void shouldAssignThePackageNameFromTheManifest() throws Exception {
        Application application = new ApplicationResolver(TestUtil.newConfig("TestAndroidManifestWithPackageName.xml")).resolveApplication();
        assertEquals("com.wacka.wa", application.getPackageName());
    }

}
