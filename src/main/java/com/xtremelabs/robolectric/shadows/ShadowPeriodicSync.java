package com.xtremelabs.robolectric.shadows;

import android.accounts.Account;
import android.content.PeriodicSync;
import android.os.Bundle;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Field;

@Implements(PeriodicSync.class)
public class ShadowPeriodicSync {

    @RealObject
    private PeriodicSync realObject;

    public void __constructor__(Account account, String authority, Bundle extras, long period) throws Exception {
        setField("account", account);
        setField("authority", authority);
        setField("period", period);
        setField("extras", extras);
    }


    private void setField(String name, Object obj) throws Exception {
        Field f = realObject.getClass().getField(name);
        f.setAccessible(true);
        f.set(realObject, obj);
    }
}
