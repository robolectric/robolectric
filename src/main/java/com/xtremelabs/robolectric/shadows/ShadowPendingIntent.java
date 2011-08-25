package com.xtremelabs.robolectric.shadows;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.TestIntentSender;
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
    private Context savedContext;
    private boolean isServiceIntent;
    private int requestCode;

    @Implementation
    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
        return create(context, intent, false, requestCode);
    }

    @Implementation
    public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
        return create(context, intent, false, requestCode);
    }

    @Implementation
    public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
        return create(context, intent, true, requestCode);
    }

    @Implementation
    public void send() throws PendingIntent.CanceledException {
    	send(savedContext, 0, savedIntent);
    }

    @Implementation
    public void send(Context context, int code, Intent intent) throws PendingIntent.CanceledException {
    	savedIntent.fillIn( intent, 0 );
    	if (isServiceIntent) {
            context.startService(savedIntent);
        } else {
            context.startActivity(savedIntent);
        }
    }

    @Implementation
    public IntentSender getIntentSender() {
        TestIntentSender testIntentSender = new TestIntentSender();
        testIntentSender.intent = savedIntent;
        return testIntentSender;
    }

    public boolean isServiceIntent() {
        return isServiceIntent;
    }

    public Context getSavedContext() {
        return savedContext;
    }

    public Intent getSavedIntent() {
        return savedIntent;
    }

    public int getRequestCode() {
        return requestCode;
    }

    private static PendingIntent create(Context context, Intent intent, boolean isService, int requestCode) {
        PendingIntent pendingIntent = Robolectric.newInstanceOf(PendingIntent.class);
        ShadowPendingIntent shadowPendingIntent = (ShadowPendingIntent) Robolectric.shadowOf_(pendingIntent);
        shadowPendingIntent.savedIntent = intent;
        shadowPendingIntent.isServiceIntent = isService;
        shadowPendingIntent.savedContext = context;
        shadowPendingIntent.requestCode = requestCode;
        return pendingIntent;
    }
}
