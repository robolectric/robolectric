package com.xtremelabs.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestBroadcastReceiver;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;
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

    @Test
    public void testGetBroadcastIntents() throws Exception {
        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
        ShadowLocalBroadcastManager shadow = shadowOf_(instance);
        shadow.clearBroadcastIntents();
        Intent intent1 = new Intent("foo");
        instance.sendBroadcast(intent1);
        Intent intent2 = new Intent("bar");
        instance.sendBroadcast(intent2);

        List<Intent> broadcastIntents = shadow.getBroadcastIntents();
        assertEquals(intent1, broadcastIntents.get(0));
        assertEquals(intent2, broadcastIntents.get(1));
    }

    @Test
    public void testGetLatestBroadcastIntent() throws Exception {
        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
        ShadowLocalBroadcastManager shadow = shadowOf_(instance);
        Intent intent1 = new Intent("foo");
        instance.sendBroadcast(intent1);
        Intent intent2 = new Intent("bar");
        instance.sendBroadcast(intent2);

        Intent latestBroadcastIntent = shadow.getLatestBroadcastIntent();
        assertEquals(intent2, latestBroadcastIntent);
    }

    @Test
    public void testHasBroadcastReceiver() throws Exception {
        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
        instance.registerReceiver(new TestBroadcastReceiver(), new IntentFilter());
        ShadowLocalBroadcastManager shadow = shadowOf_(instance);

      boolean hasBroadcastReceiver = shadow.hasBroadcastReceiver(TestBroadcastReceiver.class);
      assertTrue(hasBroadcastReceiver);
    }

    @Test
    public void testClearBroadcastIntents() throws Exception {
        LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
        ShadowLocalBroadcastManager shadow = shadowOf_(instance);
        Intent intent1 = new Intent("foo");
        instance.sendBroadcast(intent1);
        Intent intent2 = new Intent("bar");
        instance.sendBroadcast(intent2);

        shadow.clearBroadcastIntents();
        Intent intent3 = new Intent("baz");
        instance.sendBroadcast(intent3);

        List<Intent> broadcastIntents = shadow.getBroadcastIntents();
        assertEquals(1, broadcastIntents.size());
        assertEquals(intent3, broadcastIntents.get(0));
    }
}
