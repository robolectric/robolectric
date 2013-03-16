package org.robolectric;

import android.app.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.TestUtil.newConfig;

@RunWith(TestRunners.WithDefaults.class)
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
    
    @Test
    public void shouldAssignTheApplicationNameFromTheManifest() throws Exception {
        Application application = new ApplicationResolver(newConfig("TestAndroidManifestWithAppName.xml")).resolveApplication();
        assertEquals("org.robolectric.TestApplication", application.getApplicationInfo().name);
    }

    @Test
    public void shouldRegisterReceiversFromTheManifest() throws Exception {
        Application application = new ApplicationResolver(newConfig("TestAndroidManifestWithReceivers.xml")).resolveApplication();
        List<ShadowApplication.Wrapper> receivers = shadowOf(application).getRegisteredReceivers();
        assertEquals(6, receivers.size());
        assertTrue(receivers.get(0).intentFilter.matchAction("org.robolectric.ACTION1"));
    }
}
