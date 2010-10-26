package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(DogfoodRobolectricTestRunner.class)
public class ContextWrapperTest {
    public Transcript transcript;
    private ContextWrapper contextWrapper;

    @Before public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();

        Robolectric.application = new Application();

        transcript = new Transcript();
        contextWrapper = new ContextWrapper(new Activity());
    }

    @Test
    public void registerReceiver_shouldRegisterForAllIntentFilterActions() throws Exception {
        BroadcastReceiver receiver = broadcastReceiver("Larry");
        contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));

        contextWrapper.sendBroadcast(new Intent("foo"));
        transcript.assertEventsSoFar("Larry notified of foo");

        contextWrapper.sendBroadcast(new Intent("womp"));
        transcript.assertNoEventsSoFar();

        contextWrapper.sendBroadcast(new Intent("baz"));
        transcript.assertEventsSoFar("Larry notified of baz");
    }

    @Test
    public void sendBroadcast_shouldSendIntentToEveryInterestedReceiver() throws Exception {
        BroadcastReceiver larryReceiver = broadcastReceiver("Larry");
        contextWrapper.registerReceiver(larryReceiver, intentFilter("foo", "baz"));

        BroadcastReceiver bobReceiver = broadcastReceiver("Bob");
        contextWrapper.registerReceiver(bobReceiver, intentFilter("foo"));

        contextWrapper.sendBroadcast(new Intent("foo"));
        transcript.assertEventsSoFar("Larry notified of foo", "Bob notified of foo");

        contextWrapper.sendBroadcast(new Intent("womp"));
        transcript.assertNoEventsSoFar();

        contextWrapper.sendBroadcast(new Intent("baz"));
        transcript.assertEventsSoFar("Larry notified of baz");
    }

    @Test
    public void unregisterReceiver_shouldUnregisterReceiver() throws Exception {
        BroadcastReceiver receiver = broadcastReceiver("Larry");

        contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));
        contextWrapper.unregisterReceiver(receiver);

        contextWrapper.sendBroadcast(new Intent("foo"));
        transcript.assertNoEventsSoFar();
    }

    @Test(expected = IllegalArgumentException.class)
    public void unregisterReceiver_shouldThrowExceptionWhenReceiverIsNotRegistered() throws Exception {
        contextWrapper.unregisterReceiver(new AppWidgetProvider());
    }

    @Test
    public void broadcastReceivers_shouldBeSharedAcrossContextsPerApplicationContext() throws Exception {
        BroadcastReceiver receiver = broadcastReceiver("Larry");

        new ContextWrapper(Robolectric.application).registerReceiver(receiver, intentFilter("foo", "baz"));
        new ContextWrapper(Robolectric.application).sendBroadcast(new Intent("foo"));
        Robolectric.application.sendBroadcast(new Intent("baz"));
        transcript.assertEventsSoFar("Larry notified of foo", "Larry notified of baz");

        new ContextWrapper(Robolectric.application).unregisterReceiver(receiver);
    }

    @Test
    public void shouldReturnSameApplicationEveryTime() throws Exception {
        Activity activity = new Activity();
        assertThat(activity.getApplication(), sameInstance(activity.getApplication()));

        assertThat(activity.getApplication(), sameInstance(new Activity().getApplication()));
    }

    @Test
    public void shouldReturnSameApplicationContextEveryTime() throws Exception {
        Activity activity = new Activity();
        assertThat(activity.getApplicationContext(), sameInstance(activity.getApplicationContext()));

        assertThat(activity.getApplicationContext(), sameInstance(new Activity().getApplicationContext()));
    }

    @Test
    public void shouldReturnSameContentResolverEveryTime() throws Exception {
        Activity activity = new Activity();
        assertThat(activity.getContentResolver(), sameInstance(activity.getContentResolver()));

        assertThat(activity.getContentResolver(), sameInstance(new Activity().getContentResolver()));
    }

    @Test
    public void shouldReturnSameLocationManagerEveryTime() throws Exception {
        Activity activity = new Activity();
        assertThat(activity.getSystemService(Context.LOCATION_SERVICE), sameInstance(activity.getSystemService(Context.LOCATION_SERVICE)));

        assertThat(activity.getSystemService(Context.LOCATION_SERVICE), sameInstance(new Activity().getSystemService(Context.LOCATION_SERVICE)));
    }

    @Test
    public void shouldReturnSameWifiManagerEveryTime() throws Exception {
        Activity activity = new Activity();
        assertThat(activity.getSystemService(Context.WIFI_SERVICE), sameInstance(activity.getSystemService(Context.WIFI_SERVICE)));

        assertThat(activity.getSystemService(Context.WIFI_SERVICE), sameInstance(new Activity().getSystemService(Context.WIFI_SERVICE)));
    }

    private BroadcastReceiver broadcastReceiver(final String name) {
        return new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                transcript.add(name + " notified of " + intent.getAction());
            }
        };
    }

    private IntentFilter intentFilter(String... actions) {
        IntentFilter larryIntentFilter = new IntentFilter();
        for (String action : actions) {
            larryIntentFilter.addAction(action);
        }
        return larryIntentFilter;
    }
}
