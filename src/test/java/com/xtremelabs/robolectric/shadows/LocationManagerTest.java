package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class LocationManagerTest {
    private LocationManager locationManager;
    private ShadowLocationManager shadowLocationManager;

    @Before
    public void setUp() {
        locationManager = (LocationManager) Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
    }

    @Test
    public void shouldReturnProviderEnabledAsDefault() {
        Boolean enabled = locationManager.isProviderEnabled(GPS_PROVIDER);
        Assert.assertTrue(enabled);
    }

    @Test
    public void shouldDisableProvider() {
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);

        Boolean enabled = locationManager.isProviderEnabled(GPS_PROVIDER);
        assertFalse(enabled);
    }

    @Test
    public void shouldHaveListenerOnceAdded() {
        Listener listener = addGpsListenerToLocationManager();

        assertTrue(shadowLocationManager.hasGpsStatusListener(listener));
    }

    @Test
    public void shouldNotHaveListenerOnceRemoved() {
        Listener listener = addGpsListenerToLocationManager();

        locationManager.removeGpsStatusListener(listener);

        assertFalse(shadowLocationManager.hasGpsStatusListener(listener));
    }

    @Test
    public void shouldReturnEnabledProviders() throws Exception {
        shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, false);
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);
        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, false);

        assertTrue(locationManager.getProviders(true).isEmpty());

        shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);

        List<String> providers = locationManager.getProviders(true);
        assertTrue(providers.contains(NETWORK_PROVIDER));
        assertThat(providers.size(), equalTo(1));

        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
        providers = locationManager.getProviders(true);
        assertTrue(providers.contains(NETWORK_PROVIDER));
        assertTrue(providers.contains(GPS_PROVIDER));
        assertThat(providers.size(), equalTo(2));

        shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);
        providers = locationManager.getProviders(true);
        assertTrue(providers.contains(NETWORK_PROVIDER));
        assertTrue(providers.contains(GPS_PROVIDER));
        assertTrue(providers.contains(LocationManager.PASSIVE_PROVIDER));
        assertThat(providers.size(), equalTo(3));
    }

    @Test
    public void shouldReturnAllProviders() throws Exception {
        assertThat(locationManager.getAllProviders().size(), equalTo(3));

        shadowLocationManager.setProviderEnabled("MY_PROVIDER", false);
        assertThat(locationManager.getAllProviders().size(), equalTo(4));
    }

    @Test
    public void shouldReturnLastKnownLocationForAProvider() throws Exception {
        assertNull(locationManager.getLastKnownLocation(NETWORK_PROVIDER));

        Location networkLocation = new Location(NETWORK_PROVIDER);
        Location gpsLocation = new Location(GPS_PROVIDER);

        shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, networkLocation);
        shadowLocationManager.setLastKnownLocation(GPS_PROVIDER, gpsLocation);

        assertSame(locationManager.getLastKnownLocation(NETWORK_PROVIDER), networkLocation);
        assertSame(locationManager.getLastKnownLocation(GPS_PROVIDER), gpsLocation);
    }

    @Test
    public void shouldStoreRequestLocationUpdateListeners() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 2.0f, listener);
        assertSame(shadowLocationManager.getRequestLocationUpdateListeners().get(0), listener);
    }

    @Test
    public void shouldRemoveLocationListeners() throws Exception {
        TestLocationListener listener = new TestLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 2.0f, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 2.0f, listener);

        TestLocationListener otherListener = new TestLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 2.0f, otherListener);

        locationManager.removeUpdates(listener);

        List<LocationListener> expected = new ArrayList<LocationListener>();
        expected.add(otherListener);
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners(), equalTo(expected));
    }

    @Test
    public void shouldStoreBestProviderCriteriaAndEnabledOnlyFlag() throws Exception {
        Criteria criteria = new Criteria();
        assertNull(locationManager.getBestProvider(criteria, true));
        assertSame(criteria, shadowLocationManager.getLastBestProviderCriteria());
        assertTrue(shadowLocationManager.getLastBestProviderEnabledOnly());
    }

    @Test
    public void shouldReturnBestProvider() throws Exception {
        shadowLocationManager.setBestProvider("GNSS");
        assertEquals("GNSS", locationManager.getBestProvider(new Criteria(), false));
    }

    @Test
    public void shouldReturnNullWhenBestProviderIsNotSet() throws Exception {
        assertNull(locationManager.getBestProvider(new Criteria(), true));
    }

    private Listener addGpsListenerToLocationManager() {
        Listener listener = new TestGpsListener();
        locationManager.addGpsStatusListener(listener);
        return listener;
    }

    private static class TestLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }

    private class TestGpsListener implements Listener {

        @Override
        public void onGpsStatusChanged(int event) {

        }
    }
}
