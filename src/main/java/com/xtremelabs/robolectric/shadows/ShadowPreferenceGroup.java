package com.xtremelabs.robolectric.shadows;

import android.preference.PreferenceGroup;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(PreferenceGroup.class)
public class ShadowPreferenceGroup extends ShadowPreference {

	private boolean enabled = true;
	
	@Implementation
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
