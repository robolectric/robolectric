package com.xtremelabs.robolectric.shadows;

import android.preference.PreferenceActivity;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(PreferenceActivity.class)
public class ShadowPreferenceActivity extends ShadowListActivity {
	
	private int preferencesResId = -1;

	@Implementation
	public void addPreferencesFromResource(int preferencesResId) {
		this.preferencesResId = preferencesResId;
	}
	
	public int getPreferencesResId() {
		return preferencesResId;
	}	
}
