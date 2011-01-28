package com.xtremelabs.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.newConfig;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class RobolectricConfigTest {
    @Test
    public void shouldReadSdkVersionFromAndroidManifest() throws Exception {
        assertEquals(42, newConfig("TestAndroidManifestWithSdkVersion.xml").getSdkVersion());
    }
}
