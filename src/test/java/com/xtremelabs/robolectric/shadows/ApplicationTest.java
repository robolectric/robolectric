package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.res.StringResourceLoader;
import com.xtremelabs.robolectric.util.TestBroadcastReceiver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileDescriptor;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.TestUtil.newConfig;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(WithTestDefaultsRunner.class)
public class ApplicationTest {
    @Before
    public void setUp() throws Exception {
        Robolectric.application = new Application();
    }

    @Test
    public void shouldBeAContext() throws Exception {
        assertThat(new Activity().getApplication(), sameInstance(Robolectric.application));
        assertThat(new Activity().getApplication().getApplicationContext(), sameInstance((Context) Robolectric.application));
    }

    @Test
    public void shouldBeBindableToAResourceLoader() throws Exception {
        ResourceLoader resourceLoader1 = new ResourceLoader(mock(StringResourceLoader.class)) {
        };
        when(resourceLoader1.getStringValue(R.id.title)).thenReturn("title from resourceLoader1");
        Application app1 = ShadowApplication.bind(new Application(), resourceLoader1);

        ResourceLoader resourceLoader2 = new ResourceLoader(mock(StringResourceLoader.class)) {
        };
        when(resourceLoader2.getStringValue(R.id.title)).thenReturn("title from resourceLoader2");
        Application app2 = ShadowApplication.bind(new Application(), resourceLoader2);

        assertEquals("title from resourceLoader1", new ContextWrapper(app1).getResources().getString(R.id.title));
        assertEquals("title from resourceLoader2", new ContextWrapper(app2).getResources().getString(R.id.title));
    }

    @Test
    public void shouldProvideServices() throws Exception {
        checkSystemService(Context.LAYOUT_INFLATER_SERVICE, android.view.LayoutInflater.class);
        checkSystemService(Context.ACTIVITY_SERVICE, android.app.ActivityManager.class);
        checkSystemService(Context.POWER_SERVICE, android.os.PowerManager.class);
        checkSystemService(Context.ALARM_SERVICE, android.app.AlarmManager.class);
        checkSystemService(Context.NOTIFICATION_SERVICE, android.app.NotificationManager.class);
        checkSystemService(Context.KEYGUARD_SERVICE, android.app.KeyguardManager.class);
        checkSystemService(Context.LOCATION_SERVICE, android.location.LocationManager.class);
        checkSystemService(Context.SEARCH_SERVICE, android.app.SearchManager.class);
        checkSystemService(Context.SENSOR_SERVICE, android.hardware.TestSensorManager.class);
        checkSystemService(Context.STORAGE_SERVICE, android.os.storage.StorageManager.class);
        checkSystemService(Context.VIBRATOR_SERVICE, android.os.TestVibrator.class);
        checkSystemService(Context.CONNECTIVITY_SERVICE, android.net.ConnectivityManager.class);
        checkSystemService(Context.WIFI_SERVICE, android.net.wifi.WifiManager.class);
        checkSystemService(Context.AUDIO_SERVICE, android.media.AudioManager.class);
        checkSystemService(Context.TELEPHONY_SERVICE, android.telephony.TelephonyManager.class);
        checkSystemService(Context.INPUT_METHOD_SERVICE, android.view.inputmethod.InputMethodManager.class);
        checkSystemService(Context.UI_MODE_SERVICE, android.app.UiModeManager.class);
        checkSystemService(Context.DOWNLOAD_SERVICE, android.app.DownloadManager.class);
    }

    private void checkSystemService(String name, Class expectedClass) {
        Object systemService = Robolectric.application.getSystemService(name);
        assertThat(systemService, instanceOf(expectedClass));
        assertThat(systemService, sameInstance(Robolectric.application.getSystemService(name)));
    }

    @Test
    public void packageManager_shouldKnowPackageName() throws Exception {
        Application application = new ApplicationResolver(newConfig("TestAndroidManifestWithPackageName.xml")).resolveApplication();
        assertEquals("com.wacka.wa", application.getPackageManager().getPackageInfo("com.wacka.wa", 0).packageName);
    }

    @Test
    public void packageManager_shouldKnowApplicationName() throws Exception {
        Application application = new ApplicationResolver(newConfig("TestAndroidManifestWithAppName.xml")).resolveApplication();
        assertEquals("com.xtremelabs.robolectric.TestApplication",
                application.getPackageManager().getApplicationInfo("com.xtremelabs.robolectric", 0).name);
    }

    @Test
    public void bindServiceShouldCallOnServiceConnectedWhenNotPaused() {
        Robolectric.pauseMainLooper();
        ComponentName expectedComponentName = new ComponentName("", "");
        NullBinder expectedBinder = new NullBinder();
        Robolectric.shadowOf(Robolectric.application).setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);

        TestService service = new TestService();
        assertTrue(Robolectric.application.bindService(new Intent(""), service, Context.BIND_AUTO_CREATE));

        assertNull(service.name);
        assertNull(service.service);

        Robolectric.unPauseMainLooper();

