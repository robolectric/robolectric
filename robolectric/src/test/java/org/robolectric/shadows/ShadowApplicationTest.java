package org.robolectric.shadows;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
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
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.UserManager;
import android.print.PrintManager;
import android.telephony.SubscriptionManager;
import android.view.Gravity;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.autofill.AutofillManager;
import android.view.textclassifier.TextClassificationManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.util.Scheduler;

@RunWith(RobolectricTestRunner.class)
public class ShadowApplicationTest {

  @Test
  @Config(packageName = "override.package")
  public void shouldOverridePackageWithConfig() {
    assertThat(RuntimeEnvironment.application.getPackageName()).isEqualTo("override.package");
  }

  @Test
  public void shouldBeAContext() throws Exception {
    assertThat(Robolectric.setupActivity(Activity.class).getApplication()).isSameAs(RuntimeEnvironment.application);
    assertThat(Robolectric.setupActivity(Activity.class).getApplication().getApplicationContext()).isSameAs(RuntimeEnvironment.application);
  }

  @Test
  public void shouldProvideServices() throws Exception {
    checkSystemService(Context.ACTIVITY_SERVICE, android.app.ActivityManager.class);
    checkSystemService(Context.POWER_SERVICE, android.os.PowerManager.class);
    checkSystemService(Context.ALARM_SERVICE, android.app.AlarmManager.class);
    checkSystemService(Context.NOTIFICATION_SERVICE, android.app.NotificationManager.class);
    checkSystemService(Context.KEYGUARD_SERVICE, android.app.KeyguardManager.class);
    checkSystemService(Context.LOCATION_SERVICE, android.location.LocationManager.class);
    checkSystemService(Context.SEARCH_SERVICE, android.app.SearchManager.class);
    checkSystemService(Context.SENSOR_SERVICE, SystemSensorManager.class);
    checkSystemService(Context.STORAGE_SERVICE, android.os.storage.StorageManager.class);
    checkSystemService(Context.VIBRATOR_SERVICE, ShadowVibrator.class);
    checkSystemService(Context.CONNECTIVITY_SERVICE, android.net.ConnectivityManager.class);
    checkSystemService(Context.WIFI_SERVICE, android.net.wifi.WifiManager.class);
    checkSystemService(Context.AUDIO_SERVICE, android.media.AudioManager.class);
    checkSystemService(Context.TELEPHONY_SERVICE, android.telephony.TelephonyManager.class);
    checkSystemService(Context.INPUT_METHOD_SERVICE, android.view.inputmethod.InputMethodManager.class);
    checkSystemService(Context.UI_MODE_SERVICE, android.app.UiModeManager.class);
    checkSystemService(Context.DOWNLOAD_SERVICE, android.app.DownloadManager.class);
    checkSystemService(Context.DEVICE_POLICY_SERVICE, android.app.admin.DevicePolicyManager.class);
    checkSystemService(Context.DROPBOX_SERVICE, android.os.DropBoxManager.class);
    checkSystemService(Context.MEDIA_ROUTER_SERVICE, android.media.MediaRouter.class);
    checkSystemService(Context.ACCESSIBILITY_SERVICE, android.view.accessibility.AccessibilityManager.class);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldProvideServicesIntroducedInJellyBeanMr1() throws Exception {
    checkSystemService(Context.DISPLAY_SERVICE, android.hardware.display.DisplayManager.class);
    checkSystemService(Context.USER_SERVICE, UserManager.class);
  }

  @Test
  @Config(minSdk = KITKAT)
  public void shouldProvideServicesIntroducedInKitKat() throws Exception {
    checkSystemService(Context.PRINT_SERVICE, PrintManager.class);
    checkSystemService(Context.CAPTIONING_SERVICE, CaptioningManager.class);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldProvideMediaSessionService() throws Exception {
    checkSystemService(Context.MEDIA_SESSION_SERVICE, MediaSessionManager.class);
    checkSystemService(Context.BATTERY_SERVICE, BatteryManager.class);
    checkSystemService(Context.RESTRICTIONS_SERVICE, RestrictionsManager.class);
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void shouldProvideServicesIntroducedInLollipopMr1() throws Exception {
    checkSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE, SubscriptionManager.class);
  }

  @Test
  @Config(minSdk = M)
  public void shouldProvideServicesIntroducedMarshmallow() throws Exception {
    checkSystemService(Context.FINGERPRINT_SERVICE, FingerprintManager.class);
  }

  @Test
  @Config(minSdk = O)
  public void shouldProvideServicesIntroducedOreo() throws Exception {
    // Context.AUTOFILL_MANAGER_SERVICE is marked @hide and this is the documented way to obtain this
    // service.
    AutofillManager autofillManager = RuntimeEnvironment.application.getSystemService(AutofillManager.class);
    assertThat(autofillManager).isNotNull();

    checkSystemService(Context.TEXT_CLASSIFICATION_SERVICE, TextClassificationManager.class);
  }

  @Test public void shouldProvideLayoutInflater() throws Exception {
    Object systemService = RuntimeEnvironment.application.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    assertThat(systemService).isInstanceOf(RoboLayoutInflater.class);
  }

  @Test
  @Config(minSdk = KITKAT)
  public void shouldCorrectlyInstantiatedAccessibilityService() throws Exception {
    AccessibilityManager accessibilityManager = (AccessibilityManager) RuntimeEnvironment.application.getSystemService(Context.ACCESSIBILITY_SERVICE);

    AccessibilityManager.TouchExplorationStateChangeListener listener = createTouchListener();
    assertThat(accessibilityManager.addTouchExplorationStateChangeListener(listener)).isTrue();
    assertThat(accessibilityManager.removeTouchExplorationStateChangeListener(listener)).isTrue();
  }

  private static AccessibilityManager.TouchExplorationStateChangeListener createTouchListener() {
    return new AccessibilityManager.TouchExplorationStateChangeListener() {
      @Override
      public void onTouchExplorationStateChanged(boolean enabled) { }
    };
  }

  private void checkSystemService(String name, Class expectedClass) {
    Object systemService = RuntimeEnvironment.application.getSystemService(name);
    assertThat(systemService).isInstanceOf(expectedClass);
    assertThat(systemService).isSameAs(RuntimeEnvironment.application.getSystemService(name));
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWithDefaultValues() {
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(RuntimeEnvironment.application).setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    RuntimeEnvironment.application.bindService(new Intent(""), service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isEqualTo(expectedComponentName);
    assertThat(service.service).isEqualTo(expectedBinder);
    assertThat(service.nameUnbound).isNull();
    RuntimeEnvironment.application.unbindService(service);
    assertThat(service.nameUnbound).isEqualTo(expectedComponentName);
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWithNullValues() {
    TestService service = new TestService();
    RuntimeEnvironment.application.bindService(new Intent(""), service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWhenNotPaused() {
    ShadowLooper.pauseMainLooper();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected");
    Shadows.shadowOf(RuntimeEnvironment.application).setComponentNameAndServiceForBindServiceForIntent(expectedIntent, expectedComponentName, expectedBinder);

    TestService service = new TestService();
    assertThat(RuntimeEnvironment.application.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE)).isTrue();

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
    Shadows.shadowOf(RuntimeEnvironment.application).setComponentNameAndServiceForBindServiceForIntent(expectedIntent, expectedComponentName, expectedBinder);
    RuntimeEnvironment.application.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE);
    ShadowLooper.pauseMainLooper();

    RuntimeEnvironment.application.unbindService(service);
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
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntent, expectedComponentName, expectedBinder);
    RuntimeEnvironment.application.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE);
    RuntimeEnvironment.application.unbindService(service);
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
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntent, expectedComponentName, expectedBinder);
    shadowApplication.declareActionUnbindable(expectedIntent.getAction());
    assertFalse(RuntimeEnvironment.application.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE));
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
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentOne, expectedComponentNameOne, expectedBinderOne);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentTwo, expectedComponentNameTwo, expectedBinderTwo);
    RuntimeEnvironment.application.bindService(expectedIntentOne, service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isEqualTo(expectedComponentNameOne);
    assertThat(service.service).isEqualTo(expectedBinderOne);
    RuntimeEnvironment.application.bindService(expectedIntentTwo, service, Context.BIND_AUTO_CREATE);
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
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentOne, expectedComponentNameOne, expectedBinderOne);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentTwo, expectedComponentNameTwo, expectedBinderTwo);
    RuntimeEnvironment.application.bindService(expectedIntentOne, service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isEqualTo(expectedComponentNameOne);
    assertThat(service.service).isEqualTo(expectedBinderOne);
    RuntimeEnvironment.application.bindService(expectedIntentTwo, service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isEqualTo(expectedComponentNameTwo);
    assertThat(service.service).isEqualTo(expectedBinderTwo);
    RuntimeEnvironment.application.bindService(new Intent("unknown"), service, Context.BIND_AUTO_CREATE);
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
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentOne, expectedComponentNameOne, expectedBinderOne);
    shadowApplication.setComponentNameAndServiceForBindServiceForIntent(expectedIntentTwo, expectedComponentNameTwo, expectedBinderTwo);

    RuntimeEnvironment.application.bindService(expectedIntentOne, serviceOne, Context.BIND_AUTO_CREATE);
    assertThat(serviceOne.nameUnbound).isNull();
    RuntimeEnvironment.application.unbindService(serviceOne);
    assertThat(serviceOne.name).isEqualTo(expectedComponentNameOne);

    RuntimeEnvironment.application.bindService(expectedIntentTwo, serviceTwo, Context.BIND_AUTO_CREATE);
    assertThat(serviceTwo.nameUnbound).isNull();
    RuntimeEnvironment.application.unbindService(serviceTwo);
    assertThat(serviceTwo.name).isEqualTo(expectedComponentNameTwo);

    TestService serviceDefault = new TestService();
    RuntimeEnvironment.application.bindService(new Intent("default"), serviceDefault, Context.BIND_AUTO_CREATE);
    assertThat(serviceDefault.nameUnbound).isNull();
    RuntimeEnvironment.application.unbindService(serviceDefault);
    assertThat(serviceDefault.name).isNull();
  }

  @Test
  public void shouldHaveStoppedServiceIntentAndIndicateServiceWasntRunning() {
    ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);

    Activity activity = Robolectric.setupActivity(Activity.class);

    Intent intent = getSomeActionIntent("some.action");

    boolean wasRunning = activity.stopService(intent);

    assertFalse(wasRunning);
    assertEquals(intent, shadowApplication.getNextStoppedService());
  }

  private Intent getSomeActionIntent(String action) {
    Intent intent = new Intent();
    intent.setAction(action);
    return intent;
  }

  @Test
  public void shouldHaveStoppedServiceIntentAndIndicateServiceWasRunning() {
    ShadowApplication shadowApplication = shadowOf(RuntimeEnvironment.application);

    Activity activity = Robolectric.setupActivity(Activity.class);

    Intent intent = getSomeActionIntent("some.action");

    activity.startService(intent);

    boolean wasRunning = activity.stopService(intent);

    assertTrue(wasRunning);
    assertEquals(intent, shadowApplication.getNextStoppedService());
  }

  @Test
  public void shouldHaveStoppedServiceByStartedComponent() {
    ShadowApplication shadowApplication = shadowOf(RuntimeEnvironment.application);

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
    ShadowApplication shadowApplication = shadowOf(RuntimeEnvironment.application);
    shadowApplication.startService(getSomeActionIntent("some.action"));
    shadowApplication.startService(getSomeActionIntent("another.action"));

    shadowApplication.clearStartedServices();

    assertNull(shadowApplication.getNextStartedService());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIfContainsRegisteredReceiverOfAction() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.registerReceiver(new TestBroadcastReceiver(), new IntentFilter("Foo"));

    shadowOf(RuntimeEnvironment.application).assertNoBroadcastListenersOfActionRegistered(activity, "Foo");
  }

  @Test
  public void shouldNotThrowIfDoesNotContainsRegisteredReceiverOfAction() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.registerReceiver(new TestBroadcastReceiver(), new IntentFilter("Foo"));

    shadowOf(RuntimeEnvironment.application).assertNoBroadcastListenersOfActionRegistered(activity, "Bar");
  }

  @Test
  public void canAnswerIfReceiverIsRegisteredForIntent() throws Exception {
    BroadcastReceiver expectedReceiver = new TestBroadcastReceiver();
    ShadowApplication shadowApplication = shadowOf(RuntimeEnvironment.application);
    assertFalse(shadowApplication.hasReceiverForIntent(new Intent("Foo")));
    RuntimeEnvironment.application.registerReceiver(expectedReceiver, new IntentFilter("Foo"));

    assertTrue(shadowApplication.hasReceiverForIntent(new Intent("Foo")));
  }

  @Test
  public void canFindAllReceiversForAnIntent() throws Exception {
    BroadcastReceiver expectedReceiver = new TestBroadcastReceiver();
    ShadowApplication shadowApplication = shadowOf(RuntimeEnvironment.application);
    assertFalse(shadowApplication.hasReceiverForIntent(new Intent("Foo")));
    RuntimeEnvironment.application.registerReceiver(expectedReceiver, new IntentFilter("Foo"));
    RuntimeEnvironment.application.registerReceiver(expectedReceiver, new IntentFilter("Foo"));

    assertTrue(shadowApplication.getReceiversForIntent(new Intent("Foo")).size() == 2);
  }

  @Test
  public void broadcasts_shouldBeLogged() {
    Intent broadcastIntent = new Intent("foo");
    RuntimeEnvironment.application.sendBroadcast(broadcastIntent);

    List<Intent> broadcastIntents = shadowOf(RuntimeEnvironment.application).getBroadcastIntents();
    assertTrue(broadcastIntents.size() == 1);
    assertEquals(broadcastIntent, broadcastIntents.get(0));
  }

  @Test
  public void shouldRememberResourcesAfterLazilyLoading() throws Exception {
    assertSame(RuntimeEnvironment.application.getResources(), RuntimeEnvironment.application.getResources());
  }

  @Test
  public void checkPermission_shouldTrackGrantedAndDeniedPermissions() throws Exception {
    Application application = RuntimeEnvironment.application;
    shadowOf(application).grantPermissions("foo", "bar");
    shadowOf(application).denyPermissions("foo", "qux");
    assertThat(application.checkPermission("foo", -1, -1)).isEqualTo(PERMISSION_DENIED);
    assertThat(application.checkPermission("bar", -1, -1)).isEqualTo(PERMISSION_GRANTED);
    assertThat(application.checkPermission("baz", -1, -1)).isEqualTo(PERMISSION_DENIED);
    assertThat(application.checkPermission("qux", -1, -1)).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void startActivity_whenActivityCheckingEnabled_checksPackageManagerResolveInfo() throws Exception {
    Application application = RuntimeEnvironment.application;
    shadowOf(application).checkActivities(true);

    String action = "com.does.not.exist.android.app.v2.mobile";

    try {
      application.startActivity(new Intent(action));
      fail("Expected startActivity to throw ActivityNotFoundException!");
    } catch (ActivityNotFoundException e) {
      assertThat(e.getMessage()).contains(action);
      assertThat(shadowOf(application).getNextStartedActivity()).isNull();
    }
  }

  @Test
  public void bindServiceShouldAddServiceConnectionToListOfBoundServiceConnections() {
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    final ServiceConnection expectedServiceConnection = new EmptyServiceConnection();

    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(0);
    assertThat(shadowApplication.bindService(new Intent("connect"), expectedServiceConnection, 0)).isTrue();
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(1);
    assertThat(shadowApplication.getBoundServiceConnections().get(0)).isSameAs(expectedServiceConnection);
  }

  @Test
  public void bindServiceShouldAddServiceConnectionToListOfBoundServiceConnectionsEvenIfServiceUnboundable() {
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    final ServiceConnection expectedServiceConnection = new EmptyServiceConnection();
    final String unboundableAction = "refuse";
    final Intent serviceIntent = new Intent(unboundableAction);
    shadowApplication.declareActionUnbindable(unboundableAction);
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(0);
    assertThat(shadowApplication.bindService(serviceIntent, expectedServiceConnection, 0)).isFalse();
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(1);
    assertThat(shadowApplication.getBoundServiceConnections().get(0)).isSameAs(expectedServiceConnection);
  }

  @Test
  public void unbindServiceShouldRemoveServiceConnectionFromListOfBoundServiceConnections() {
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    final ServiceConnection expectedServiceConnection = new EmptyServiceConnection();

    assertThat(shadowApplication.bindService(new Intent("connect"), expectedServiceConnection, 0)).isTrue();
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(1);
    assertThat(shadowApplication.getUnboundServiceConnections()).hasSize(0);
    shadowApplication.unbindService(expectedServiceConnection);
    assertThat(shadowApplication.getBoundServiceConnections()).hasSize(0);
    assertThat(shadowApplication.getUnboundServiceConnections()).hasSize(1);
    assertThat(shadowApplication.getUnboundServiceConnections().get(0)).isSameAs(expectedServiceConnection);
  }

  @Test
  public void getThreadScheduler_shouldMatchRobolectricValue() {
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    assertThat(shadowApplication.getForegroundThreadScheduler()).isSameAs(Robolectric.getForegroundThreadScheduler());
    assertThat(shadowApplication.getBackgroundThreadScheduler()).isSameAs(Robolectric.getBackgroundThreadScheduler());
  }

  @Test
  public void getForegroundThreadScheduler_shouldMatchRuntimeEnvironment() {
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    assertThat(shadowApplication.getForegroundThreadScheduler()).isSameAs(s);
  }

  @Test
  public void getBackgroundThreadScheduler_shouldDifferFromRuntimeEnvironment_byDefault() {
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    assertThat(shadowApplication.getBackgroundThreadScheduler()).isNotSameAs(RuntimeEnvironment.getMasterScheduler());
  }

  @Test
  public void getBackgroundThreadScheduler_shouldDifferFromRuntimeEnvironment_withAdvancedScheduling() {
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    final ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    assertThat(shadowApplication.getBackgroundThreadScheduler()).isNotSameAs(s);
  }

  @Test
  public void getLatestPopupWindow() {
    PopupWindow pw = new PopupWindow(new LinearLayout(RuntimeEnvironment.application));

    pw.showAtLocation(new LinearLayout(RuntimeEnvironment.application), Gravity.CENTER, 0, 0);

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

    @Override
    public void onReceive(Context context, Intent intent) {
      this.context = context;
      this.intent = intent;
    }
  }
}
