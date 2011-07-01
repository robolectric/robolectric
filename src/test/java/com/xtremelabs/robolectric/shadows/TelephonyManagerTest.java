package com.xtremelabs.robolectric.shadows;

import static android.content.Context.TELEPHONY_SERVICE;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import android.content.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

@RunWith(WithTestDefaultsRunner.class)
public class TelephonyManagerTest {
	
	private TelephonyManager manager;
	private ShadowTelephonyManager shadowManager;
	private MyPhoneStateListener listener;

	@Before
	public void setUp() throws Exception {
		manager = Robolectric.newInstanceOf(TelephonyManager.class);
		shadowManager = Robolectric.shadowOf(manager);

		listener = new MyPhoneStateListener(); 
	}

	@Test
	public void testListen() {
		manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		assertThat(shadowManager.getListener(), notNullValue());
		assertThat((MyPhoneStateListener) shadowManager.getListener(), sameInstance(listener));
		assertThat(shadowManager.getEventFlags(), equalTo(PhoneStateListener.LISTEN_CALL_STATE));
	}

    @Test
    public void shouldGiveDeviceId() {
        String testId = "TESTING123";
        ShadowTelephonyManager.setDeviceId(testId);
        TelephonyManager telephonyManager = (TelephonyManager) Robolectric.application.getSystemService(TELEPHONY_SERVICE);
        assertEquals(testId, telephonyManager.getDeviceId());
    }

	private class MyPhoneStateListener extends PhoneStateListener {
		
	}
}
