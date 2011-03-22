package com.xtremelabs.robolectric.shadows;

import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(PreferenceActivity.class)
public class ShadowPreferenceActivity extends ShadowListActivity {
	
	private int preferencesResId = -1;
	private PreferenceScreen preferenceScreen;
	
	public ShadowPreferenceActivity() {
		preferenceScreen = Robolectric.newInstanceOf(PreferenceScreen.class);
	}

	@Implementation
	public void addPreferencesFromResource(int preferencesResId) {
		this.preferencesResId = preferencesResId;
	}
	
	public int getPreferencesResId() {
		return preferencesResId;
	}	
	
	@Implementation
	public PreferenceScreen getPreferenceScreen() {
		return preferenceScreen;
	}
}
