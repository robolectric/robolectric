package org.robolectric.shadows;

import android.app.Activity;
import android.webkit.CookieSyncManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class CookieSyncManagerTest {

    @Test
    public void testCreateInstance() {
        assertThat(CookieSyncManager.createInstance(new Activity()), notNullValue());
    }

    @Test
    public void testGetInstance() {
        CookieSyncManager.createInstance(new Activity());
        assertThat(CookieSyncManager.getInstance(), notNullValue());
    }

    @Test
    public void testSyncAndReset() {
        CookieSyncManager.createInstance(new Activity());
        CookieSyncManager mgr = CookieSyncManager.getInstance();

        ShadowCookieSyncManager shadowManager = Robolectric.shadowOf(mgr);
        assertThat(shadowManager.synced(), equalTo(false));
        mgr.sync();
        assertThat(shadowManager.synced(), equalTo(true));
        shadowManager.reset();
        assertThat(shadowManager.synced(), equalTo(false));
    }
}
