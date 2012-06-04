package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.TestUtil.newConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    
    @Test
    public void shouldAssignTheApplicationNameFromTheManifest() throws Exception {
        Application application = new ApplicationResolver(newConfig("TestAndroidManifestWithAppName.xml")).resolveApplication();
        assertEquals("com.xtremelabs.robolectric.TestApplication", application.getApplicationInfo().name);
    }

    @Test
    public void shouldRegisterReceiversFromTheManifest() throws Exception {
        Application application = new ApplicationResolver(newConfig("TestAndroidManifestWithReceivers.xml")).resolveApplication();
        List<ShadowApplication.Wrapper> receivers = shadowOf(application).getRegisteredReceivers();
        assertEquals(7, receivers.size());
        assertTrue(receivers.get(0).intentFilter.matchAction("com.xtremelabs.robolectric.ACTION1"));
    }
}
