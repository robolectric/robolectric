package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Implements(NfcAdapter.class)
public class ShadowNfcAdapter {
    @RealObject NfcAdapter nfcAdapter;
    private Activity enabledActivity;
    private PendingIntent intent;
    private IntentFilter[] filters;
    private String[][] techLists;
    private Activity disabledActivity;

    @Implementation
    public static NfcAdapter getDefaultAdapter(Context context) {
        try {
            Constructor<NfcAdapter> constructor = NfcAdapter.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Implementation
    public void enableForegroundDispatch(Activity activity, PendingIntent intent, IntentFilter[] filters, String[][] techLists) {
        this.enabledActivity = activity;
        this.intent = intent;
        this.filters = filters;
        this.techLists = techLists;
    }

    @Implementation
    public void disableForegroundDispatch(Activity activity) {
        disabledActivity = activity;
    }

    public Activity getEnabledActivity() {
        return enabledActivity;
    }

    public PendingIntent getIntent() {
        return intent;
    }

    public IntentFilter[] getFilters() {
        return filters;
    }

    public String[][] getTechLists() {
        return techLists;
    }

    public Activity getDisabledActivity() {
        return disabledActivity;
    }
}
