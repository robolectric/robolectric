package com.xtremelabs.robolectric.shadows;

import android.app.KeyguardManager;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadows the {@code android.app.KeyguardManager} class.
 */
@Implements(KeyguardManager.class)
public class ShadowKeyguardManager {

	private boolean inRestrictedInputMode = false;
	
	@Implementation
	public boolean inKeyguardRestrictedInputMode() {
		return inRestrictedInputMode;
	}
	
	public void setinRestrictedInputMode(boolean restricted) {
		inRestrictedInputMode = restricted;
	}
}
