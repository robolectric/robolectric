package org.robolectric.shadows;

import android.app.Dialog;
import android.preference.PreferenceScreen;

import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

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
