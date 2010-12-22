package com.xtremelabs.robolectric;

import android.app.Application;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ApplicationResolverTest {

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
        new ApplicationResolver(resourceFile("TestAndroidManifestWithBadAppName.xml")).resolveApplication();
    }

    @Test
    public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName() throws Exception {
        assertEquals(Application.class,
                new ApplicationResolver(resourceFile("TestAndroidManifest.xml")).resolveApplication().getClass());
    }

    @Test
    public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
        assertEquals(TestApplication.class,
                new ApplicationResolver(resourceFile("TestAndroidManifestWithAppName.xml")).resolveApplication().getClass());
    }

    @Test
    public void shouldAssignThePackageNameFromTheManifest() throws Exception {
        Application application = new ApplicationResolver(resourceFile("TestAndroidManifestWithPackageName.xml")).resolveApplication();
        assertEquals("com.wacka.wa", application.getPackageName());
    }
}
