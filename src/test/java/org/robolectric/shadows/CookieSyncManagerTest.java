package org.robolectric.shadows;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.webkit.CookieSyncManager;

import org.robolectric.Robolectric;

@RunWith(TestRunners.WithDefaults.class)
public class CookieSyncManagerTest {

	@Test
	public void testCreateInstance() {
		assertThat( CookieSyncManager.createInstance( new Activity() ) , notNullValue() );
	}
	
	@Test
	public void testGetInstance() {
		CookieSyncManager.createInstance( new Activity() );
		assertThat( CookieSyncManager.getInstance(), notNullValue() );
	}
	
	@Test
	public void testSyncAndReset() {
		CookieSyncManager.createInstance( new Activity() );
		CookieSyncManager mgr = CookieSyncManager.getInstance();
		
		ShadowCookieSyncManager shadowMgr = Robolectric.shadowOf( mgr );
		assertThat( shadowMgr.synced(), equalTo( false ) );
		mgr.sync();
		assertThat( shadowMgr.synced(), equalTo( true ) );
        shadowMgr.reset();
        assertThat( shadowMgr.synced(), equalTo( false ) );
	}
}
