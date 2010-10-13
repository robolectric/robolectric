package com.xtremelabs.droidsugar.fakes;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.FakeHelper;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

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
