package org.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
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
  public void sendBroadcast_shouldOnlySendIntentWithMatchingReceiverPermission() {
    BroadcastReceiver receiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"), "validPermission", null);

    contextWrapper.sendBroadcast(new Intent("foo"));
    transcript.assertNoEventsSoFar();

    contextWrapper.sendBroadcast(new Intent("foo"), null);
    transcript.assertNoEventsSoFar();

    contextWrapper.sendBroadcast(new Intent("foo"), "wrongPermission");
    transcript.assertNoEventsSoFar();

    contextWrapper.sendBroadcast(new Intent("foo"), "validPermission");
    transcript.assertEventsSoFar("Larry notified of foo");

    contextWrapper.sendBroadcast(new Intent("baz"), "validPermission");
    transcript.assertEventsSoFar("Larry notified of baz");
  }

  @SuppressWarnings("all") // Couldn't figure out which to use for suppressing nullables or null checks, ("null") didn't work
  @Test
  public void sendBroadcast_shouldSendIntentUsingHandlerIfOneIsProvided() {
    HandlerThread handlerThread = new HandlerThread("test");
    handlerThread.start();

    Handler handler = new Handler(handlerThread.getLooper());
    assertNotSame(handler.getLooper(), Looper.getMainLooper());

    BroadcastReceiver receiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"), null, handler);

    assertThat(shadowOf(handler.getLooper()).getScheduler().size()).isEqualTo(0);
    contextWrapper.sendBroadcast(new Intent("foo"));
    assertThat(shadowOf(handler.getLooper()).getScheduler().size()).isEqualTo(1);
    shadowOf(handlerThread.getLooper()).idle();
    assertThat(shadowOf(handler.getLooper()).getScheduler().size()).isEqualTo(0);

    transcript.assertEventsSoFar("Larry notified of foo");
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
    assertThat(activity.getApplication()).isSameAs(activity.getApplication());

    assertThat(activity.getApplication()).isSameAs(new Activity().getApplication());
  }

  @Test
  public void shouldReturnSameApplicationContextEveryTime() throws Exception {
    Activity activity = new Activity();
    assertThat(activity.getApplicationContext()).isSameAs(activity.getApplicationContext());

    assertThat(activity.getApplicationContext()).isSameAs(new Activity().getApplicationContext());
  }

  @Test
  public void shouldReturnApplicationContext_forViewContextInflatedWithApplicationContext() throws Exception {
    View view = LayoutInflater.from(Robolectric.application).inflate(R.layout.custom_layout, null);
    Context viewContext = new ContextWrapper(view.getContext());
    assertThat(viewContext.getApplicationContext()).isEqualTo(Robolectric.application);
  }

  @Test
  public void shouldReturnSameContentResolverEveryTime() throws Exception {
    Activity activity = new Activity();
    assertThat(activity.getContentResolver()).isSameAs(activity.getContentResolver());

    assertThat(activity.getContentResolver()).isSameAs(new Activity().getContentResolver());
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
    assertThat(contextWrapper.checkPermission("foo", 0, 0)).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkPermission("bar", 0, 0)).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkPermission("baz", 0, 0)).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void shouldReturnAContext() {
    assertThat(contextWrapper.getBaseContext()).isNotNull();

    contextWrapper = new ContextWrapper(null);
    shadowOf(contextWrapper).callAttachBaseContext(null);
    assertThat(contextWrapper.getBaseContext()).isNull();

    Activity baseContext = new Activity();
    shadowOf(contextWrapper).callAttachBaseContext(baseContext);
    assertThat(contextWrapper.getBaseContext()).isSameAs(baseContext);
  }

  private void assertSameInstanceEveryTime(String serviceName) {
    Activity activity1 = buildActivity(Activity.class).create().get();
    Activity activity2 = buildActivity(Activity.class).create().get();
    assertThat(activity1.getSystemService(serviceName)).isSameAs(activity1.getSystemService(serviceName));
    assertThat(activity1.getSystemService(serviceName)).isSameAs(activity2.getSystemService(serviceName));
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

  @Test
  public void packageManagerShouldNotBeNullWhenWrappingAnApplication() {
    assertThat(new Application().getPackageManager()).isNotNull();
  }

  @Test
  public void checkCallingPermissionsShouldReturnPermissionGrantedToAddedPermissions() throws Exception {
    shadowOf(contextWrapper).grantPermissions("foo", "bar");
    assertThat(contextWrapper.checkCallingPermission("foo")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingPermission("bar")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingPermission("baz")).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void checkCallingOrSelfPermissionsShouldReturnPermissionGrantedToAddedPermissions() throws Exception {
    shadowOf(contextWrapper).grantPermissions("foo", "bar");
    assertThat(contextWrapper.checkCallingOrSelfPermission("foo")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("bar")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("baz")).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void checkCallingPermission_shouldReturnPermissionDeniedForRemovedPermissions() throws Exception {
    shadowOf(contextWrapper).grantPermissions("foo", "bar");
    shadowOf(contextWrapper).denyPermissions("foo", "qux");
    assertThat(contextWrapper.checkCallingPermission("foo")).isEqualTo(PERMISSION_DENIED);
    assertThat(contextWrapper.checkCallingPermission("bar")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingPermission("baz")).isEqualTo(PERMISSION_DENIED);
    assertThat(contextWrapper.checkCallingPermission("qux")).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void checkCallingOrSelfPermission_shouldReturnPermissionDeniedForRemovedPermissions() throws Exception {
    shadowOf(contextWrapper).grantPermissions("foo", "bar");
    shadowOf(contextWrapper).denyPermissions("foo", "qux");
    assertThat(contextWrapper.checkCallingOrSelfPermission("foo")).isEqualTo(PERMISSION_DENIED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("bar")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("baz")).isEqualTo(PERMISSION_DENIED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("qux")).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void openOrCreateDatabaseShouldAlwaysReturnSameDatabase() throws Exception {
    assertThat(contextWrapper.openOrCreateDatabase("db", 0, null)).isNotNull();
  }
}
