package com.xtremelabs.robolectric.shadows;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(PreferenceActivity.class)
public class ShadowPreferenceActivity extends ShadowListActivity {

	private int preferencesResId = -1;
	private PreferenceScreen preferenceScreen;

	@Implementation
	public void addPreferencesFromResource(int preferencesResId) {
		this.preferencesResId = preferencesResId;
		preferenceScreen = getResourceLoader().inflatePreferences(getApplicationContext(), preferencesResId);
	}

	public int getPreferencesResId() {
		return preferencesResId;
	}

	@Implementation
	public PreferenceScreen getPreferenceScreen() {
		return preferenceScreen;
	}

	@Implementation
	public Preference findPreference(CharSequence key) {
		if (preferenceScreen != null && preferenceScreen instanceof PreferenceGroup) {
			return ((PreferenceGroup) preferenceScreen).findPreference(key);
		}
		return null;
	}
}