        assertEquals(expectedComponentName, service.name);
        assertEquals(expectedBinder, service.service);
    }

    @Test
    public void unbindServiceShouldCallOnServiceDisconnectedWhenNotPaused() {
        TestService service = new TestService();
        ComponentName expectedComponentName = new ComponentName("", "");
        NullBinder expectedBinder = new NullBinder();
        Robolectric.shadowOf(Robolectric.application).setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
        Robolectric.application.bindService(new Intent(""), service, Context.BIND_AUTO_CREATE);
        Robolectric.pauseMainLooper();

        Robolectric.application.unbindService(service);
        assertNull(service.nameUnbound);
        Robolectric.unPauseMainLooper();
        assertEquals(expectedComponentName, service.nameUnbound);
    }

    @Test
    public void unbindServiceAddsEntryToUnboundServicesCollection() {
        TestService service = new TestService();
        ComponentName expectedComponentName = new ComponentName("", "");
        NullBinder expectedBinder = new NullBinder();
        final ShadowApplication shadowApplication = Robolectric.shadowOf(Robolectric.application);
        shadowApplication.setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
        Robolectric.application.bindService(new Intent(""), service, Context.BIND_AUTO_CREATE);
        Robolectric.application.unbindService(service);
        assertEquals(1, shadowApplication.getUnboundServiceConnections().size());
        assertEquals(service, shadowApplication.getUnboundServiceConnections().get(0));
    }

    @Test
    public void declaringServiceUnbindableMakesBindServiceReturnFalse() {
        Robolectric.pauseMainLooper();
        TestService service = new TestService();
        ComponentName expectedComponentName = new ComponentName("", "");
        NullBinder expectedBinder = new NullBinder();
        final ShadowApplication shadowApplication = Robolectric.shadowOf(Robolectric.application);
        shadowApplication.setComponentNameAndServiceForBindService(expectedComponentName, expectedBinder);
        shadowApplication.declareActionUnbindable("refuseToBind");
        assertFalse(Robolectric.application.bindService(new Intent("refuseToBind"), service, Context.BIND_AUTO_CREATE));
        Robolectric.unPauseMainLooper();
        assertNull(service.name);
        assertNull(service.service);
        assertNull(shadowApplication.peekNextStartedService());
    }

    @Test
    public void shouldHaveStoppedServiceIntentAndIndicateServiceWasntRunning() {
        ShadowApplication shadowApplication = Robolectric.shadowOf(Robolectric.application);

        Activity activity = new Activity();

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
        ShadowApplication shadowApplication = shadowOf(Robolectric.application);

        Activity activity = new Activity();

        Intent intent = getSomeActionIntent("some.action");

        activity.startService(intent);

        boolean wasRunning = activity.stopService(intent);

        assertTrue(wasRunning);
        assertEquals(intent, shadowApplication.getNextStoppedService());
    }

    @Test
    public void shouldClearStartedServiceIntents() {
        ShadowApplication shadowApplication = shadowOf(Robolectric.application);
        shadowApplication.startService(getSomeActionIntent("some.action"));
        shadowApplication.startService(getSomeActionIntent("another.action"));

        shadowApplication.clearStartedServices();

        assertNull(shadowApplication.getNextStartedService());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfContainsRegisteredReceiverOfAction() {
        Activity activity = new Activity();
        activity.registerReceiver(new TestBroadcastReceiver(), new IntentFilter("Foo"));

        shadowOf(Robolectric.application).assertNoBroadcastListenersOfActionRegistered(activity, "Foo");
    }

    @Test
    public void shouldNotThrowIfDoesNotContainsRegisteredReceiverOfAction() {
        Activity activity = new Activity();
        activity.registerReceiver(new TestBroadcastReceiver(), new IntentFilter("Foo"));

        shadowOf(Robolectric.application).assertNoBroadcastListenersOfActionRegistered(activity, "Bar");
    }

    @Test
    public void canAnswerIfReceiverIsRegisteredForIntent() throws Exception {
        BroadcastReceiver expectedReceiver = new TestBroadcastReceiver();
        ShadowApplication shadowApplication = shadowOf(Robolectric.application);
        assertFalse(shadowApplication.hasReceiverForIntent(new Intent("Foo")));
        Robolectric.application.registerReceiver(expectedReceiver, new IntentFilter("Foo"));

        assertTrue(shadowApplication.hasReceiverForIntent(new Intent("Foo")));
    }

    @Test
    public void canFindAllReceiversForAnIntent() throws Exception {
        BroadcastReceiver expectedReceiver = new TestBroadcastReceiver();
        ShadowApplication shadowApplication = shadowOf(Robolectric.application);
        assertFalse(shadowApplication.hasReceiverForIntent(new Intent("Foo")));
        Robolectric.application.registerReceiver(expectedReceiver, new IntentFilter("Foo"));
        Robolectric.application.registerReceiver(expectedReceiver, new IntentFilter("Foo"));

        assertTrue(shadowApplication.getReceiversForIntent(new Intent("Foo")).size() == 2);
    }

    @Test
    public void broadcasts_shouldBeLogged() {
        Intent broadcastIntent = new Intent("foo");
        Robolectric.application.sendBroadcast(broadcastIntent);

        List<Intent> broadcastIntents = shadowOf(Robolectric.application).getBroadcastIntents();
        assertTrue(broadcastIntents.size() == 1);
        assertEquals(broadcastIntent, broadcastIntents.get(0));
    }

    private static class NullBinder implements IBinder {
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return null;
        }

        @Override
        public boolean pingBinder() {
            return false;
        }

        @Override
        public boolean isBinderAlive() {
            return false;
        }

        @Override
        public IInterface queryLocalInterface(String descriptor) {
            return null;
        }

        @Override
        public void dump(FileDescriptor fd, String[] args) throws RemoteException {
        }

        @Override
        public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return false;
        }

        @Override
        public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
        }

        @Override
        public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
            return false;
        }

        @Override
        public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
        }
    }
}
