package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionsManager;
import android.content.ServiceConnection;
import android.hardware.SystemSensorManager;
import android.hardware.fingerprint.FingerprintManager;
import android.media.session.MediaSessionManager;
import android.net.nsd.NsdManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.UserManager;
import android.os.Vibrator;
import android.print.PrintManager;
import android.telephony.SubscriptionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.autofill.AutofillManager;
import android.view.textclassifier.TextClassificationManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
public class ShadowApplicationTest {

  private Application context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void shouldBeAContext() throws Exception {
    assertThat(Robolectric.setupActivity(Activity.class).getApplication())
        .isSameAs(ApplicationProvider.getApplicationContext());
    assertThat(Robolectric.setupActivity(Activity.class).getApplication().getApplicationContext())
        .isSameAs(ApplicationProvider.getApplicationContext());
  }

  @Test
  public void shouldProvideServices() throws Exception {
    assertThat(context.getSystemService(Context.ACTIVITY_SERVICE))
        .isInstanceOf(android.app.ActivityManager.class);
    assertThat(context.getSystemService(Context.POWER_SERVICE))
        .isInstanceOf(android.os.PowerManager.class);
    assertThat(context.getSystemService(Context.ALARM_SERVICE))
        .isInstanceOf(android.app.AlarmManager.class);
    assertThat(context.getSystemService(Context.NOTIFICATION_SERVICE))
        .isInstanceOf(android.app.NotificationManager.class);
    assertThat(context.getSystemService(Context.KEYGUARD_SERVICE))
        .isInstanceOf(android.app.KeyguardManager.class);
    assertThat(context.getSystemService(Context.LOCATION_SERVICE))
        .isInstanceOf(android.location.LocationManager.class);
    assertThat(context.getSystemService(Context.SEARCH_SERVICE))
        .isInstanceOf(android.app.SearchManager.class);
    assertThat(context.getSystemService(Context.SENSOR_SERVICE))
        .isInstanceOf(SystemSensorManager.class);
    assertThat(context.getSystemService(Context.STORAGE_SERVICE))
        .isInstanceOf(android.os.storage.StorageManager.class);
    assertThat(context.getSystemService(Context.VIBRATOR_SERVICE)).isInstanceOf(Vibrator.class);
    assertThat(context.getSystemService(Context.CONNECTIVITY_SERVICE))
        .isInstanceOf(android.net.ConnectivityManager.class);
    assertThat(context.getSystemService(Context.WIFI_SERVICE))
        .isInstanceOf(android.net.wifi.WifiManager.class);
    assertThat(context.getSystemService(Context.AUDIO_SERVICE))
        .isInstanceOf(android.media.AudioManager.class);
    assertThat(context.getSystemService(Context.TELEPHONY_SERVICE))
        .isInstanceOf(android.telephony.TelephonyManager.class);
    assertThat(context.getSystemService(Context.INPUT_METHOD_SERVICE))
        .isInstanceOf(android.view.inputmethod.InputMethodManager.class);
    assertThat(context.getSystemService(Context.UI_MODE_SERVICE))
        .isInstanceOf(android.app.UiModeManager.class);
    assertThat(context.getSystemService(Context.DOWNLOAD_SERVICE))
        .isInstanceOf(android.app.DownloadManager.class);
    assertThat(context.getSystemService(Context.DEVICE_POLICY_SERVICE))
        .isInstanceOf(android.app.admin.DevicePolicyManager.class);
    assertThat(context.getSystemService(Context.DROPBOX_SERVICE))
        .isInstanceOf(android.os.DropBoxManager.class);
    assertThat(context.getSystemService(Context.MEDIA_ROUTER_SERVICE))
        .isInstanceOf(android.media.MediaRouter.class);
    assertThat(context.getSystemService(Context.ACCESSIBILITY_SERVICE))
        .isInstanceOf(AccessibilityManager.class);
    assertThat(context.getSystemService(Context.NSD_SERVICE)).isInstanceOf(NsdManager.class);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldProvideServicesIntroducedInJellyBeanMr1() throws Exception {
    assertThat(context.getSystemService(Context.DISPLAY_SERVICE))
        .isInstanceOf(android.hardware.display.DisplayManager.class);
    assertThat(context.getSystemService(Context.USER_SERVICE)).isInstanceOf(UserManager.class);
  }

  @Test
  @Config(minSdk = KITKAT)
  public void shouldProvideServicesIntroducedInKitKat() throws Exception {
    assertThat(context.getSystemService(Context.PRINT_SERVICE)).isInstanceOf(PrintManager.class);
    assertThat(context.getSystemService(Context.CAPTIONING_SERVICE))
        .isInstanceOf(CaptioningManager.class);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldProvideServicesIntroducedInLollipop() throws Exception {
    assertThat(context.getSystemService(Context.MEDIA_SESSION_SERVICE))
        .isInstanceOf(MediaSessionManager.class);
    assertThat(context.getSystemService(Context.BATTERY_SERVICE))
        .isInstanceOf(BatteryManager.class);
    assertThat(context.getSystemService(Context.RESTRICTIONS_SERVICE))
        .isInstanceOf(RestrictionsManager.class);
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void shouldProvideServicesIntroducedInLollipopMr1() throws Exception {
    assertThat(context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE))
        .isInstanceOf(SubscriptionManager.class);
  }

  @Test
  @Config(minSdk = M)
  public void shouldProvideServicesIntroducedMarshmallow() throws Exception {
    assertThat(context.getSystemService(Context.FINGERPRINT_SERVICE))
        .isInstanceOf(FingerprintManager.class);
  }

  @Test
  @Config(minSdk = O)
  public void shouldProvideServicesIntroducedOreo() throws Exception {
    // Context.AUTOFILL_MANAGER_SERVICE is marked @hide and this is the documented way to obtain
    // this service.
    AutofillManager autofillManager = context.getSystemService(AutofillManager.class);
    assertThat(autofillManager).isNotNull();

    assertThat(context.getSystemService(Context.TEXT_CLASSIFICATION_SERVICE))
        .isInstanceOf(TextClassificationManager.class);
  }

  @Test public void shouldProvideLayoutInflater() throws Exception {
    Object systemService = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    assertThat(systemService).isInstanceOf(LayoutInflater.class);
  }

  @Test
  @Config(minSdk = KITKAT)
  public void shouldCorrectlyInstantiatedAccessibilityService() throws Exception {
    AccessibilityManager accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

    AccessibilityManager.TouchExplorationStateChangeListener listener = createTouchListener();
    assertThat(accessibilityManager.addTouchExplorationStateChangeListener(listener)).isTrue();
    assertThat(accessibilityManager.removeTouchExplorationStateChangeListener(listener)).isTrue();
  }

  private static AccessibilityManager.TouchExplorationStateChangeListener createTouchListener() {
    return enabled -> {};
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWithDefaultValues() {
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    context.bindService(new Intent(""), service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isEqualTo(expectedComponentName);
    assertThat(service.service).isEqualTo(expectedBinder);
    assertThat(service.nameUnbound).isNull();
    context.unbindService(service);
    assertThat(service.nameUnbound).isEqualTo(expectedComponentName);
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWithNullValues() {
    TestService service = new TestService();
    context.bindService(new Intent(""), service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWhenNotPaused() {
    ShadowLooper.pauseMainLooper();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntent, expectedComponentName, expectedBinder);

    TestService service = new TestService();
    assertThat(context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE)).isTrue();

    assertThat(service.name).isNull();
    assertThat(service.service).isNull();

    ShadowLooper.unPauseMainLooper();

    assertThat(service.name).isEqualTo(expectedComponentName);
    assertThat(service.service).isEqualTo(expectedBinder);
  }

  @Test
  public void unbindServiceShouldCallOnServiceDisconnectedWhenNotPaused() {
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntent, expectedComponentName, expectedBinder);
    context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE);
    ShadowLooper.pauseMainLooper();

    context.unbindService(service);
    assertThat(service.nameUnbound).isNull();
    ShadowLooper.unPauseMainLooper();
    assertThat(service.nameUnbound).isEqualTo(expectedComponentName);
  }

  @Test
  public void unbindServiceAddsEntryToUnboundServicesCollection() {
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected");
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntent, expectedComponentName, expectedBinder);
    context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE);
    context.unbindService(service);
    assertThat(shadowApplication.getUnboundServiceConnections()).hasSize(1);
    assertThat(shadowApplication.getUnboundServiceConnections().get(0)).isSameAs(service);
  }

  @Test
  public void declaringServiceUnbindableMakesBindServiceReturnFalse() {
    ShadowLooper.pauseMainLooper();
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("refuseToBind");
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntent, expectedComponentName, expectedBinder);
    shadowApplication.declareActionUnbindable(expectedIntent.getAction());
    assertFalse(context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE));
    ShadowLooper.unPauseMainLooper();
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
    assertThat(shadowApplication.peekNextStartedService()).isNull();
  }

  @Test
  public void bindServiceWithMultipleIntentsMapping() {
    TestService service = new TestService();
    ComponentName expectedComponentNameOne = new ComponentName("package", "one");
    Binder expectedBinderOne = new Binder();
    Intent expectedIntentOne = new Intent("expected_one");
    ComponentName expectedComponentNameTwo = new ComponentName("package", "two");
    Binder expectedBinderTwo = new Binder();
    Intent expectedIntentTwo = new Intent("expected_two");
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentOne, expectedComponentNameOne, expectedBinderOne);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentTwo, expectedComponentNameTwo, expectedBinderTwo);
    context.bindService(expectedIntentOne, service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isEqualTo(expectedComponentNameOne);
    assertThat(service.service).isEqualTo(expectedBinderOne);
    context.bindService(expectedIntentTwo, service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isEqualTo(expectedComponentNameTwo);
    assertThat(service.service).isEqualTo(expectedBinderTwo);
  }

  @Test
  public void bindServiceWithMultipleIntentsMappingWithDefault() {
    TestService service = new TestService();
    ComponentName expectedComponentNameOne = new ComponentName("package", "one");
    Binder expectedBinderOne = new Binder();
    Intent expectedIntentOne = new Intent("expected_one");
    ComponentName expectedComponentNameTwo = new ComponentName("package", "two");
    Binder expectedBinderTwo = new Binder();
    Intent expectedIntentTwo = new Intent("expected_two");
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentOne, expectedComponentNameOne, expectedBinderOne);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentTwo, expectedComponentNameTwo, expectedBinderTwo);
    context.bindService(expectedIntentOne, service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isEqualTo(expectedComponentNameOne);
    assertThat(service.service).isEqualTo(expectedBinderOne);
    context.bindService(expectedIntentTwo, service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isEqualTo(expectedComponentNameTwo);
    assertThat(service.service).isEqualTo(expectedBinderTwo);
    context.bindService(new Intent("unknown"), service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
  }

  @Test
  public void unbindServiceWithMultipleIntentsMapping() {
    TestService serviceOne = new TestService();
    ComponentName expectedComponentNameOne = new ComponentName("package", "one");
    Binder expectedBinderOne = new Binder();
    Intent expectedIntentOne = new Intent("expected_one");
    TestService serviceTwo = new TestService();
    ComponentName expectedComponentNameTwo = new ComponentName("package", "two");
    Binder expectedBinderTwo = new Binder();
    Intent expectedIntentTwo = new Intent("expected_two");
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentOne, expectedComponentNameOne, expectedBinderOne);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentTwo, expectedComponentNameTwo, expectedBinderTwo);

    context.bindService(expectedIntentOne, serviceOne, Context.BIND_AUTO_CREATE);
    assertThat(serviceOne.nameUnbound).isNull();
    context.unbindService(serviceOne);
    assertThat(serviceOne.name).isEqualTo(expectedComponentNameOne);

    context.bindService(expectedIntentTwo, serviceTwo, Context.BIND_AUTO_CREATE);
    assertThat(serviceTwo.nameUnbound).isNull();
    context.unbindService(serviceTwo);
    assertThat(serviceTwo.name).isEqualTo(expectedComponentNameTwo);

    TestService serviceDefault = new TestService();
    context.bindService(new Intent("default"), serviceDefault, Context.BIND_AUTO_CREATE);
    assertThat(serviceDefault.nameUnbound).isNull();
    context.unbindService(serviceDefault);
    assertThat(serviceDefault.name).isNull();
  }

  @Test
  public void shouldHaveStoppedServiceIntentAndIndicateServiceWasntRunning() {
    ShadowApplication shadowApplication = Shadows.shadowOf(context);

    Activity activity = Robolectric.setupActivity(Activity.class);

    Intent intent = getSomeActionIntent("some.action");

    boolean wasRunning = activity.stopService(intent);

    assertFalse(wasRunning);
    assertThat(shadowApplication.getNextStoppedService()).isEqualTo(intent);
  }

  private Intent getSomeActionIntent(String action) {
    Intent intent = new Intent();
    intent.setAction(action);
    return intent;
  }

  @Test
  public void shouldHaveStoppedServiceIntentAndIndicateServiceWasRunning() {
    ShadowApplication shadowApplication = shadowOf(context);

    Activity activity = Robolectric.setupActivity(Activity.class);

    Intent intent = getSomeActionIntent("some.action");

    activity.startService(intent);

    boolean wasRunning = activity.stopService(intent);

    assertTrue(wasRunning);
    assertThat(shadowApplication.getNextStoppedService()).isEqualTo(intent);
  }

  @Test
  public void shouldHaveStoppedServiceByStartedComponent() {
    ShadowApplication shadowApplication = shadowOf(context);

    Activity activity = Robolectric.setupActivity(Activity.class);

    ComponentName componentName = new ComponentName("package.test", "package.test.TestClass");
    Intent startServiceIntent = new Intent().setComponent(componentName);

    ComponentName startedComponent = activity.startService(startServiceIntent);
    assertThat(startedComponent.getPackageName()).isEqualTo("package.test");
    assertThat(startedComponent.getClassName()).isEqualTo("package.test.TestClass");

    Intent stopServiceIntent = new Intent().setComponent(startedComponent);
    stopServiceIntent.putExtra("someExtra", "someValue");
    boolean wasRunning = activity.stopService(stopServiceIntent);

    assertTrue(wasRunning);
    final Intent nextStoppedService = shadowApplication.getNextStoppedService();
    assertThat(nextStoppedService.filterEquals(startServiceIntent)).isTrue();
    assertThat(nextStoppedService.getStringExtra("someExtra")).isEqualTo("someValue");
  }

  @Test
  public void shouldClearStartedServiceIntents() {
    context.startService(getSomeActionIntent("some.action"));
    context.startService(getSomeActionIntent("another.action"));

    shadowOf(context).clearStartedServices();

    assertNull(shadowOf(context).getNextStartedService());
  }

  @Test
  public void shouldThrowIfContainsRegisteredReceiverOfAction() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.registerReceiver(new TestBroadcastReceiver(), new IntentFilter("Foo"));

    try {
      shadowOf(context).assertNoBroadcastListenersOfActionRegistered(activity, "Foo");

      fail("should have thrown IllegalStateException");
    } catch (IllegalStateException e) {
      // ok
    }
  }

  @Test
  public void shouldNotThrowIfDoesNotContainsRegisteredReceiverOfAction() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.registerReceiver(new TestBroadcastReceiver(), new IntentFilter("Foo"));

    shadowOf(context).assertNoBroadcastListenersOfActionRegistered(activity, "Bar");
  }

  @Test
  public void canAnswerIfReceiverIsRegisteredForIntent() throws Exception {
    BroadcastReceiver expectedReceiver = new TestBroadcastReceiver();
    ShadowApplication shadowApplication = shadowOf(context);
    assertFalse(shadowApplication.hasReceiverForIntent(new Intent("Foo")));
    context.registerReceiver(expectedReceiver, new IntentFilter("Foo"));

    assertTrue(shadowApplication.hasReceiverForIntent(new Intent("Foo")));
  }

  @Test
  public void canFindAllReceiversForAnIntent() throws Exception {
    BroadcastReceiver expectedReceiver = new TestBroadcastReceiver();
    ShadowApplication shadowApplication = shadowOf(context);
    assertFalse(shadowApplication.hasReceiverForIntent(new Intent("Foo")));
    context.registerReceiver(expectedReceiver, new IntentFilter("Foo"));
    context.registerReceiver(expectedReceiver, new IntentFilter("Foo"));

    assertThat(shadowApplication.getReceiversForIntent(new Intent("Foo"))).hasSize(2);
  }

  @Test
  public void broadcasts_shouldBeLogged() {
    Intent broadcastIntent = new Intent("foo");
    context.sendBroadcast(broadcastIntent);

    List<Intent> broadcastIntents = shadowOf(context).getBroadcastIntents();
    assertThat(broadcastIntents).hasSize(1);
    assertThat(broadcastIntents.get(0)).isEqualTo(broadcastIntent);
  }

  @Test
  public void sendStickyBroadcast() {
    Intent broadcastIntent = new Intent("Foo");
    context.sendStickyBroadcast(broadcastIntent);

    // Register after the broadcast has fired. We should immediately get a sticky event.
    TestBroadcastReceiver receiver = new TestBroadcastReceiver();
    context.registerReceiver(receiver, new IntentFilter("Foo"));
    assertTrue(receiver.isSticky);

    // Fire the broadcast again, and we should get a non-sticky event.
    context.sendStickyBroadcast(broadcastIntent);
    assertFalse(receiver.isSticky);
  }

  @Test
  public void shouldRememberResourcesAfterLazilyLoading() throws Exception {
    assertSame(context.getResources(), context.getResources());
  }

  @Test
  public void startActivity_whenActivityCheckingEnabled_checksPackageManagerResolveInfo() throws Exception {
    shadowOf(context).checkActivities(true);

    String action = "com.does.not.exist.android.app.v2.mobile";

    try {
      context.startActivity(new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      fail("Expected startActivity to throw ActivityNotFoundException!");
    } catch (ActivityNotFoundException e) {
      assertThat(e.getMessage()).contains(action);
      assertThat(shadowOf(context).getNextStartedActivity()).isNull();
    }
  }

  @Test
  public void bindServiceShouldAddServiceConnectionToListOfBoundServiceConnections() {
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    final ServiceConnection expectedServiceConnection = new EmptyServiceConnection();

    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(0);
    assertThat(context.bindService(new Intent("connect"), expectedServiceConnection, 0)).isTrue();
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(1);
    assertThat(shadowApplication.getBoundServiceConnections().get(0))
        .isSameAs(expectedServiceConnection);
  }

  @Test
  public void bindServiceShouldAddServiceConnectionToListOfBoundServiceConnectionsEvenIfServiceUnboundable() {
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    final ServiceConnection expectedServiceConnection = new EmptyServiceConnection();
    final String unboundableAction = "refuse";
    final Intent serviceIntent = new Intent(unboundableAction);
    shadowApplication.declareActionUnbindable(unboundableAction);
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(0);
    assertThat(context.bindService(serviceIntent, expectedServiceConnection, 0)).isFalse();
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(1);
    assertThat(shadowApplication.getBoundServiceConnections().get(0)).isSameAs(expectedServiceConnection);
  }

  @Test
  public void unbindServiceShouldRemoveServiceConnectionFromListOfBoundServiceConnections() {
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    final ServiceConnection expectedServiceConnection = new EmptyServiceConnection();

    assertThat(context.bindService(new Intent("connect"), expectedServiceConnection, 0)).isTrue();
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(1);
    assertThat(shadowApplication.getUnboundServiceConnections()).hasSize(0);
    context.unbindService(expectedServiceConnection);
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(0);
    assertThat(shadowApplication.getUnboundServiceConnections()).hasSize(1);
    assertThat(shadowApplication.getUnboundServiceConnections().get(0))
        .isSameAs(expectedServiceConnection);
  }

  @Test
  public void getThreadScheduler_shouldMatchRobolectricValue() {
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    assertThat(shadowApplication.getForegroundThreadScheduler()).isSameAs(Robolectric.getForegroundThreadScheduler());
    assertThat(shadowApplication.getBackgroundThreadScheduler()).isSameAs(Robolectric.getBackgroundThreadScheduler());
  }

  @Test
  public void getForegroundThreadScheduler_shouldMatchRuntimeEnvironment() {
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    assertThat(shadowApplication.getForegroundThreadScheduler()).isSameAs(s);
  }

  @Test
  public void getBackgroundThreadScheduler_shouldDifferFromRuntimeEnvironment_byDefault() {
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    assertThat(shadowApplication.getBackgroundThreadScheduler()).isNotSameAs(RuntimeEnvironment.getMasterScheduler());
  }

  @Test
  public void getBackgroundThreadScheduler_shouldDifferFromRuntimeEnvironment_withAdvancedScheduling() {
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    final ShadowApplication shadowApplication = Shadows.shadowOf(context);
    assertThat(shadowApplication.getBackgroundThreadScheduler()).isNotSameAs(s);
  }

  @Test
  public void getLatestPopupWindow() {
    PopupWindow pw = new PopupWindow(new LinearLayout(context));

    pw.showAtLocation(new LinearLayout(context), Gravity.CENTER, 0, 0);

    PopupWindow latestPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
    assertThat(latestPopupWindow).isSameAs(pw);
  }

  /////////////////////////////

  private static class EmptyServiceConnection implements ServiceConnection {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {}

    @Override
    public void onServiceDisconnected(ComponentName name) {}
  }

  public static class TestBroadcastReceiver extends BroadcastReceiver {
    public Context context;
    public Intent intent;
    public boolean isSticky;

    @Override
    public void onReceive(Context context, Intent intent) {
      this.context = context;
      this.intent = intent;
      this.isSticky = isInitialStickyBroadcast();
    }
  }
}
