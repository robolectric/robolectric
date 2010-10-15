package com.xtremelabs.robolectric.fakes;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.FakeHelper;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(PendingIntent.class)
public class FakePendingIntent {
    private Intent savedIntent;
    private boolean isServiceIntent;

    @Implementation
    public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
        PendingIntent pendingIntent = FakeHelper.newInstanceOf(PendingIntent.class);
        FakePendingIntent fakePendingIntent = (FakePendingIntent) ProxyDelegatingHandler.getInstance().proxyFor(pendingIntent);
        fakePendingIntent.savedIntent = intent;
        fakePendingIntent.isServiceIntent = true;
        return pendingIntent;
    }

    @Implementation
    public void send(Context context, int code, Intent intent) throws PendingIntent.CanceledException {
        if(isServiceIntent) {
            context.startService(savedIntent);
        }
        // else, will startActivity
    }
}
