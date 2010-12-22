package com.xtremelabs.robolectric;

import android.app.Application;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static java.io.File.separator;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ApplicationResolverTest {

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenManifestContainsBadApplicationClassName() throws Exception {
        new ApplicationResolver(new File("test" + separator + "TestAndroidManifestWithBadAppName.xml")).resolveApplication();
    }

    @Test
    public void shouldReturnDefaultAndroidApplicationWhenManifestDeclaresNoAppName() throws Exception {
        assertEquals(Application.class,
                new ApplicationResolver(new File("test" + separator + "TestAndroidManifest.xml")).resolveApplication().getClass());
    }

    @Test
    public void shouldReturnSpecifiedApplicationWhenManifestDeclaresAppName() throws Exception {
        assertEquals(TestApplication.class,
                new ApplicationResolver(new File("test" + separator + "TestAndroidManifestWithAppName.xml")).resolveApplication().getClass());
    }

    @Test
    public void shouldAssignThePackageNameFromTheManifest() throws Exception {
        Application application = new ApplicationResolver(new File("test" + separator + "TestAndroidManifestWithPackageName.xml")).resolveApplication();
        assertEquals("com.wacka.wa", application.getPackageName());
    }
}
