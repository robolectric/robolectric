package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.res.StringResourceLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
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
        checkSystemService(Context.SENSOR_SERVICE, android.hardware.SensorManager.class);
        checkSystemService(Context.STORAGE_SERVICE, android.os.storage.StorageManager.class);
        checkSystemService(Context.VIBRATOR_SERVICE, android.os.Vibrator.class);
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
        Application application = new ApplicationResolver(resourceFile("TestAndroidManifestWithPackageName.xml")).resolveApplication();
        assertEquals("com.wacka.wa", application.getPackageManager().getPackageInfo(null, 0).packageName);
    }
}
