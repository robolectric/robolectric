package org.robolectric.shadows;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowContextWrapperTest {
  public ArrayList<String> transcript;
  private ContextWrapper contextWrapper;

  private final Context context = ApplicationProvider.getApplicationContext();
  private final ShadowContextWrapper shadowContextWrapper = Shadow.extract(context);

  @Before public void setUp() throws Exception {
    transcript = new ArrayList<>();
    contextWrapper = new ContextWrapper(context);
  }

  @Test
  public void registerReceiver_shouldRegisterForAllIntentFilterActions() throws Exception {
    BroadcastReceiver receiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));

    contextWrapper.sendBroadcast(new Intent("foo"));
    assertThat(transcript).containsExactly("Larry notified of foo");
    transcript.clear();

    contextWrapper.sendBroadcast(new Intent("womp"));
    assertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("baz"));
    assertThat(transcript).containsExactly("Larry notified of baz");
  }

  @Test
  public void sendBroadcast_shouldSendIntentToEveryInterestedReceiver() throws Exception {
    BroadcastReceiver larryReceiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(larryReceiver, intentFilter("foo", "baz"));

    BroadcastReceiver bobReceiver = broadcastReceiver("Bob");
    contextWrapper.registerReceiver(bobReceiver, intentFilter("foo"));

    contextWrapper.sendBroadcast(new Intent("foo"));
    assertThat(transcript).containsExactly("Larry notified of foo", "Bob notified of foo");
    transcript.clear();

    contextWrapper.sendBroadcast(new Intent("womp"));
    assertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("baz"));
    assertThat(transcript).containsExactly("Larry notified of baz");
  }

  @Test
  public void sendBroadcast_shouldOnlySendIntentWithMatchingReceiverPermission() {
    BroadcastReceiver receiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"), "validPermission", null);

    contextWrapper.sendBroadcast(new Intent("foo"));
    assertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("foo"), null);
    assertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("foo"), "wrongPermission");
    assertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("foo"), "validPermission");
    assertThat(transcript).containsExactly("Larry notified of foo");
    transcript.clear();

    contextWrapper.sendBroadcast(new Intent("baz"), "validPermission");
    assertThat(transcript).containsExactly("Larry notified of baz");
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

    assertThat(transcript).containsExactly("Larry notified of foo");
  }

  @Test
  public void sendOrderedBroadcast_shouldReturnValues() throws Exception {
    String action = "test";

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(2);
    BroadcastReceiver highReceiver = broadcastReceiver("High");
    contextWrapper.registerReceiver(highReceiver, highFilter);

    final FooReceiver resultReceiver = new FooReceiver();
    contextWrapper.sendOrderedBroadcast(new Intent(action), null, resultReceiver, null, 1, "initial", null);
    assertThat(transcript).containsExactly("High notified of test", "Low notified of test");
    assertThat(resultReceiver.resultCode).isEqualTo(1);
  }

  private static final class FooReceiver extends BroadcastReceiver {
    private int resultCode;
    private SettableFuture<Void> settableFuture = SettableFuture.create();

    @Override
    public void onReceive(Context context, Intent intent) {
      resultCode = getResultCode();
      settableFuture.set(null);
    }
  }

  @Test
  public void sendOrderedBroadcast_shouldExecuteSerially() {
    String action = "test";
    AtomicReference<BroadcastReceiver.PendingResult> midResult = new AtomicReference<>();

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter midFilter = new IntentFilter(action);
    midFilter.setPriority(2);
    AsyncReceiver midReceiver = new AsyncReceiver(midResult);
    contextWrapper.registerReceiver(midReceiver, midFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(3);
    BroadcastReceiver highReceiver = broadcastReceiver("High");
    contextWrapper.registerReceiver(highReceiver, highFilter);

    contextWrapper.sendOrderedBroadcast(new Intent(action), null);
    assertThat(transcript).containsExactly("High notified of test", "Mid notified of test");
    transcript.clear();
    assertThat(midResult.get()).isNotNull();
    midResult.get().finish();
    Robolectric.flushForegroundThreadScheduler();
    assertThat(transcript).containsExactly("Low notified of test");
  }

  private class AsyncReceiver extends BroadcastReceiver {
    private final AtomicReference<PendingResult> reference;

    private AsyncReceiver(AtomicReference<PendingResult> reference) {
      this.reference = reference;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      reference.set(goAsync());
      transcript.add("Mid notified of " + intent.getAction());
    }
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
    assertThat(transcript).containsExactly("High notified of test", "Low notified of test");
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
    assertThat(transcript).containsExactly("High notified of test");
  }

  @Test
  public void unregisterReceiver_shouldUnregisterReceiver() throws Exception {
    BroadcastReceiver receiver = broadcastReceiver("Larry");

    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));
    contextWrapper.unregisterReceiver(receiver);

    contextWrapper.sendBroadcast(new Intent("foo"));
    assertThat(transcript).isEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void unregisterReceiver_shouldThrowExceptionWhenReceiverIsNotRegistered() throws Exception {
    contextWrapper.unregisterReceiver(new AppWidgetProvider());
  }

  @Test
  public void broadcastReceivers_shouldBeSharedAcrossContextsPerApplicationContext() throws Exception {
    BroadcastReceiver receiver = broadcastReceiver("Larry");

    Application application = ApplicationProvider.getApplicationContext();
    new ContextWrapper(application).registerReceiver(receiver, intentFilter("foo", "baz"));
    new ContextWrapper(application).sendBroadcast(new Intent("foo"));
    application.sendBroadcast(new Intent("baz"));
    assertThat(transcript).containsExactly("Larry notified of foo", "Larry notified of baz");

    new ContextWrapper(application).unregisterReceiver(receiver);
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
    assertThat(transcript).containsExactly("Larry notified of foo");
    transcript.clear();

    contextWrapper.sendStickyBroadcast(new Intent("womp"));
    assertThat(transcript).isEmpty();

    contextWrapper.sendStickyBroadcast(new Intent("baz"));
    assertThat(transcript).containsExactly("Larry notified of baz");
  }

  @Test
  public void sendStickyBroadcast_shouldStickSentIntent() {
    contextWrapper.sendStickyBroadcast(new Intent("foo"));
    assertThat(transcript).isEmpty();

    BroadcastReceiver receiver = broadcastReceiver("Larry");
    Intent sticker = contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));
    assertThat(transcript).containsExactly("Larry notified of foo");
    assertThat(sticker).isNotNull();
    assertThat(sticker.getAction()).isEqualTo("foo");
  }

  @Test
  public void afterSendStickyBroadcast_allSentIntentsShouldBeDeliveredToNewRegistrants() {
    contextWrapper.sendStickyBroadcast(new Intent("foo"));
    contextWrapper.sendStickyBroadcast(new Intent("baz"));
    assertThat(transcript).isEmpty();

    BroadcastReceiver receiver = broadcastReceiver("Larry");
    Intent sticker = contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));
    assertThat(transcript).containsExactly("Larry notified of foo", "Larry notified of baz");
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
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertThat(activity.getApplicationContext()).isSameAs(activity.getApplicationContext());

    assertThat(activity.getApplicationContext()).isSameAs(Robolectric.setupActivity(Activity.class).getApplicationContext());
  }

  @Test
  public void shouldReturnApplicationContext_forViewContextInflatedWithApplicationContext() throws Exception {
    View view =
        LayoutInflater.from(ApplicationProvider.getApplicationContext())
            .inflate(R.layout.custom_layout, null);
    Context viewContext = new ContextWrapper(view.getContext());
    assertThat(viewContext.getApplicationContext())
        .isEqualTo(ApplicationProvider.getApplicationContext());
  }

  @Test
  public void shouldReturnSameContentResolverEveryTime() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertThat(activity.getContentResolver()).isSameAs(activity.getContentResolver());

    assertThat(activity.getContentResolver()).isSameAs(Robolectric.setupActivity(Activity.class).getContentResolver());
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
  @Config(minSdk = 23)
  public void checkSelfPermission() {
    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    shadowContextWrapper.grantPermissions("MY_PERMISSON");

    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    assertThat(contextWrapper.checkSelfPermission("UNKNOWN_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
  }

  @Test
  public void checkPermissionUidPid() {
    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 1, 1))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    shadowContextWrapper.grantPermissions(1, 1, "MY_PERMISSON");

    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 2, 1))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 1, 1))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
  }

  @Test
  @Config(minSdk = 23)
  public void checkAdditionalSelfPermission() {
    shadowContextWrapper.grantPermissions("MY_PERMISSON");
    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    assertThat(contextWrapper.checkSelfPermission("ANOTHER_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    shadowContextWrapper.grantPermissions("ANOTHER_PERMISSON");
    assertThat(contextWrapper.checkSelfPermission("ANOTHER_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
  }

  @Test
  @Config(minSdk = 23)
  public void revokeSelfPermission() {
    shadowContextWrapper.grantPermissions("MY_PERMISSON");

    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    shadowContextWrapper.denyPermissions("MY_PERMISSON");

    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
  }

  @Test
  public void revokePermissionUidPid() {
    shadowContextWrapper.grantPermissions(1, 1, "MY_PERMISSON");

    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 1, 1))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    shadowContextWrapper.denyPermissions(1, 1, "MY_PERMISSON");

    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 1, 1))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
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
    assertEquals(
        "foo",
        shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedService()
            .getAction());
  }

  @Test
  public void startActivities_shouldStartAllActivities() {
    final Intent view = new Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    final Intent pick = new Intent(Intent.ACTION_PICK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    contextWrapper.startActivities(new Intent[] {view, pick});

    assertThat(ShadowApplication.getInstance().getNextStartedActivity()).isEqualTo(pick);
    assertThat(ShadowApplication.getInstance().getNextStartedActivity()).isEqualTo(view);
  }

  @Test
  public void startActivities_withBundle_shouldStartAllActivities() {
    final Intent view = new Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    final Intent pick = new Intent(Intent.ACTION_PICK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
    assertThat(ApplicationProvider.getApplicationContext().getPackageManager()).isNotNull();
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

    assertThat(transcript).containsExactly(
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
  public void removeSystemService_getSystemServiceReturnsNull() {
    shadowContextWrapper.removeSystemService(Context.WALLPAPER_SERVICE);
    assertThat(context.getSystemService(Context.WALLPAPER_SERVICE)).isNull();
  }
}
