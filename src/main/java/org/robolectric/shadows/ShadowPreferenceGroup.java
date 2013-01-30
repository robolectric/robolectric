package com.xtremelabs.robolectric.shadows;

import java.util.ArrayList;

import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.text.TextUtils;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

/**
 * See: http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob_plain;f=core/java/android/preference/PreferenceGroup.java;hb=HEAD
 */
@Implements(PreferenceGroup.class)
public class ShadowPreferenceGroup extends ShadowPreference {

    @RealObject private PreferenceGroup realPreferenceGroup;

    private ArrayList<Preference> preferenceList = new ArrayList<Preference>();
	
	@Implementation
	public void addItemFromInflater(Preference preference) {
		addPreference(preference);
	}
	
	@Implementation
	public boolean addPreference(Preference preference) {
		if (preferenceList.contains(preference)) {
            return true;
        }
		
		// TODO currently punting on ordering logic
		preferenceList.add(preference);

		return true;
	}
	
	@Implementation
	public Preference getPreference(int index) {
		return preferenceList.get(index);
	}
	
	@Implementation
	public int getPreferenceCount() {
		return preferenceList.size();
	}
	
	@Implementation
	public boolean removePreference(Preference preference) {
		return preferenceList.remove(preference);
	}
	
	@Implementation
	public void removeAll() {
		preferenceList.clear();
	}
	
	/**
	 * Note: copied wholesale from Android source
	 * @param key
	 * @return
	 */
	@Implementation
	public Preference findPreference(CharSequence key) {
        if (TextUtils.equals(getKey(), key)) {
            return realPreferenceGroup;
        }
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            final Preference preference = getPreference(i);
            final String curKey = preference.getKey();

            if (curKey != null && curKey.equals(key)) {
                return preference;
            }
            
            if (preference instanceof PreferenceGroup) {
                final Preference returnedPreference = ((PreferenceGroup)preference)
                        .findPreference(key);
                if (returnedPreference != null) {
                    return returnedPreference;
                }
            }
        }

        return null;
    }
	
	/**
	 * Note: copied wholesale from Android source
	 */
	@Implementation
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).setEnabled(enabled);
        }
	}
}
