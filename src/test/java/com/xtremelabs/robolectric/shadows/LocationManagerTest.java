package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus.Listener;
import android.location.LocationManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class LocationManagerTest {
    private LocationManager locationManager;

    @Before
    public void setUp() {
        locationManager = (LocationManager) Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
    }

    @Test
    public void shouldReturnProviderEnabledAsDefault() {
        Boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Assert.assertTrue(enabled);
    }

    @Test
    public void shouldDisableProvider() {
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);

        Boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Assert.assertFalse(enabled);
    }

    @Test
    public void shouldHaveListenerOnceAdded() {
        Listener listener = addListenerToLocationManager();

        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);

        Assert.assertTrue(shadowLocationManager.hasListener(listener));
    }

    @Test
    public void shouldNotHaveListenerOnceRemoved() {
        Listener listener = addListenerToLocationManager();

        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);

        locationManager.removeGpsStatusListener(listener);

        Assert.assertFalse(shadowLocationManager.hasListener(listener));
    }

    @Test
    public void shouldStoreBestProviderCriteriaAndEnabledOnlyFlag() throws Exception {
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        Criteria criteria = new Criteria();
        assertNull(locationManager.getBestProvider(criteria, true));
        assertSame(criteria, shadowLocationManager.getLastBestProviderCriteria());
        assertTrue(shadowLocationManager.getLastBestProviderEnabledOnly());
    }

    @Test
    public void shouldReturnBestProvider() throws Exception {
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        shadowLocationManager.setBestProvider("GNSS");
        assertEquals("GNSS", locationManager.getBestProvider(new Criteria(), false));
    }

    @Test
    public void shouldReturnNullWhenBestProviderIsNotSet() throws Exception {
        assertNull(locationManager.getBestProvider(new Criteria(), true));
    }

    private Listener addListenerToLocationManager() {
        Listener listener = new TestListener();
        locationManager.addGpsStatusListener(listener);
        return listener;
    }

    private class TestListener implements Listener {

        @Override
        public void onGpsStatusChanged(int event) {

        }

    }
}
