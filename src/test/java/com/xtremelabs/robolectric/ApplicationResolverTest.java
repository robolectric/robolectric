package com.xtremelabs.robolectric;

import android.app.Application;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.newConfig;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ApplicationResolverTest {

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
        new ApplicationResolver(newConfig("TestAndroidManifestWithBadAppName.xml")).resolveApplication();
    }

    @Test
    public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName() throws Exception {
        assertEquals(Application.class,
                new ApplicationResolver(newConfig("TestAndroidManifest.xml")).resolveApplication().getClass());
    }

    @Test
    public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
        assertEquals(TestApplication.class,
                new ApplicationResolver(newConfig("TestAndroidManifestWithAppName.xml")).resolveApplication().getClass());
    }

    @Test
    public void shouldAssignThePackageNameFromTheManifest() throws Exception {
        Application application = new ApplicationResolver(newConfig("TestAndroidManifestWithPackageName.xml")).resolveApplication();
        assertEquals("com.wacka.wa", application.getPackageName());
    }

}
