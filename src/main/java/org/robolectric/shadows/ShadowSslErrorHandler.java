package com.xtremelabs.robolectric.shadows;

import android.webkit.SslErrorHandler;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(SslErrorHandler.class)
public class ShadowSslErrorHandler extends ShadowHandler {

	private boolean cancelCalled = false;
	private boolean proceedCalled = false;
	
    @Implementation
    public void cancel() {
    	cancelCalled = true;
    }
    
    public boolean wasCancelCalled() {
    	return cancelCalled;
    }
	
    @Implementation
    public void proceed() {
    	proceedCalled = true;
    }
    
    public boolean wasProceedCalled() {
    	return proceedCalled;
    }
}
