package com.xtremelabs.robolectric.shadows;

import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(InputMethodManager.class)
public class ShadowInputMethodManager {
	
	private boolean softInputVisible;

	@Implementation
	public boolean showSoftInput(View view, int flags) {
		return showSoftInput(view, flags, null);
	}
	
	@Implementation
	public boolean showSoftInput(View view, int flags, ResultReceiver resultReceiver) {
		softInputVisible = true;
		return true;
	}
	
	@Implementation
	public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
		return hideSoftInputFromWindow(windowToken, flags, null);
	}
	
	@Implementation
	public boolean hideSoftInputFromWindow(IBinder windowToken, int flags, ResultReceiver resultReceiver) {
		softInputVisible = false;
		return true;
	}
	
	public boolean isSoftInputVisible() {
		return softInputVisible;
	}
}
