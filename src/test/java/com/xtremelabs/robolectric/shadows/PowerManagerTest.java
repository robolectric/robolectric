package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class PowerManagerTest {
	
	@Test
	public void testIsScreenOn() {
		Activity activity = new Activity();
		PowerManager mgr = ( PowerManager ) activity.getSystemService( Context.POWER_SERVICE );
		assertThat( mgr.isScreenOn(), equalTo( true ) );
		ShadowPowerManager shadowMgr = Robolectric.shadowOf( mgr );
		shadowMgr.setIsScreenOn( false );
		assertThat( mgr.isScreenOn(), equalTo( false ) );		
	}
}