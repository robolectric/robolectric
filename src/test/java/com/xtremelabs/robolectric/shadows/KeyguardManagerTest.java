package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class KeyguardManagerTest {
	
	@Test
	public void testIsInRestrcitedInputMode() {
		Activity activity = new Activity();
		KeyguardManager mgr = ( KeyguardManager ) activity.getSystemService( Context.KEYGUARD_SERVICE );
		assertThat( mgr.inKeyguardRestrictedInputMode(), equalTo( false ) );
		ShadowKeyguardManager shadowMgr = Robolectric.shadowOf( mgr );
		shadowMgr.setinRestrictedInputMode( true );
		assertThat( mgr.inKeyguardRestrictedInputMode(), equalTo( true ) );		
	}
}
