package com.xtremelabs.robolectric.shadows;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(TelephonyManager.class)
public class ShadowTelephonyManager {
	
	private PhoneStateListener listener;
	private int eventFlags;
    private String deviceId;
    private String networkOperatorName;
    private String networkCountryIso;
    private String networkOperator;
    private boolean readPhoneStatePermission = true;
    private int phoneType = TelephonyManager.PHONE_TYPE_GSM;
    private String simCountryIso;

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
        checkReadPhoneStatePermission();
        return deviceId;
    }

    public void setDeviceId(String newDeviceId) {
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

    @Implementation
    public String getSimCountryIso() {
        return simCountryIso;
    }

    public void setSimCountryIso(String simCountryIso) {
        this.simCountryIso = simCountryIso;
    }

    public void setReadPhoneStatePermission(boolean readPhoneStatePermission) {
        this.readPhoneStatePermission = readPhoneStatePermission;
    }

    private void checkReadPhoneStatePermission() {
        if (!readPhoneStatePermission) {
            throw new SecurityException();
        }
    }
    
    @Implementation
    public int getPhoneType() {
    	return phoneType;
    }
    
    public void setPhoneType(int phoneType) {
    	this.phoneType = phoneType;
    }
}
