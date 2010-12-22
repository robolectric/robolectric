package com.xtremelabs.robolectric.shadows;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow of {@code PendingIntent} that creates and sends {@code Intent}s appropriately.
 */
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

    @Implementation
    public void send(Context context, int code, Intent intent) throws PendingIntent.CanceledException {
        if (isServiceIntent) {
            context.startService(savedIntent);
        } else {
            context.startActivity(savedIntent);
        }
    }

    private static PendingIntent create(Intent intent, boolean isService) {
        PendingIntent pendingIntent = Robolectric.newInstanceOf(PendingIntent.class);
        ShadowPendingIntent shadowPendingIntent = (ShadowPendingIntent) Robolectric.shadowOf_(pendingIntent);
        shadowPendingIntent.savedIntent = intent;
        shadowPendingIntent.isServiceIntent = isService;
        return pendingIntent;
    }
}
