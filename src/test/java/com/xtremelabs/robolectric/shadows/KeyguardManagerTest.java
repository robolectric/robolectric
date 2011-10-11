package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.KeyguardManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Context.KEYGUARD_SERVICE;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class KeyguardManagerTest {

	@Test
	public void testIsInRestrcitedInputMode() {
		Activity activity = new Activity();
		KeyguardManager mgr = ( KeyguardManager ) activity.getSystemService( KEYGUARD_SERVICE );
		assertThat( mgr.inKeyguardRestrictedInputMode(), equalTo( false ) );
		ShadowKeyguardManager shadowMgr = shadowOf(mgr);
		shadowMgr.setinRestrictedInputMode( true );
		assertThat( mgr.inKeyguardRestrictedInputMode(), equalTo( true ) );		
	}

    @Test
    public void testShouldBeAbleToDisableTheKeyguardLock() throws Exception {
        Activity activity = new Activity();
        KeyguardManager mgr = ( KeyguardManager ) activity.getSystemService( KEYGUARD_SERVICE );
        KeyguardManager.KeyguardLock lock = mgr.newKeyguardLock(KEYGUARD_SERVICE);
        assertTrue(shadowOf(lock).isEnabled());

        lock.disableKeyguard();
        assertFalse(shadowOf(lock).isEnabled());

        lock.reenableKeyguard();
        assertTrue(shadowOf(lock).isEnabled());
    }
}
