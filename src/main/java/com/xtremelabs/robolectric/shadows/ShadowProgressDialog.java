package com.xtremelabs.robolectric.shadows;


import android.app.ProgressDialog;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ProgressDialog.class)
public class ShadowProgressDialog {

	private CharSequence message;
	
	@Implementation
	public void setMessage(CharSequence message) {
		this.message = message;
	}
	
	public CharSequence getMessage() {
		return message;
	}	
}
