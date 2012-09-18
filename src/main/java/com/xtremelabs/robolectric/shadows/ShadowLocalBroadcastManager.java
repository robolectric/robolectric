package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Implements(LocalBroadcastManager.class)
public class ShadowLocalBroadcastManager {

    private static LocalBroadcastManager publicInstance;
    private static HiddenLocalBroadcastManager localInstance;
    private final static Object lock = new Object();

    // TODO there's some probability that the receivers field has to be synchronized on each access inside this class
    private final Set<BroadcastReceiver> receivers = new HashSet<BroadcastReceiver>();
    private List<Intent> broadcastIntents = new ArrayList<Intent>();

    public static void reset() {
        Robolectric.Reflection.setFinalStaticField(LocalBroadcastManager.class, "mInstance", null);
        publicInstance = null;
    }

    @Implementation
    public static LocalBroadcastManager getInstance(Context context) {
        synchronized (lock) {
            if (publicInstance == null) {
                publicInstance = Robolectric.newInstance(LocalBroadcastManager.class, new Class[] {Context.class}, new Object[] {context});
                localInstance = HiddenLocalBroadcastManager.getInstance(new Activity());
            }
        }
        return publicInstance;
    }

    @Implementation
    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        receivers.add(receiver);
        localInstance.registerReceiver(receiver, filter);
    }

    @Implementation
    public void unregisterReceiver(BroadcastReceiver receiver) {
        receivers.remove(receiver);
        localInstance.unregisterReceiver(receiver);
    }

    @Implementation
    public boolean sendBroadcast(Intent intent) {
        broadcastIntents.add(intent);
        return localInstance.sendBroadcast(intent);
    }

    @Implementation
    public void sendBroadcastSync(Intent intent) {
        broadcastIntents.add(intent);
        localInstance.sendBroadcastSync(intent);
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

    // Copied from original Android sources.
    // Do we have a better way to simulate the real behaviour inside the shadow?
    private static class HiddenLocalBroadcastManager {
        private static class ReceiverRecord {
            final IntentFilter filter;
            final BroadcastReceiver receiver;
            boolean broadcasting;

            ReceiverRecord(IntentFilter _filter, BroadcastReceiver _receiver) {
                filter = _filter;
                receiver = _receiver;
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder(128);
                builder.append("Receiver{");
                builder.append(receiver);
                builder.append(" filter=");
                builder.append(filter);
                builder.append("}");
                return builder.toString();
            }
        }

        private static class BroadcastRecord {
            final Intent intent;
            final ArrayList<ReceiverRecord> receivers;

            BroadcastRecord(Intent _intent, ArrayList<ReceiverRecord> _receivers) {
                intent = _intent;
                receivers = _receivers;
            }
        }

        private static final String TAG = "LocalBroadcastManager";
        private static final boolean DEBUG = false;

        private final Context mAppContext;

        private final HashMap<BroadcastReceiver, ArrayList<IntentFilter>> mReceivers
            = new HashMap<BroadcastReceiver, ArrayList<IntentFilter>>();
        private final HashMap<String, ArrayList<ReceiverRecord>> mActions
            = new HashMap<String, ArrayList<ReceiverRecord>>();

        private final ArrayList<BroadcastRecord> mPendingBroadcasts
            = new ArrayList<BroadcastRecord>();

        static final int MSG_EXEC_PENDING_BROADCASTS = 1;

        private final Handler mHandler;

        private static final Object mLock = new Object();
        private static HiddenLocalBroadcastManager mInstance;

        public static HiddenLocalBroadcastManager getInstance(Context context) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new HiddenLocalBroadcastManager(context.getApplicationContext());
                }
                return mInstance;
            }
        }

        private HiddenLocalBroadcastManager(Context context) {
            mAppContext = context;
            mHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                      case MSG_EXEC_PENDING_BROADCASTS:
                          executePendingBroadcasts();
                          break;
                      default:
                          super.handleMessage(msg);
                    }
                }
            };
        }

        /**
         * Register a receive for any local broadcasts that match the given IntentFilter.
         *
         * @param receiver The BroadcastReceiver to handle the broadcast.
         * @param filter Selects the Intent broadcasts to be received.
         *
         * @see #unregisterReceiver
         */
        public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
            synchronized (mReceivers) {
                ReceiverRecord entry = new ReceiverRecord(filter, receiver);
                ArrayList<IntentFilter> filters = mReceivers.get(receiver);
                if (filters == null) {
                    filters = new ArrayList<IntentFilter>(1);
                    mReceivers.put(receiver, filters);
                }
                filters.add(filter);
                for (int i=0; i<filter.countActions(); i++) {
                    String action = filter.getAction(i);
                    ArrayList<ReceiverRecord> entries = mActions.get(action);
                    if (entries == null) {
                        entries = new ArrayList<ReceiverRecord>(1);
                        mActions.put(action, entries);
                    }
                    entries.add(entry);
                }
            }
        }

        /**
         * Unregister a previously registered BroadcastReceiver.  <em>All</em>
         * filters that have been registered for this BroadcastReceiver will be
         * removed.
         *
         * @param receiver The BroadcastReceiver to unregister.
         *
         * @see #registerReceiver
         */
        public void unregisterReceiver(BroadcastReceiver receiver) {
            synchronized (mReceivers) {
                ArrayList<IntentFilter> filters = mReceivers.remove(receiver);
                if (filters == null) {
                    return;
                }
                for (int i=0; i<filters.size(); i++) {
                    IntentFilter filter = filters.get(i);
                    for (int j=0; j<filter.countActions(); j++) {
                        String action = filter.getAction(j);
                        ArrayList<ReceiverRecord> receivers = mActions.get(action);
                        if (receivers != null) {
                            for (int k=0; k<receivers.size(); k++) {
                                if (receivers.get(k).receiver == receiver) {
                                    receivers.remove(k);
                                    k--;
                                }
                            }
                            if (receivers.size() <= 0) {
                                mActions.remove(action);
                            }
                        }
                    }
                }
            }
        }

        /**
         * Broadcast the given intent to all interested BroadcastReceivers.  This
         * call is asynchronous; it returns immediately, and you will continue
         * executing while the receivers are run.
         *
         * @param intent The Intent to broadcast; all receivers matching this
         *     Intent will receive the broadcast.
         *
         * @see #registerReceiver
         */
        public boolean sendBroadcast(Intent intent) {
            synchronized (mReceivers) {
                final String action = intent.getAction();
                final String type = intent.resolveTypeIfNeeded(
                    mAppContext.getContentResolver());
                final Uri data = intent.getData();
                final String scheme = intent.getScheme();
                final Set<String> categories = intent.getCategories();

                final boolean debug = DEBUG ||
                                      ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);
                if (debug) Log.v(
                    TAG, "Resolving type " + type + " scheme " + scheme
                         + " of intent " + intent);

                ArrayList<ReceiverRecord> entries = mActions.get(intent.getAction());
                if (entries != null) {
                    if (debug) Log.v(TAG, "Action list: " + entries);

                    ArrayList<ReceiverRecord> receivers = null;
                    for (int i=0; i<entries.size(); i++) {
                        ReceiverRecord receiver = entries.get(i);
                        if (debug) Log.v(TAG, "Matching against filter " + receiver.filter);

                        if (receiver.broadcasting) {
                            if (debug) {
                                Log.v(TAG, "  Filter's target already added");
                            }
                            continue;
                        }

                        int match = receiver.filter.match(action, type, scheme, data,
                                                          categories, "LocalBroadcastManager");
                        if (match >= 0) {
                            if (debug) Log.v(TAG, "  Filter matched!  match=0x" +
                                                  Integer.toHexString(match));
                            if (receivers == null) {
                                receivers = new ArrayList<ReceiverRecord>();
                            }
                            receivers.add(receiver);
                            receiver.broadcasting = true;
                        } else {
                            if (debug) {
                                String reason;
                                switch (match) {
                                    case IntentFilter.NO_MATCH_ACTION: reason = "action"; break;
                                    case IntentFilter.NO_MATCH_CATEGORY: reason = "category"; break;
                                    case IntentFilter.NO_MATCH_DATA: reason = "data"; break;
                                    case IntentFilter.NO_MATCH_TYPE: reason = "type"; break;
                                    default: reason = "unknown reason"; break;
                                }
                                Log.v(TAG, "  Filter did not match: " + reason);
                            }
                        }
                    }

                    if (receivers != null) {
                        for (int i=0; i<receivers.size(); i++) {
                            receivers.get(i).broadcasting = false;
                        }
                        mPendingBroadcasts.add(new BroadcastRecord(intent, receivers));
                        if (!mHandler.hasMessages(MSG_EXEC_PENDING_BROADCASTS)) {
                            mHandler.sendEmptyMessage(MSG_EXEC_PENDING_BROADCASTS);
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Like {@link #sendBroadcast(Intent)}, but if there are any receivers for
         * the Intent this function will block and immediately dispatch them before
         * returning.
         */
        public void sendBroadcastSync(Intent intent) {
            if (sendBroadcast(intent)) {
                executePendingBroadcasts();
            }
        }

        private void executePendingBroadcasts() {
            while (true) {
                BroadcastRecord[] brs = null;
                synchronized (mReceivers) {
                    final int N = mPendingBroadcasts.size();
                    if (N <= 0) {
                        return;
                    }
                    brs = new BroadcastRecord[N];
                    mPendingBroadcasts.toArray(brs);
                    mPendingBroadcasts.clear();
                }
                for (int i=0; i<brs.length; i++) {
                    BroadcastRecord br = brs[i];
                    for (int j=0; j<br.receivers.size(); j++) {
                        br.receivers.get(j).receiver.onReceive(mAppContext, br.intent);
                    }
                }
            }
        }
    }
}
