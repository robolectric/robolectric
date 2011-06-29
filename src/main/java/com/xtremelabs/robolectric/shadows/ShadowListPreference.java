package com.xtremelabs.robolectric.shadows;

import android.preference.ListPreference;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ListPreference.class)
public class ShadowListPreference extends ShadowDialogPreference {
	
	CharSequence[] entries;
	CharSequence[] entryValues;
	
	@Implementation
	public CharSequence[] getEntries() {
		return entries;
	}

	@Implementation
	public void setEntries(CharSequence[] entries) {
		this.entries = entries;
	}

	@Implementation
	public void setEntries(int entriesResId) {
		this.entries = context.getResources().getStringArray(entriesResId);
	}

	@Implementation
	public CharSequence[] getEntryValues() {
		return entryValues;
	}

	@Implementation
	public void setEntryValues(CharSequence[] entryValues) {
		this.entryValues = entryValues;
	}

	@Implementation
	public void setEntryValues(int entryValuesResId) {
		this.entryValues = context.getResources().getStringArray(entryValuesResId);
	}
}
