package com.xtremelabs.robolectric.shadows;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.xtremelabs.robolectric.Robolectric.*;
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
		manager = newInstanceOf(TelephonyManager.class);
		shadowManager = shadowOf(manager);

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
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
        assertEquals(testId, telephonyManager.getDeviceId());
    }

    @Test
    public void shouldGiveNetworkOperatorName() {
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
        ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
        shadowTelephonyManager.setNetworkOperatorName("SomeOperatorName");
        assertEquals("SomeOperatorName", telephonyManager.getNetworkOperatorName());
    }

    @Test
    public void shouldGiveNetworkCountryIso() {
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
        ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
        shadowTelephonyManager.setNetworkCountryIso("SomeIso");
        assertEquals("SomeIso", telephonyManager.getNetworkCountryIso());
    }

    @Test
    public void shouldGiveNetworkOperator() {
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
        ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
        shadowTelephonyManager.setNetworkOperator("SomeOperator");
        assertEquals("SomeOperator", telephonyManager.getNetworkOperator());
    }

	private class MyPhoneStateListener extends PhoneStateListener {
		
	}
}
