package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Implementation;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

@Implements(TelephonyManager.class)
public class ShadowTelephonyManager {
	
	private PhoneStateListener listener;
	private int eventFlags;
	
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

}
