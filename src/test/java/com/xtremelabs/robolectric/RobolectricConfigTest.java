package com.xtremelabs.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.pm.ApplicationInfo.*;
import static com.xtremelabs.robolectric.util.TestUtil.newConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class RobolectricConfigTest {
    @Test
    public void shouldReadBroadcastReceivers() throws Exception {
        RobolectricConfig config = newConfig("TestAndroidManifestWithReceivers.xml");

        assertEquals(7, config.getReceiverCount());

        assertEquals("com.xtremelabs.robolectric.RobolectricConfigTest.ConfigTestReceiver", config.getReceiverClassName(0));
        assertEquals("com.xtremelabs.robolectric.ACTION1", config.getReceiverIntentFilterActions(0).get(0));

        assertEquals("com.xtremelabs.robolectric.RobolectricConfigTest.ConfigTestReceiver", config.getReceiverClassName(1));
        assertEquals("com.xtremelabs.robolectric.ACTION2", config.getReceiverIntentFilterActions(1).get(0));

        assertEquals("com.xtremelabs.robolectric.test.ConfigTestReceiver", config.getReceiverClassName(2));
        assertEquals("com.xtremelabs.robolectric.ACTION_SUPERSET_PACKAGE", config.getReceiverIntentFilterActions(2).get(0));

        assertEquals("com.xtremelabs.ConfigTestReceiver", config.getReceiverClassName(3));
        assertEquals("com.xtremelabs.robolectric.ACTION_SUBSET_PACKAGE", config.getReceiverIntentFilterActions(3).get(0));

        assertEquals("com.xtremelabs.robolectric.DotConfigTestReceiver", config.getReceiverClassName(4));
        assertEquals("com.xtremelabs.robolectric.ACTION_DOT_PACKAGE", config.getReceiverIntentFilterActions(4).get(0));

        assertEquals("com.xtremelabs.robolectric.test.ConfigTestReceiver", config.getReceiverClassName(5));
        assertEquals("com.xtremelabs.robolectric.ACTION_DOT_SUBPACKAGE", config.getReceiverIntentFilterActions(5).get(0));

        assertEquals("com.foo.Receiver", config.getReceiverClassName(6));
        assertEquals("com.xtremelabs.robolectric.ACTION_DIFFERENT_PACKAGE", config.getReceiverIntentFilterActions(6).get(0));
    }

    @Test
    public void shouldReadSdkVersionFromAndroidManifest() throws Exception {
        assertEquals(42, newConfig("TestAndroidManifestWithSdkVersion.xml").getSdkVersion());
        assertEquals(3, newConfig("TestAndroidManifestWithSdkVersion.xml").getMinSdkVersion());
    }
    
    @Test
    public void shouldRessolveSdkVersionForResources() throws Exception {
        assertEquals(3, newConfig("TestAndroidManifestWithMinSdkVersionOnly.xml").getRealSdkVersion());
        assertEquals(42, newConfig("TestAndroidManifestWithSdkVersion.xml").getRealSdkVersion());
    }
    
    @Test
    public void shouldReadProcessFromAndroidManifest() throws Exception {
    	assertEquals("robolectricprocess", newConfig("TestAndroidManifestWithProcess.xml").getProcessName());
    }

    @Test
    public void shouldReturnPackageNameWhenNoProcessIsSpecifiedInTheManifest() {
    	assertEquals("com.xtremelabs.robolectric", newConfig("TestAndroidManifestWithNoProcess.xml").getProcessName());
    }

    @Test
    public void shouldReadFlagsFromAndroidManifest() throws Exception {
        RobolectricConfig config = newConfig("TestAndroidManifestWithFlags.xml");
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_ALLOW_BACKUP));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_ALLOW_CLEAR_USER_DATA));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_ALLOW_TASK_REPARENTING));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_DEBUGGABLE));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_HAS_CODE));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_KILL_AFTER_RESTORE));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_PERSISTENT));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_RESIZEABLE_FOR_SCREENS));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_RESTORE_ANY_VERSION));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_LARGE_SCREENS));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_NORMAL_SCREENS));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_SCREEN_DENSITIES));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_SMALL_SCREENS));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_TEST_ONLY));
        assertTrue(hasFlag(config.getApplicationFlags(), FLAG_VM_SAFE_MODE));
    }
    
    private boolean hasFlag(final int flags, final int flag) {
    	return (flags & flag) != 0;
    }
    
    public static class ConfigTestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }
}
