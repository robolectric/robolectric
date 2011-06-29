package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

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

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testListen() {
		manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		assertThat(shadowManager.getListener(), notNullValue());
		assertThat((MyPhoneStateListener) shadowManager.getListener(), sameInstance(listener));
		assertThat(shadowManager.getEventFlags(), equalTo(PhoneStateListener.LISTEN_CALL_STATE));
	}

	private class MyPhoneStateListener extends PhoneStateListener {
		
	}
	
}
