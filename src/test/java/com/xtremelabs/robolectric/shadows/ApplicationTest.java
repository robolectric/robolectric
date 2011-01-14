package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
        Application app = Robolectric.application;

        Object service = app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assertTrue(service instanceof LayoutInflater);
        service = app.getSystemService(Context.ALARM_SERVICE);
        assertTrue(service instanceof AlarmManager);
        service = app.getSystemService(Context.LOCATION_SERVICE);
        assertTrue(service instanceof LocationManager);
        service = app.getSystemService(Context.WIFI_SERVICE);
        assertTrue(service instanceof WifiManager);
        service = app.getSystemService(Context.WINDOW_SERVICE);
        assertTrue(service instanceof WindowManager);
        service = app.getSystemService(Context.AUDIO_SERVICE);
        assertTrue(service instanceof AudioManager);
    }

    @Test
    public void packageManager_shouldKnowPackageName() throws Exception {
        Application application = new ApplicationResolver(resourceFile("TestAndroidManifestWithPackageName.xml")).resolveApplication();
        assertEquals("com.wacka.wa", application.getPackageManager().getPackageInfo(null, 0).packageName);
    }
}
