package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.util.SheepWrangler;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Service.class)
public class ShadowService extends ShadowContextWrapper {
    @SheepWrangler ProxyDelegatingHandler proxyDelegatingHandler;
    @RealObject Service realService;

    public ShadowService(Service realService) {
        super(realService);
    }

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

    public void assertNoBroadcastListenersRegistered() {
        ((ShadowApplication) proxyDelegatingHandler.shadowFor(getApplicationContext())).assertNoBroadcastListenersRegistered(realService, "Service");
    }
}
