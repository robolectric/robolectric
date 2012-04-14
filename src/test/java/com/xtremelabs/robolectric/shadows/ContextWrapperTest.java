package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ContextWrapperTest {
    public Transcript transcript;
    private ContextWrapper contextWrapper;

    @Before public void setUp() throws Exception {
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
	public void broadcasts_shouldBeLogged() {
		Intent broadcastIntent = new Intent("foo");
		contextWrapper.sendBroadcast(broadcastIntent);
		
		List<Intent> broadcastIntents = shadowOf(contextWrapper).getBroadcastIntents();
		assertTrue(broadcastIntents.size() == 1);
		assertEquals(broadcastIntent, broadcastIntents.get(0));
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
        assertSameInstanceEveryTime(Context.LOCATION_SERVICE);
    }

    @Test
    public void shouldReturnSameWifiManagerEveryTime() throws Exception {
        assertSameInstanceEveryTime(Context.WIFI_SERVICE);
    }

    @Test
    public void shouldReturnSameAlarmServiceEveryTime() throws Exception {
        assertSameInstanceEveryTime(Context.ALARM_SERVICE);
    }

    @Test
    public void checkPermissionsShouldReturnPermissionGrantedToAddedPermissions() throws Exception {
        shadowOf(contextWrapper).grantPermissions("foo", "bar");
        assertThat(contextWrapper.checkPermission("foo", 0, 0), equalTo(PERMISSION_GRANTED));
        assertThat(contextWrapper.checkPermission("bar", 0, 0), equalTo(PERMISSION_GRANTED));
        assertThat(contextWrapper.checkPermission("baz", 0, 0), equalTo(PERMISSION_DENIED));
    }

    @Test
    public void shouldReturnAContext() {
    	assertThat(contextWrapper.getBaseContext(), notNullValue());
    	ShadowContextWrapper shContextWrapper = Robolectric.shadowOf(contextWrapper);
    	shContextWrapper.attachBaseContext(null);
    	assertThat(contextWrapper.getBaseContext(), nullValue());

    	Activity baseContext = new Activity();
    	shContextWrapper.attachBaseContext(baseContext);
    	assertThat(contextWrapper.getBaseContext(), sameInstance((Context) baseContext));
    }

    private void assertSameInstanceEveryTime(String serviceName) {
        Activity activity = new Activity();
        assertThat(activity.getSystemService(serviceName), sameInstance(activity.getSystemService(serviceName)));

        assertThat(activity.getSystemService(serviceName), sameInstance(new Activity().getSystemService(serviceName)));
    }

    @Test
    public void bindServiceDelegatesToShadowApplication() {
        contextWrapper.bindService(new Intent("foo"), new TestService(), Context.BIND_AUTO_CREATE);
        assertEquals("foo", shadowOf(Robolectric.application).getNextStartedService().getAction());
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
