package com.xtremelabs.robolectric.shadows;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

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
    public void shouldReturnNoProviderEnabledByDefault() {
        Boolean enabled = locationManager.isProviderEnabled(GPS_PROVIDER);
        assertFalse(enabled);
        enabled = locationManager.isProviderEnabled(NETWORK_PROVIDER);
        assertFalse(enabled);
        enabled = locationManager.isProviderEnabled("RANDOM_PROVIDER");
        assertFalse(enabled);
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
        assertThat(locationManager.getProviders(false).size(), equalTo(3));

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
    public void shouldRemovePendingIntentsWhenRequestingLocationUpdatesUsingCriteria() {
        Intent someIntent = new Intent("some_action");
        PendingIntent someLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric
                .getShadowApplication().getApplicationContext(), 0, someIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent someOtherIntent = new Intent("some_other_action");
        PendingIntent someOtherLocationListenerPendingIntent = PendingIntent.getBroadcast(
                Robolectric.getShadowApplication().getApplicationContext(), 0, someOtherIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Criteria criteria = new Criteria();
        locationManager.requestLocationUpdates(0, 0, criteria, someLocationListenerPendingIntent);
        locationManager.requestLocationUpdates(0, 0, criteria, someOtherLocationListenerPendingIntent);

        locationManager.removeUpdates(someLocationListenerPendingIntent);

        Map<PendingIntent, Criteria> expectedCriteria = new HashMap<PendingIntent, Criteria>();
        expectedCriteria.put(someOtherLocationListenerPendingIntent, criteria);
        assertThat(shadowLocationManager.getRequestLocationUdpateCriteriaPendingIntents(), equalTo(expectedCriteria));
    }

    @Test
    public void shouldRemovePendingIntentsWhenRequestingLocationUpdatesUsingLocationListeners() {
        Intent someIntent = new Intent("some_action");
        PendingIntent someLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric.getShadowApplication().getApplicationContext(), 0,
                someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent someOtherIntent = new Intent("some_other_action");
        PendingIntent someOtherLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric.getShadowApplication().getApplicationContext(),
                0, someOtherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, someLocationListenerPendingIntent);
        locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, someOtherLocationListenerPendingIntent);

        locationManager.removeUpdates(someLocationListenerPendingIntent);

        Map<PendingIntent, String> expectedProviders = new HashMap<PendingIntent, String>();
        expectedProviders.put(someOtherLocationListenerPendingIntent, NETWORK_PROVIDER);
        assertThat(shadowLocationManager.getRequestLocationUdpateProviderPendingIntents(),
                equalTo(expectedProviders));
    }

    @Test
    public void shouldStoreBestProviderCriteriaAndEnabledOnlyFlag() throws Exception {
        Criteria criteria = new Criteria();
        assertNull(locationManager.getBestProvider(criteria, true));
        assertSame(criteria, shadowLocationManager.getLastBestProviderCriteria());
        assertTrue(shadowLocationManager.getLastBestProviderEnabledOnly());
    }

    // Refactor this monster
    @Test
    public void shouldReturnBestProvider() throws Exception {
        Criteria criteria = new Criteria();
        assertNull(locationManager.getBestProvider(null, false));
        assertNull(locationManager.getBestProvider(null, true));
        assertNull(locationManager.getBestProvider(criteria, false));
        assertNull(locationManager.getBestProvider(criteria, true));

        try {
            shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", true);
            Assert.fail("The best provider is unknown!");
        } catch (Exception e) {
            // No worries, everything is fine...
        }

        shadowLocationManager.setProviderEnabled("BEST_ENABLED_PROVIDER", true);
        assertFalse(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", false));
        assertTrue(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", true));
        assertThat("BEST_ENABLED_PROVIDER", equalTo(locationManager.getBestProvider(null, true)));
        assertNull(locationManager.getBestProvider(null, false));

        shadowLocationManager.setProviderEnabled("BEST_DISABLED_PROVIDER", false);
        assertTrue(shadowLocationManager.setBestProvider("BEST_DISABLED_PROVIDER", false));
        assertThat("BEST_DISABLED_PROVIDER", equalTo(locationManager.getBestProvider(null, false)));
        assertThat("BEST_ENABLED_PROVIDER", equalTo(locationManager.getBestProvider(null, true)));

        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        assertThat(LocationManager.GPS_PROVIDER, equalTo(locationManager.getBestProvider(criteria, false)));

        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        assertThat(LocationManager.NETWORK_PROVIDER, equalTo(locationManager.getBestProvider(criteria, false)));

        //TODO true?!?!
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        assertThat(LocationManager.NETWORK_PROVIDER, equalTo(locationManager.getBestProvider(criteria, false)));

        assertThat("BEST_ENABLED_PROVIDER", equalTo(locationManager.getBestProvider(null, true)));

        // Manually set best provider should be returned
        Criteria providerCriteria = new Criteria();
        ArrayList<Criteria> criteriaList = new ArrayList<Criteria>();
        providerCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteriaList.add(providerCriteria);
        shadowLocationManager.setProviderEnabled("BEST_ENABLED_PROVIDER_WITH_CRITERIA", true, criteriaList);
        assertTrue(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER_WITH_CRITERIA", true));
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        assertThat("BEST_ENABLED_PROVIDER_WITH_CRITERIA", equalTo(locationManager.getBestProvider(criteria, true)));
        assertTrue(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER_WITH_CRITERIA", true));
        assertThat("BEST_ENABLED_PROVIDER_WITH_CRITERIA", equalTo(locationManager.getBestProvider(criteria, false)));
        assertThat("BEST_ENABLED_PROVIDER_WITH_CRITERIA", equalTo(locationManager.getBestProvider(criteria, true)));
    }

    @Test
    public void shouldNotifyAllListenersIfProviderStateChanges() {
        TestLocationListener listener = new TestLocationListener();
        locationManager.requestLocationUpdates("TEST_PROVIDER", 0, 0, listener);
        shadowLocationManager.setProviderEnabled("TEST_PROVIDER", true);
        assertTrue(listener.providerEnabled);
        shadowLocationManager.setProviderEnabled("TEST_PROVIDER", false);
        assertFalse(listener.providerEnabled);
    }

    @Test
    public void shouldRegisterLocationUpdatesWhenProviderGiven() {
        Intent someIntent = new Intent("some_action");
        PendingIntent someLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric.getShadowApplication().getApplicationContext(), 0,
                someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, someLocationListenerPendingIntent);

        assertThat(shadowLocationManager.getRequestLocationUdpateProviderPendingIntents().get(someLocationListenerPendingIntent),
                equalTo(GPS_PROVIDER));
    }

    @Test
    public void shouldRegisterLocationUpdatesWhenCriteriaGiven() {
        Intent someIntent = new Intent("some_action");
        PendingIntent someLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric.getShadowApplication().getApplicationContext(), 0,
                someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Criteria someCriteria = new Criteria();
        someCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        locationManager.requestLocationUpdates(0, 0, someCriteria, someLocationListenerPendingIntent);

        assertThat(shadowLocationManager.getRequestLocationUdpateCriteriaPendingIntents().get(someLocationListenerPendingIntent),
                equalTo(someCriteria));
    }

    private Listener addGpsListenerToLocationManager() {
        Listener listener = new TestGpsListener();
        locationManager.addGpsStatusListener(listener);
        return listener;
    }

    private static class TestLocationListener implements LocationListener {
        public boolean providerEnabled;

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
            providerEnabled = true;
        }

        @Override
        public void onProviderDisabled(String s) {
            providerEnabled = false;
        }
    }

    private class TestGpsListener implements Listener {

        @Override
        public void onGpsStatusChanged(int event) {

        }
    }
}
