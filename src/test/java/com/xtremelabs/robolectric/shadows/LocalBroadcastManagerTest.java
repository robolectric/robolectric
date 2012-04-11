package com.xtremelabs.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class LocalBroadcastManagerTest {
    private static LocalBroadcastManager lastInstance;

    @Test
    public void shouldGetInstance() throws Exception {
        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
        assertNotNull(instance);
        assertSame(instance, LocalBroadcastManager.getInstance(Robolectric.application));
    }

    @Test
    public void shouldSendBroadcasts() throws Exception {
        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
        final boolean[] called = new boolean[1];
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                called[0] = true;

            }
        };
        instance.registerReceiver(receiver, new IntentFilter("com.foo"));

        instance.sendBroadcast(new Intent("com.bar"));
        assertFalse(called[0]);
        instance.sendBroadcast(new Intent("com.foo"));
        assertTrue(called[0]);
    }

    @Test
    public void shouldUnregisterReceiver() throws Exception {

        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
        final boolean[] called = new boolean[1];
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                called[0] = true;

            }
        };
        instance.registerReceiver(receiver, new IntentFilter("com.foo"));
        instance.unregisterReceiver(receiver);
        instance.sendBroadcast(new Intent("com.foo"));
        assertFalse(called[0]);
    }

    @Test
    public void shouldResetStateBetweenTests1() throws Exception {
        lastInstance = LocalBroadcastManager.getInstance(Robolectric.application);
        assertNotNull(lastInstance);
    }

    @Test
    public void shouldResetStateBetweenTests2() throws Exception {
        assertNotNull(lastInstance);
        assertNotSame(lastInstance, LocalBroadcastManager.getInstance(Robolectric.application));
        lastInstance = null;
    }
}
