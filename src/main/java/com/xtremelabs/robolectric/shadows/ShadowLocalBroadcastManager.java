package com.xtremelabs.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Implements(LocalBroadcastManager.class)
public class ShadowLocalBroadcastManager {

    private static LocalBroadcastManager uniqueInstance;
    private final static Object lock = new Object();

    // TODO there's some probability that the receivers field has to be synchronized on each access inside this class
    private final Set<BroadcastReceiver> receivers = new HashSet<BroadcastReceiver>();
    private List<Intent> broadcastIntents = new ArrayList<Intent>();

    public static void reset() {
        Robolectric.Reflection.setFinalStaticField(LocalBroadcastManager.class, "mInstance", null);
    }

    @Implementation
    public static LocalBroadcastManager getInstance(Context context) {
        synchronized (lock) {
            if (uniqueInstance == null) {
                uniqueInstance = Robolectric.newInstance(LocalBroadcastManager.class, new Class[] {Context.class}, new Object[] {context});
            }
        }
        return uniqueInstance;
    }

    @Implementation
    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        receivers.add(receiver);
    }

    @Implementation
    public void unregisterReceiver(BroadcastReceiver receiver) {
        receivers.remove(receiver);
    }

    @Implementation
    public boolean sendBroadcast(Intent intent) {
        broadcastIntents.add(intent);
        return false;
    }

    @Implementation
    public void sendBroadcastSync(Intent intent) {
        broadcastIntents.add(intent);
    }

    public List<Intent> getBroadcastIntents() {
        return broadcastIntents;
    }

    public Intent getLatestBroadcastIntent() {
        if (broadcastIntents.isEmpty()) {
          return null;
        }
        return broadcastIntents.get(broadcastIntents.size() - 1);
    }

    public boolean hasBroadcastReceiver(Class<? extends BroadcastReceiver> type) {
        for (BroadcastReceiver receiver : receivers) {
            if (type.isAssignableFrom(receiver.getClass())) {
               return true;
            }
        }
        return false;
    }

    public void clearBroadcastIntents() {
        broadcastIntents.clear();
    }
}
