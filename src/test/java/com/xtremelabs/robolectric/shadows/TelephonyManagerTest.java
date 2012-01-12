package com.xtremelabs.robolectric.shadows;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.xtremelabs.robolectric.Robolectric.application;
import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
        shadowOf(telephonyManager).setDeviceId(testId);
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

    @Test(expected = SecurityException.class)
    public void getDeviceId_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted() throws Exception {
        shadowManager.setReadPhoneStatePermission(false);
        manager.getDeviceId();
    }
    
    @Test
    public void shouldGivePhoneType() {
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
        ShadowTelephonyManager shadowTelephonyManager = shadowOf(telephonyManager);
        shadowTelephonyManager.setPhoneType( TelephonyManager.PHONE_TYPE_CDMA );
        assertEquals(TelephonyManager.PHONE_TYPE_CDMA, telephonyManager.getPhoneType());
        shadowTelephonyManager.setPhoneType( TelephonyManager.PHONE_TYPE_GSM );
        assertEquals(TelephonyManager.PHONE_TYPE_GSM, telephonyManager.getPhoneType());
    }

	private class MyPhoneStateListener extends PhoneStateListener {
		
	}
}
