package com.xtremelabs.robolectric.shadows;

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
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.*;
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
        // No provider is enabled by default, so it must be manually enabled
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);
        assertFalse(locationManager.isProviderEnabled(GPS_PROVIDER));
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
    public void shouldKeepTrackOfWhichProvidersAListenerIsBoundTo_withoutDuplicates_inAnyOrder() throws Exception {
        TestLocationListener listener1 = new TestLocationListener();
        TestLocationListener listener2 = new TestLocationListener();

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, listener1);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, listener1);

        Set<String> listOfExpectedProvidersForListener1 = new HashSet<String>();
        listOfExpectedProvidersForListener1.add(LocationManager.NETWORK_PROVIDER);
        listOfExpectedProvidersForListener1.add(LocationManager.GPS_PROVIDER);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, listener2);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, listener2);

        Set<String> listOfExpectedProvidersForListener2 = new HashSet<String>();
        listOfExpectedProvidersForListener2.add(LocationManager.NETWORK_PROVIDER);

        assertEquals(listOfExpectedProvidersForListener1, new HashSet<String>(shadowLocationManager.getProvidersForListener(listener1)));
        assertEquals(listOfExpectedProvidersForListener2, new HashSet<String>(shadowLocationManager.getProvidersForListener(listener2)));

        locationManager.removeUpdates(listener1);
        assertEquals(0, shadowLocationManager.getProvidersForListener(listener1).size());
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
    public void shouldRemovePendingIntentsWhenRequestingLocationUpdatesUsingCriteria() throws Exception {
        Intent someIntent = new Intent("some_action");
        PendingIntent someLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric
                .getShadowApplication().getApplicationContext(), 0, someIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent someOtherIntent = new Intent("some_other_action");
        PendingIntent someOtherLocationListenerPendingIntent = PendingIntent.getBroadcast(
                Robolectric.getShadowApplication().getApplicationContext(), 0, someOtherIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
        shadowLocationManager.setBestProvider(LocationManager.GPS_PROVIDER, true);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        locationManager.requestLocationUpdates(0, 0, criteria, someLocationListenerPendingIntent);
        locationManager.requestLocationUpdates(0, 0, criteria, someOtherLocationListenerPendingIntent);

        locationManager.removeUpdates(someLocationListenerPendingIntent);

        Map<PendingIntent, Criteria> expectedCriteria = new HashMap<PendingIntent, Criteria>();
        expectedCriteria.put(someOtherLocationListenerPendingIntent, criteria);
        assertThat(shadowLocationManager.getRequestLocationUdpateCriteriaPendingIntents(), equalTo(expectedCriteria));
    }

    @Test
    public void shouldNotSetBestEnabledProviderIfProviderIsDisabled() throws Exception {
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
        assertTrue(shadowLocationManager.setBestProvider(LocationManager.GPS_PROVIDER, true));
    }

    @Test
    public void shouldNotSetBestDisabledProviderIfProviderIsEnabled() throws Exception {
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
        assertFalse(shadowLocationManager.setBestProvider(LocationManager.GPS_PROVIDER, false));
    }

    @Test
    public void shouldRemovePendingIntentsWhenRequestingLocationUpdatesUsingLocationListeners() throws Exception {
        Intent someIntent = new Intent("some_action");
        PendingIntent someLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric.getShadowApplication().getApplicationContext(), 0,
                someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent someOtherIntent = new Intent("some_other_action");
        PendingIntent someOtherLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric.getShadowApplication().getApplicationContext(),
                0, someOtherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
        shadowLocationManager.setBestProvider(LocationManager.GPS_PROVIDER, true);
        shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);

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

    @Test
    public void shouldReturnNullIfBestProviderNotExplicitlySet() throws Exception {
        Criteria criteria = new Criteria();
        assertNull(locationManager.getBestProvider(null, false));
        assertNull(locationManager.getBestProvider(null, true));
        assertNull(locationManager.getBestProvider(criteria, false));
        assertNull(locationManager.getBestProvider(criteria, true));
    }

    @Test
    public void shouldThrowExceptionWhenRequestingLocationUpdatesWithANullIntent() throws Exception {
        try {
            shadowLocationManager.requestLocationUpdates(0, 0, new Criteria(), null);
            Assert.fail("When requesting location updates the intent must not be null!");
        } catch (Exception e) {
            // No worries, everything is fine...
        }
    }

    @Test
    public void shouldThrowExceptionWhenRequestingLocationUpdatesAndNoProviderIsFound() throws Exception {
        Intent someIntent = new Intent("some_action");
        PendingIntent someLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric.getShadowApplication().getApplicationContext(), 0,
                someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        try {
            shadowLocationManager.requestLocationUpdates(0, 0, criteria, someLocationListenerPendingIntent);
            Assert.fail("When requesting location updates the intent must not be null!");
        } catch (Exception e) {
            // No worries, everything is fine...
        }
    }

    @Test
    public void shouldThrowExceptionIfTheBestProviderIsUnknown() throws Exception {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        try {
            shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", true);
            Assert.fail("The best provider is unknown!");
        } catch (Exception e) {
            // No worries, everything is fine...
        }
    }

    @Test
    public void shouldReturnBestCustomProviderUsingCriteria() throws Exception {
        Criteria criteria = new Criteria();
        Criteria customProviderCriteria = new Criteria();

        // Manually set best provider should be returned
        ArrayList<Criteria> criteriaList = new ArrayList<Criteria>();
        customProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteriaList.add(customProviderCriteria);
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
    public void shouldReturnBestProviderUsingCriteria() {
        Criteria criteria = new Criteria();

        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        assertThat(LocationManager.GPS_PROVIDER, equalTo(locationManager.getBestProvider(criteria, false)));

        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        assertThat(LocationManager.NETWORK_PROVIDER, equalTo(locationManager.getBestProvider(criteria, false)));

        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        assertThat(LocationManager.NETWORK_PROVIDER, equalTo(locationManager.getBestProvider(criteria, false)));
    }

    @Test
    public void shouldReturnBestDisabledProvider() throws Exception {
        shadowLocationManager.setProviderEnabled("BEST_DISABLED_PROVIDER", false);
        shadowLocationManager.setBestProvider("BEST_DISABLED_PROVIDER", false);
        shadowLocationManager.setProviderEnabled("BEST_ENABLED_PROVIDER", true);
        shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", true);

        assertTrue(shadowLocationManager.setBestProvider("BEST_DISABLED_PROVIDER", false));
        assertThat("BEST_DISABLED_PROVIDER", equalTo(locationManager.getBestProvider(null, false)));
        assertThat("BEST_ENABLED_PROVIDER", equalTo(locationManager.getBestProvider(null, true)));
    }

    @Test
    public void shouldReturnBestEnabledProvider() throws Exception {
        shadowLocationManager.setProviderEnabled("BEST_ENABLED_PROVIDER", true);

        assertTrue(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", true));
        assertFalse(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", false));
        assertThat("BEST_ENABLED_PROVIDER", equalTo(locationManager.getBestProvider(null, true)));
        assertNull(locationManager.getBestProvider(null, false));
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
    public void shouldRegisterLocationUpdatesWhenProviderGiven() throws Exception {
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
        shadowLocationManager.setBestProvider(LocationManager.GPS_PROVIDER, true);

        Intent someIntent = new Intent("some_action");
        PendingIntent someLocationListenerPendingIntent = PendingIntent.getBroadcast(Robolectric.getShadowApplication().getApplicationContext(), 0,
                someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, someLocationListenerPendingIntent);

        assertThat(shadowLocationManager.getRequestLocationUdpateProviderPendingIntents().get(someLocationListenerPendingIntent),
                equalTo(GPS_PROVIDER));
    }

    @Test
    public void shouldRegisterLocationUpdatesWhenCriteriaGiven() throws Exception {
        shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
        shadowLocationManager.setBestProvider(LocationManager.NETWORK_PROVIDER, true);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

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
