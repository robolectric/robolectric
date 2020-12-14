package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

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
import android.os.Build.VERSION_CODES;
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
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.testing.TestActivity;
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
        .isSameInstanceAs(ApplicationProvider.getApplicationContext());
    assertThat(Robolectric.setupActivity(Activity.class).getApplication().getApplicationContext())
        .isSameInstanceAs(ApplicationProvider.getApplicationContext());
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

  @Test
  public void shouldProvideLayoutInflater() throws Exception {
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
  public void bindServiceShouldThrowIfSetToThrow() {
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    SecurityException expectedException = new SecurityException("expected");
    Shadows.shadowOf(context).setThrowInBindService(expectedException);

    try {
      context.bindService(new Intent("").setPackage("package"), service, Context.BIND_AUTO_CREATE);
      fail("bindService should throw SecurityException!");
    } catch (SecurityException thrownException) {
      assertThat(thrownException).isEqualTo(expectedException);
    }
  }

  @Test
  public void
      setBindServiceCallsOnServiceConnectedDirectly_setToTrue_onServiceConnectedCalledDuringCall() {
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    Shadows.shadowOf(context).setBindServiceCallsOnServiceConnectedDirectly(true);

    context.bindService(new Intent("").setPackage("package"), service, Context.BIND_AUTO_CREATE);

    assertThat(service.service).isNotNull();
  }

  @Test
  public void
      setBindServiceCallsOnServiceConnectedDirectly_setToTrue_locksUntilBound_onServiceConnectedCalledDuringCall()
          throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    TestService service =
        new TestService() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
            super.onServiceConnected(name, service);
            latch.countDown();
          }
        };
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    Shadows.shadowOf(context).setBindServiceCallsOnServiceConnectedDirectly(true);

    context.bindService(new Intent("").setPackage("package"), service, Context.BIND_AUTO_CREATE);

    // Lock waiting for onService connected to finish
    assertThat(latch.await(1000, MILLISECONDS)).isTrue();
    assertThat(service.service).isNotNull();
  }

  @Test
  public void
      setBindServiceCallsOnServiceConnectedDirectly_setToFalse_onServiceConnectedNotCalledDuringCall() {
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    Shadows.shadowOf(context).setBindServiceCallsOnServiceConnectedDirectly(false);

    context.bindService(new Intent("").setPackage("package"), service, Context.BIND_AUTO_CREATE);

    assertThat(service.service).isNull();
  }

  @Test
  public void
      setBindServiceCallsOnServiceConnectedDirectly_setToFalse_locksUntilBound_onServiceConnectedCalledDuringCall()
          throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    TestService service =
        new TestService() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
            super.onServiceConnected(name, service);
            latch.countDown();
          }
        };
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    Shadows.shadowOf(context).setBindServiceCallsOnServiceConnectedDirectly(false);

    context.bindService(new Intent("").setPackage("package"), service, Context.BIND_AUTO_CREATE);

    // Lock waiting for onService connected to finish
    assertThat(latch.await(1000, MILLISECONDS)).isFalse();
    assertThat(service.service).isNull();

    // After idling the callback has been made.
    ShadowLooper.idleMainLooper();

    assertThat(latch.await(1000, MILLISECONDS)).isTrue();
    assertThat(service.service).isNotNull();
  }

  @Test
  public void
      setBindServiceCallsOnServiceConnectedDirectly_notSet_onServiceConnectedNotCalledDuringCall() {
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);

    context.bindService(new Intent("").setPackage("package"), service, Context.BIND_AUTO_CREATE);

    assertThat(service.service).isNull();
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWithDefaultValues_ifFlagUnset() {
    Shadows.shadowOf(context).setUnbindServiceCallsOnServiceDisconnected(false);
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    context.bindService(new Intent("").setPackage("package"), service, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(service.name).isEqualTo(expectedComponentName);
    assertThat(service.service).isEqualTo(expectedBinder);
    assertThat(service.nameDisconnected).isNull();
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWithDefaultValues_ifFlagSet() {
    Shadows.shadowOf(context).setUnbindServiceCallsOnServiceDisconnected(true);
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    context.bindService(new Intent("").setPackage("package"), service, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(service.name).isEqualTo(expectedComponentName);
    assertThat(service.service).isEqualTo(expectedBinder);
    assertThat(service.nameDisconnected).isNull();
    context.unbindService(service);
    shadowMainLooper().idle();
    assertThat(service.nameDisconnected).isEqualTo(expectedComponentName);
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWithNullValues() {
    TestService service = new TestService();
    context.bindService(new Intent("").setPackage("package"), service, Context.BIND_AUTO_CREATE);
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
  }

  @Test
  public void bindServiceShouldCallOnServiceConnectedWhenNotPaused() {
    shadowMainLooper().pause();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntent, expectedComponentName, expectedBinder);

    TestService service = new TestService();
    assertThat(context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE)).isTrue();

    assertThat(service.name).isNull();
    assertThat(service.service).isNull();

    shadowMainLooper().idle();

    assertThat(service.name).isEqualTo(expectedComponentName);
    assertThat(service.service).isEqualTo(expectedBinder);
  }

  @Test
  public void unbindServiceShouldNotCallOnServiceDisconnected_ifFlagUnset() {
    Shadows.shadowOf(context).setUnbindServiceCallsOnServiceDisconnected(false);
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntent, expectedComponentName, expectedBinder);
    context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE);

    context.unbindService(service);

    shadowMainLooper().idle();
    assertThat(service.name).isEqualTo(expectedComponentName);
    assertThat(service.service).isEqualTo(expectedBinder);
    assertThat(service.nameDisconnected).isNull();
  }

  @Test
  public void unbindServiceShouldCallOnServiceDisconnectedWhenNotPaused_ifFlagSet() {
    Shadows.shadowOf(context).setUnbindServiceCallsOnServiceDisconnected(true);
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntent, expectedComponentName, expectedBinder);
    context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE);
    shadowMainLooper().pause();

    context.unbindService(service);
    assertThat(service.nameDisconnected).isNull();
    shadowMainLooper().idle();
    assertThat(service.nameDisconnected).isEqualTo(expectedComponentName);
  }

  @Test
  public void unbindServiceAddsEntryToUnboundServicesCollection() {
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntent, expectedComponentName, expectedBinder);
    context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE);
    context.unbindService(service);
    assertThat(Shadows.shadowOf(context).getUnboundServiceConnections()).hasSize(1);
    assertThat(Shadows.shadowOf(context).getUnboundServiceConnections().get(0))
        .isSameInstanceAs(service);
  }

  @Test
  public void declaringActionUnbindableMakesBindServiceReturnFalse() {
    shadowMainLooper().pause();
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("", "");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("refuseToBind").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntent, expectedComponentName, expectedBinder);
    Shadows.shadowOf(context).declareActionUnbindable(expectedIntent.getAction());
    assertFalse(context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE));
    shadowMainLooper().idle();
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
    assertThat(Shadows.shadowOf(context).peekNextStartedService()).isNull();
  }

  @Test
  public void declaringComponentUnbindableMakesBindServiceReturnFalse_intentWithComponent() {
    shadowMainLooper().pause();
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("unbindable", "service");
    Intent intent = new Intent("unbindable").setComponent(expectedComponentName);
    Shadows.shadowOf(context).declareComponentUnbindable(expectedComponentName);
    assertThat(context.bindService(intent, service, Context.BIND_AUTO_CREATE)).isFalse();
    shadowMainLooper().idle();
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
    assertThat(Shadows.shadowOf(context).peekNextStartedService()).isNull();
  }

  @Test
  public void declaringComponentUnbindableMakesBindServiceReturnFalse_intentWithoutComponent() {
    shadowMainLooper().pause();
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("unbindable", "service");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntent, expectedComponentName, expectedBinder);
    Shadows.shadowOf(context).declareComponentUnbindable(expectedComponentName);
    assertThat(context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE)).isFalse();
    shadowMainLooper().idle();
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
    assertThat(Shadows.shadowOf(context).peekNextStartedService()).isNull();
  }

  @Test
  public void declaringComponentUnbindableMakesBindServiceReturnFalse_defaultComponent() {
    shadowMainLooper().pause();
    TestService service = new TestService();
    ComponentName expectedComponentName = new ComponentName("unbindable", "service");
    Binder expectedBinder = new Binder();
    Intent expectedIntent = new Intent("expected").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
    Shadows.shadowOf(context).declareComponentUnbindable(expectedComponentName);
    assertThat(context.bindService(expectedIntent, service, Context.BIND_AUTO_CREATE)).isFalse();
    shadowMainLooper().idle();
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
    assertThat(Shadows.shadowOf(context).peekNextStartedService()).isNull();
  }

  @Test
  public void bindServiceWithMultipleIntentsMapping() {
    TestService service = new TestService();
    ComponentName expectedComponentNameOne = new ComponentName("package", "one");
    Binder expectedBinderOne = new Binder();
    Intent expectedIntentOne = new Intent("expected_one").setPackage("package");
    ComponentName expectedComponentNameTwo = new ComponentName("package", "two");
    Binder expectedBinderTwo = new Binder();
    Intent expectedIntentTwo = new Intent("expected_two").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntentOne, expectedComponentNameOne, expectedBinderOne);
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntentTwo, expectedComponentNameTwo, expectedBinderTwo);
    context.bindService(expectedIntentOne, service, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(service.name).isEqualTo(expectedComponentNameOne);
    assertThat(service.service).isEqualTo(expectedBinderOne);
    context.bindService(expectedIntentTwo, service, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(service.name).isEqualTo(expectedComponentNameTwo);
    assertThat(service.service).isEqualTo(expectedBinderTwo);
  }

  @Test
  public void bindServiceWithMultipleIntentsMappingWithDefault() {
    TestService service = new TestService();
    ComponentName expectedComponentNameOne = new ComponentName("package", "one");
    Binder expectedBinderOne = new Binder();
    Intent expectedIntentOne = new Intent("expected_one").setPackage("package");
    ComponentName expectedComponentNameTwo = new ComponentName("package", "two");
    Binder expectedBinderTwo = new Binder();
    Intent expectedIntentTwo = new Intent("expected_two").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntentOne, expectedComponentNameOne, expectedBinderOne);
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntentTwo, expectedComponentNameTwo, expectedBinderTwo);
    context.bindService(expectedIntentOne, service, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(service.name).isEqualTo(expectedComponentNameOne);
    assertThat(service.service).isEqualTo(expectedBinderOne);
    context.bindService(expectedIntentTwo, service, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(service.name).isEqualTo(expectedComponentNameTwo);
    assertThat(service.service).isEqualTo(expectedBinderTwo);
    context.bindService(
        new Intent("unknown").setPackage("package"), service, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(service.name).isNull();
    assertThat(service.service).isNull();
  }

  @Test
  public void unbindServiceWithMultipleIntentsMapping() {
    TestService serviceOne = new TestService();
    ComponentName expectedComponentNameOne = new ComponentName("package", "one");
    Binder expectedBinderOne = new Binder();
    Intent expectedIntentOne = new Intent("expected_one").setPackage("package");
    TestService serviceTwo = new TestService();
    ComponentName expectedComponentNameTwo = new ComponentName("package", "two");
    Binder expectedBinderTwo = new Binder();
    Intent expectedIntentTwo = new Intent("expected_two").setPackage("package");
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntentOne, expectedComponentNameOne, expectedBinderOne);
    Shadows.shadowOf(context)
        .setComponentNameAndServiceForBindServiceForIntent(
            expectedIntentTwo, expectedComponentNameTwo, expectedBinderTwo);

    context.bindService(expectedIntentOne, serviceOne, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(serviceOne.nameDisconnected).isNull();
    context.unbindService(serviceOne);
    shadowMainLooper().idle();
    assertThat(serviceOne.name).isEqualTo(expectedComponentNameOne);

    context.bindService(expectedIntentTwo, serviceTwo, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(serviceTwo.nameDisconnected).isNull();
    context.unbindService(serviceTwo);
    shadowMainLooper().idle();
    assertThat(serviceTwo.name).isEqualTo(expectedComponentNameTwo);

    TestService serviceDefault = new TestService();
    context.bindService(
        new Intent("default").setPackage("package"), serviceDefault, Context.BIND_AUTO_CREATE);
    shadowMainLooper().idle();
    assertThat(serviceDefault.nameDisconnected).isNull();
    context.unbindService(serviceDefault);
    shadowMainLooper().idle();
    assertThat(serviceDefault.name).isNull();
  }

  @Test
  public void shouldHaveStoppedServiceIntentAndIndicateServiceWasntRunning() {

    Activity activity = Robolectric.setupActivity(Activity.class);

    Intent intent = getSomeActionIntent("some.action");

    boolean wasRunning = activity.stopService(intent);

    assertFalse(wasRunning);
    assertThat(Shadows.shadowOf(context).getNextStoppedService()).isEqualTo(intent);
  }

  private Intent getSomeActionIntent(String action) {
    Intent intent = new Intent();
    intent.setAction(action);
    intent.setPackage("package");
    return intent;
  }

  @Test
  public void shouldHaveStoppedServiceIntentAndIndicateServiceWasRunning() {

    Activity activity = Robolectric.setupActivity(Activity.class);

    Intent intent = getSomeActionIntent("some.action");

    activity.startService(intent);

    boolean wasRunning = activity.stopService(intent);

    assertTrue(wasRunning);
    assertThat(shadowOf(context).getNextStoppedService()).isEqualTo(intent);
  }

  @Test
  public void shouldHaveStoppedServiceByStartedComponent() {

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
    final Intent nextStoppedService = shadowOf(context).getNextStoppedService();
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
    assertFalse(shadowOf(context).hasReceiverForIntent(new Intent("Foo")));
    context.registerReceiver(expectedReceiver, new IntentFilter("Foo"));

    assertTrue(shadowOf(context).hasReceiverForIntent(new Intent("Foo")));
  }

  @Test
  public void canFindAllReceiversForAnIntent() throws Exception {
    BroadcastReceiver expectedReceiver = new TestBroadcastReceiver();
    assertFalse(shadowOf(context).hasReceiverForIntent(new Intent("Foo")));
    context.registerReceiver(expectedReceiver, new IntentFilter("Foo"));
    context.registerReceiver(expectedReceiver, new IntentFilter("Foo"));

    assertThat(shadowOf(context).getReceiversForIntent(new Intent("Foo"))).hasSize(2);
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
  public void clearRegisteredReceivers_clearsReceivers() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.registerReceiver(new TestBroadcastReceiver(), new IntentFilter("Foo"));

    assertThat(shadowOf(context).getRegisteredReceivers().size()).isAtLeast(1);

    shadowOf(context).clearRegisteredReceivers();

    assertThat(shadowOf(context).getRegisteredReceivers()).isEmpty();
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
    shadowMainLooper().idle();
    assertFalse(receiver.isSticky);
  }

  @Test
  public void shouldRememberResourcesAfterLazilyLoading() throws Exception {
    assertSame(context.getResources(), context.getResources());
  }

  @Test
  public void startActivity_whenActivityCheckingEnabled_doesntFindResolveInfo() throws Exception {
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
  public void startActivity_whenActivityCheckingEnabled_findsResolveInfo() throws Exception {
    shadowOf(context).checkActivities(true);

    context.startActivity(
        new Intent()
            .setClassName(context, TestActivity.class.getName())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    assertThat(shadowOf(context).getNextStartedActivity()).isNotNull();
  }

  @Test
  public void bindServiceShouldAddServiceConnectionToListOfBoundServiceConnections() {
    final ServiceConnection expectedServiceConnection = new EmptyServiceConnection();

    assertThat(Shadows.shadowOf(context).getBoundServiceConnections()).hasSize(0);
    assertThat(
            context.bindService(
                new Intent("connect").setPackage("dummy.package"), expectedServiceConnection, 0))
        .isTrue();
    assertThat(Shadows.shadowOf(context).getBoundServiceConnections()).hasSize(1);
    assertThat(Shadows.shadowOf(context).getBoundServiceConnections().get(0))
        .isSameInstanceAs(expectedServiceConnection);
  }

  @Test
  public void
      bindServiceShouldAddServiceConnectionToListOfBoundServiceConnectionsEvenIfServiceUnbindable() {
    final ServiceConnection expectedServiceConnection = new EmptyServiceConnection();
    final String unboundableAction = "refuse";
    final Intent serviceIntent = new Intent(unboundableAction).setPackage("dummy.package");
    Shadows.shadowOf(context).declareActionUnbindable(unboundableAction);
    assertThat(Shadows.shadowOf(context).getBoundServiceConnections()).hasSize(0);
    assertThat(context.bindService(serviceIntent, expectedServiceConnection, 0)).isFalse();
    assertThat(Shadows.shadowOf(context).getBoundServiceConnections()).hasSize(1);
    assertThat(Shadows.shadowOf(context).getBoundServiceConnections().get(0))
        .isSameInstanceAs(expectedServiceConnection);
  }

  @Test
  public void unbindServiceShouldRemoveServiceConnectionFromListOfBoundServiceConnections() {
    final ServiceConnection expectedServiceConnection = new EmptyServiceConnection();

    assertThat(
            context.bindService(
                new Intent("connect").setPackage("dummy.package"), expectedServiceConnection, 0))
        .isTrue();
    assertThat(Shadows.shadowOf(context).getBoundServiceConnections()).hasSize(1);
    assertThat(Shadows.shadowOf(context).getUnboundServiceConnections()).hasSize(0);
    context.unbindService(expectedServiceConnection);
    assertThat(Shadows.shadowOf(context).getBoundServiceConnections()).hasSize(0);
    assertThat(Shadows.shadowOf(context).getUnboundServiceConnections()).hasSize(1);
    assertThat(Shadows.shadowOf(context).getUnboundServiceConnections().get(0))
        .isSameInstanceAs(expectedServiceConnection);
  }

  @Test
  public void getForegroundThreadScheduler_shouldMatchRobolectricValue() {
    assertThat(Shadows.shadowOf(context).getForegroundThreadScheduler())
        .isSameInstanceAs(Robolectric.getForegroundThreadScheduler());
  }

  @Test
  public void getBackgroundThreadScheduler_shouldMatchRobolectricValue() {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.LEGACY);
    assertThat(Shadows.shadowOf(context).getBackgroundThreadScheduler())
        .isSameInstanceAs(Robolectric.getBackgroundThreadScheduler());
  }

  @Test
  public void getForegroundThreadScheduler_shouldMatchRuntimeEnvironment() {
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    assertThat(Shadows.shadowOf(context).getForegroundThreadScheduler()).isSameInstanceAs(s);
  }

  @Test
  public void getBackgroundThreadScheduler_shouldDifferFromRuntimeEnvironment_byDefault() {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.LEGACY);
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    assertThat(Shadows.shadowOf(context).getBackgroundThreadScheduler())
        .isNotSameInstanceAs(RuntimeEnvironment.getMasterScheduler());
  }

  @Test
  public void
      getBackgroundThreadScheduler_shouldDifferFromRuntimeEnvironment_withAdvancedScheduling() {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.LEGACY);
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    assertThat(Shadows.shadowOf(context).getBackgroundThreadScheduler()).isNotSameInstanceAs(s);
  }

  @Test
  public void getLatestPopupWindow() {
    PopupWindow pw = new PopupWindow(new LinearLayout(context));

    pw.showAtLocation(new LinearLayout(context), Gravity.CENTER, 0, 0);

    PopupWindow latestPopupWindow =
        Shadows.shadowOf(RuntimeEnvironment.application).getLatestPopupWindow();
    assertThat(latestPopupWindow).isSameInstanceAs(pw);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void shouldReturnNonDefaultProcessName() {
    ShadowApplication.setProcessName("org.foo:bar");
    assertThat(Application.getProcessName()).isEqualTo("org.foo:bar");
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

