package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.ServiceConnection;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Service.class)
public class ShadowService extends ShadowContextWrapper {
    @RealObject Service realService;
    
    private boolean unbindServiceShouldThrowIllegalArgument = false;

    @Implementation
    public final Application getApplication() {
        return Robolectric.application;
    }

    @Implementation @Override
    public Context getApplicationContext() {
        return Robolectric.application;
    }

    @Implementation
    public void onDestroy() {
        assertNoBroadcastListenersRegistered();
    }
    
    @Implementation 
    public void unbindService(ServiceConnection conn) {
    	if (unbindServiceShouldThrowIllegalArgument) {
    		throw new IllegalArgumentException();
    	}
    }
    
    public void setUnbindServiceShouldThrowIllegalArgument(boolean flag) {
    	unbindServiceShouldThrowIllegalArgument = flag;
    }

    /**
     * Utility method that throws a {@code RuntimeException} if any {@code BroadcastListener}s are still registered.
     */
    public void assertNoBroadcastListenersRegistered() {
        ((ShadowApplication) shadowOf(getApplicationContext())).assertNoBroadcastListenersRegistered(realService, "Service");
    }
}
