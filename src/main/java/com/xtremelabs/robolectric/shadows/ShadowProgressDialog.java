package com.xtremelabs.robolectric.shadows;


import android.app.ProgressDialog;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ProgressDialog.class)
public class ShadowProgressDialog extends ShadowAlertDialog {

	private boolean indeterminate;
	
	@Implementation
	public void setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
	}
	
	@Implementation
	public boolean isIndeterminate() {
		return indeterminate;
	}
}
