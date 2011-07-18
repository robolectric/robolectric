package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Implementation;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

@Implements(TelephonyManager.class)
public class ShadowTelephonyManager {
	
	private PhoneStateListener listener;
	private int eventFlags;
    private static String deviceId;
    private String networkOperatorName;
    private String networkCountryIso;
    private String networkOperator;

    @Implementation
	public void listen(PhoneStateListener listener, int events) {
		this.listener = listener;
		this.eventFlags = events;
	}
	
	/**
	 * Non-Android accessor.  Returns the most recent listener
	 * passed to #listen().
	 * 
	 * @return
	 */
	public PhoneStateListener getListener() {
		return listener;
	}

	/**
	 * Non-Android accessor.  Returns the most recent flags
	 * passed to #listen().
	 * @return
	 */
	public int getEventFlags() {
		return eventFlags;
	}

    @Implementation
    public String getDeviceId() {
        return deviceId;
    }

    public static void setDeviceId(String newDeviceId) {
        deviceId = newDeviceId;
    }

    public void setNetworkOperatorName(String networkOperatorName) {
        this.networkOperatorName = networkOperatorName;
    }

    @Implementation
    public String getNetworkOperatorName() {
        return networkOperatorName;
    }

    public void setNetworkCountryIso(String networkCountryIso) {
        this.networkCountryIso = networkCountryIso;
    }

    @Implementation
    public String getNetworkCountryIso() {
        return networkCountryIso;
    }

    public void setNetworkOperator(String networkOperator) {
        this.networkOperator = networkOperator;
    }

    @Implementation
    public String getNetworkOperator() {
        return networkOperator;
    }
}
