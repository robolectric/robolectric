package com.xtremelabs.robolectric.fakes;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(PendingIntent.class)
public class ShadowPendingIntent {
    private Intent savedIntent;
    private boolean isServiceIntent;

    @Implementation
    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
        return create(intent, false);
    }

    @Implementation
    public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
        return create(intent, true);
    }

    private static PendingIntent create(Intent intent, boolean isService) {
        PendingIntent pendingIntent = Robolectric.newInstanceOf(PendingIntent.class);
        ShadowPendingIntent fakePendingIntent = (ShadowPendingIntent) ProxyDelegatingHandler.getInstance().shadowFor(pendingIntent);
        fakePendingIntent.savedIntent = intent;
        fakePendingIntent.isServiceIntent = isService;
        return pendingIntent;
    }

    @Implementation
    public void send(Context context, int code, Intent intent) throws PendingIntent.CanceledException {
        if (isServiceIntent) {
            context.startService(savedIntent);
        } else {
            context.startActivity(savedIntent);
        }
    }
}
