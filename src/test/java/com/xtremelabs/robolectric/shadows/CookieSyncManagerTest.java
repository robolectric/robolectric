package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.webkit.CookieSyncManager;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
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
