package com.xtremelabs.robolectric.shadows;

import android.app.Dialog;
import android.preference.PreferenceScreen;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(PreferenceScreen.class)
public class ShadowPreferenceScreen extends ShadowPreferenceGroup {

	private Dialog dialog;
	
	@Implementation
	public Dialog getDialog() {
		return dialog;
	}
	
	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}
}
