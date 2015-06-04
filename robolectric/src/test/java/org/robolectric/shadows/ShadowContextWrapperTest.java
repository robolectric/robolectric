package org.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowContextWrapperTest {
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
  public void sendOrderedBroadcast_shouldSendByPriority() throws Exception {
    String action = "test";

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(2);
    BroadcastReceiver highReceiver = broadcastReceiver("High");
    contextWrapper.registerReceiver(highReceiver, highFilter);

    contextWrapper.sendOrderedBroadcast(new Intent(action), null);
    transcript.assertEventsSoFar("High notified of test", "Low notified of test");
  }

  @Test
  public void orderedBroadcasts_shouldAbort() throws Exception {
    String action = "test";

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(2);
    BroadcastReceiver highReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        transcript.add("High" + " notified of " + intent.getAction());
        abortBroadcast();
      }
    };
    contextWrapper.registerReceiver(highReceiver, highFilter);

    contextWrapper.sendOrderedBroadcast(new Intent(action), null);
    transcript.assertEventsSoFar("High notified of test");
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

    new ContextWrapper(RuntimeEnvironment.application).registerReceiver(receiver, intentFilter("foo", "baz"));
    new ContextWrapper(RuntimeEnvironment.application).sendBroadcast(new Intent("foo"));
    RuntimeEnvironment.application.sendBroadcast(new Intent("baz"));
    transcript.assertEventsSoFar("Larry notified of foo", "Larry notified of baz");

    new ContextWrapper(RuntimeEnvironment.application).unregisterReceiver(receiver);
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
  public void sendStickyBroadcast_shouldDeliverIntentToAllRegisteredReceivers() {
    BroadcastReceiver receiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));

    contextWrapper.sendStickyBroadcast(new Intent("foo"));
    transcript.assertEventsSoFar("Larry notified of foo");

    contextWrapper.sendStickyBroadcast(new Intent("womp"));
    transcript.assertNoEventsSoFar();

    contextWrapper.sendStickyBroadcast(new Intent("baz"));
    transcript.assertEventsSoFar("Larry notified of baz");
  }

  @Test
  public void sendStickyBroadcast_shouldStickSentIntent() {
    contextWrapper.sendStickyBroadcast(new Intent("foo"));
    transcript.assertNoEventsSoFar();

    BroadcastReceiver receiver = broadcastReceiver("Larry");
    Intent sticker = contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));
    transcript.assertEventsSoFar("Larry notified of foo");
    assertThat(sticker).isNotNull();
    assertThat(sticker.getAction()).isEqualTo("foo");
  }

  @Test
  public void afterSendStickyBroadcast_allSentIntentsShouldBeDeliveredToNewRegistrants() {
    contextWrapper.sendStickyBroadcast(new Intent("foo"));
    contextWrapper.sendStickyBroadcast(new Intent("baz"));
    transcript.assertNoEventsSoFar();

    BroadcastReceiver receiver = broadcastReceiver("Larry");
    Intent sticker = contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));
    transcript.assertEventsSoFar("Larry notified of foo", "Larry notified of baz");
    /*
       Note: we do not strictly test what is returned by the method in this case
             because there no guaranties what particular Intent will be returned by Android system
     */
    assertThat(sticker).isNotNull();
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
    View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.custom_layout, null);
    Context viewContext = new ContextWrapper(view.getContext());
    assertThat(viewContext.getApplicationContext()).isEqualTo(RuntimeEnvironment.application);
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
    assertEquals("foo", shadowOf(RuntimeEnvironment.application).getNextStartedService().getAction());
  }

  @Test
  public void startActivities_shouldStartAllActivities() {
    final Intent view = new Intent(Intent.ACTION_VIEW);
    final Intent pick = new Intent(Intent.ACTION_PICK);
    contextWrapper.startActivities(new Intent[] {view, pick});

    assertThat(ShadowApplication.getInstance().getNextStartedActivity()).isEqualTo(pick);
    assertThat(ShadowApplication.getInstance().getNextStartedActivity()).isEqualTo(view);
  }

  @Test
  public void startActivities_withBundle_shouldStartAllActivities() {
    final Intent view = new Intent(Intent.ACTION_VIEW);
    final Intent pick = new Intent(Intent.ACTION_PICK);
    contextWrapper.startActivities(new Intent[] {view, pick}, new Bundle());

    assertThat(ShadowApplication.getInstance().getNextStartedActivity()).isEqualTo(pick);
    assertThat(ShadowApplication.getInstance().getNextStartedActivity()).isEqualTo(view);
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
  public void getSharedPreferencesShouldReturnSameInstanceWhenSameNameIsSupplied() {
    final SharedPreferences pref1 = contextWrapper.getSharedPreferences("pref", Context.MODE_PRIVATE);
    final SharedPreferences pref2 = contextWrapper.getSharedPreferences("pref", Context.MODE_PRIVATE);

    assertThat(pref1).isSameAs(pref2);
  }

  @Test
  public void getSharedPreferencesShouldReturnDifferentInstancesWhenDifferentNameIsSupplied() {
    final SharedPreferences pref1 = contextWrapper.getSharedPreferences("pref1", Context.MODE_PRIVATE);
    final SharedPreferences pref2 = contextWrapper.getSharedPreferences("pref2", Context.MODE_PRIVATE);

    assertThat(pref1).isNotSameAs(pref2);
  }

  @Test
  public void sendBroadcast_shouldOnlySendIntentWithTypeWhenReceiverMatchesType()
    throws IntentFilter.MalformedMimeTypeException {

    final BroadcastReceiver viewAllTypesReceiver = broadcastReceiver("ViewActionWithAnyTypeReceiver");
    final IntentFilter allTypesIntentFilter = intentFilter("view");
    allTypesIntentFilter.addDataType("*/*");
    contextWrapper.registerReceiver(viewAllTypesReceiver, allTypesIntentFilter);

    final BroadcastReceiver imageReceiver = broadcastReceiver("ImageReceiver");
    final IntentFilter imageIntentFilter = intentFilter("view");
    imageIntentFilter.addDataType("img/*");
    contextWrapper.registerReceiver(imageReceiver, imageIntentFilter);

    final BroadcastReceiver videoReceiver = broadcastReceiver("VideoReceiver");
    final IntentFilter videoIntentFilter = intentFilter("view");
    videoIntentFilter.addDataType("video/*");
    contextWrapper.registerReceiver(videoReceiver, videoIntentFilter);

    final BroadcastReceiver viewReceiver = broadcastReceiver("ViewActionReceiver");
    final IntentFilter viewIntentFilter = intentFilter("view");
    contextWrapper.registerReceiver(viewReceiver, viewIntentFilter);

    final Intent imageIntent = new Intent("view");
    imageIntent.setType("img/jpeg");
    contextWrapper.sendBroadcast(imageIntent);

    final Intent videoIntent = new Intent("view");
    videoIntent.setType("video/mp4");
    contextWrapper.sendBroadcast(videoIntent);

    transcript.assertEventsSoFar(
            "ViewActionWithAnyTypeReceiver notified of view",
            "ImageReceiver notified of view",
            "ViewActionWithAnyTypeReceiver notified of view",
            "VideoReceiver notified of view");
  }

  @Test
  public void getApplicationInfo_shouldReturnApplicationInfoForApplicationPackage() {
    final ApplicationInfo info = contextWrapper.getApplicationInfo();
    assertThat(info.packageName).isEqualTo("org.robolectric");
  }

  @Test
  public void getApplicationInfo_whenPackageManagerIsNull_shouldNotExplode() {
    RuntimeEnvironment.setRobolectricPackageManager(null);
    contextWrapper.getApplicationInfo();
  }
}
