package org.robolectric.shadows;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Process;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowLocationManagerTest {
  private LocationManager locationManager;
  private ShadowLocationManager shadowLocationManager;
  private Application context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    shadowLocationManager = shadowOf(locationManager);
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void shouldReturnLocationDisabledByDefault() {
    assertFalse(locationManager.isLocationEnabled());
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void shouldReturnLocationEnabledOnceSet() {
    locationManager.setLocationEnabledForUser(true, Process.myUserHandle());
    assertTrue(locationManager.isLocationEnabled());
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
  public void getProviders_returnsProvidersBasedOnEnabledParameter() throws Exception {
    assertTrue(locationManager.getProviders(true).isEmpty());
    assertThat(locationManager.getProviders(false).size()).isEqualTo(3);

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);

    List<String> providers = locationManager.getProviders(true);
    assertTrue(providers.contains(NETWORK_PROVIDER));
    assertThat(providers.size()).isEqualTo(1);

    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    providers = locationManager.getProviders(true);
    assertTrue(providers.contains(NETWORK_PROVIDER));
    assertTrue(providers.contains(GPS_PROVIDER));
    assertThat(providers.size()).isEqualTo(2);

    shadowLocationManager.setProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);
    providers = locationManager.getProviders(true);
    assertTrue(providers.contains(NETWORK_PROVIDER));
    assertTrue(providers.contains(GPS_PROVIDER));
    assertTrue(providers.contains(LocationManager.PASSIVE_PROVIDER));
    assertThat(providers.size()).isEqualTo(3);
  }

  @Test
  public void shouldReturnAllProviders() throws Exception {
    assertThat(locationManager.getAllProviders().size()).isEqualTo(3);

    shadowLocationManager.setProviderEnabled("MY_PROVIDER", false);
    assertThat(locationManager.getAllProviders().size()).isEqualTo(4);
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

    Set<String> listOfExpectedProvidersForListener1 = new HashSet<>();
    listOfExpectedProvidersForListener1.add(LocationManager.NETWORK_PROVIDER);
    listOfExpectedProvidersForListener1.add(LocationManager.GPS_PROVIDER);

    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, listener2);
    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, listener2);

    Set<String> listOfExpectedProvidersForListener2 = new HashSet<>();
    listOfExpectedProvidersForListener2.add(LocationManager.NETWORK_PROVIDER);

    assertEquals(listOfExpectedProvidersForListener1, new HashSet<>(shadowLocationManager.getProvidersForListener(listener1)));
    assertEquals(listOfExpectedProvidersForListener2, new HashSet<>(shadowLocationManager.getProvidersForListener(listener2)));

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

    List<LocationListener> expected = new ArrayList<>();
    expected.add(otherListener);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEqualTo(expected);
  }

  @Test
  public void shouldRemovePendingIntentsWhenRequestingLocationUpdatesUsingCriteria() throws Exception {
    Intent someIntent = new Intent("some_action");
    PendingIntent someLocationListenerPendingIntent =
        PendingIntent.getBroadcast(context, 0, someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    Intent someOtherIntent = new Intent("some_other_action");
    PendingIntent someOtherLocationListenerPendingIntent =
        PendingIntent.getBroadcast(context, 0, someOtherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    shadowLocationManager.setBestProvider(LocationManager.GPS_PROVIDER, true);
    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_FINE);

    locationManager.requestLocationUpdates(0, 0, criteria, someLocationListenerPendingIntent);
    locationManager.requestLocationUpdates(0, 0, criteria, someOtherLocationListenerPendingIntent);

    locationManager.removeUpdates(someLocationListenerPendingIntent);

    Map<PendingIntent, Criteria> expectedCriteria = new HashMap<>();
    expectedCriteria.put(someOtherLocationListenerPendingIntent, criteria);
    assertThat(shadowLocationManager.getRequestLocationUdpateCriteriaPendingIntents()).isEqualTo(expectedCriteria);
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
    PendingIntent someLocationListenerPendingIntent =
        PendingIntent.getBroadcast(context, 0, someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    Intent someOtherIntent = new Intent("some_other_action");
    PendingIntent someOtherLocationListenerPendingIntent =
        PendingIntent.getBroadcast(context, 0, someOtherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    shadowLocationManager.setBestProvider(LocationManager.GPS_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);

    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, someLocationListenerPendingIntent);
    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, someOtherLocationListenerPendingIntent);

    locationManager.removeUpdates(someLocationListenerPendingIntent);

    Map<PendingIntent, String> expectedProviders = new HashMap<>();
    expectedProviders.put(someOtherLocationListenerPendingIntent, NETWORK_PROVIDER);
    assertThat(shadowLocationManager.getRequestLocationUdpateProviderPendingIntents()).isEqualTo(expectedProviders);
  }

  @Test
  public void shouldStoreBestProviderCriteriaAndEnabledOnlyFlag() throws Exception {
    Criteria criteria = new Criteria();
    assertNull(locationManager.getBestProvider(criteria, true));
    assertSame(criteria, shadowLocationManager.getLastBestProviderCriteria());
    assertTrue(shadowLocationManager.getLastBestProviderEnabledOnly());
  }

  @Test
  public void getBestProvider_returnsProviderBasedOnCriteriaAndEnabledState() throws Exception {
    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    assertThat(locationManager.getBestProvider(null, false)).isEqualTo(LocationManager.GPS_PROVIDER);
    assertThat(locationManager.getBestProvider(null, true)).isNull();
    assertThat(locationManager.getBestProvider(criteria, false)).isEqualTo(LocationManager.NETWORK_PROVIDER);
    assertThat(locationManager.getBestProvider(criteria, true)).isNull();
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
    PendingIntent someLocationListenerPendingIntent =
        PendingIntent.getBroadcast(context, 0, someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
    ArrayList<Criteria> criteriaList = new ArrayList<>();
    customProviderCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
    criteriaList.add(customProviderCriteria);
    shadowLocationManager.setProviderEnabled("BEST_ENABLED_PROVIDER_WITH_CRITERIA", true, criteriaList);
    assertTrue(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER_WITH_CRITERIA", true));
    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
    assertThat(locationManager.getBestProvider(criteria, true))
        .isEqualTo("BEST_ENABLED_PROVIDER_WITH_CRITERIA");
    assertThat(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER_WITH_CRITERIA", true))
        .isTrue();
    assertThat(locationManager.getBestProvider(criteria, false))
        .isEqualTo("BEST_ENABLED_PROVIDER_WITH_CRITERIA");
    assertThat(locationManager.getBestProvider(criteria, true))
        .isEqualTo("BEST_ENABLED_PROVIDER_WITH_CRITERIA");
  }

  @Test
  public void shouldReturnBestProviderUsingCriteria() {
    Criteria criteria = new Criteria();

    shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    assertThat(locationManager.getBestProvider(criteria, false))
        .isEqualTo(LocationManager.GPS_PROVIDER);

    shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false);
    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    assertThat(locationManager.getBestProvider(criteria, false))
        .isEqualTo(LocationManager.NETWORK_PROVIDER);

    criteria.setPowerRequirement(Criteria.POWER_LOW);
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    assertThat(locationManager.getBestProvider(criteria, false))
        .isEqualTo(LocationManager.NETWORK_PROVIDER);
  }

  @Test
  public void shouldReturnBestDisabledProvider() throws Exception {
    shadowLocationManager.setProviderEnabled("BEST_DISABLED_PROVIDER", false);
    shadowLocationManager.setBestProvider("BEST_DISABLED_PROVIDER", false);
    shadowLocationManager.setProviderEnabled("BEST_ENABLED_PROVIDER", true);
    shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", true);

    assertTrue(shadowLocationManager.setBestProvider("BEST_DISABLED_PROVIDER", false));
    assertThat(locationManager.getBestProvider(null, false)).isEqualTo("BEST_DISABLED_PROVIDER");
    assertThat(locationManager.getBestProvider(null, true)).isEqualTo("BEST_ENABLED_PROVIDER");
  }

  @Test
  public void getBestProvider_returnsBestProviderBasedOnEnabledState() throws Exception {
    shadowLocationManager.setProviderEnabled("BEST_ENABLED_PROVIDER", true);

    assertThat(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", true)).isTrue();
    assertThat(shadowLocationManager.setBestProvider("BEST_ENABLED_PROVIDER", false)).isFalse();
    assertThat(locationManager.getBestProvider(null, true)).isEqualTo("BEST_ENABLED_PROVIDER");
    assertThat(locationManager.getBestProvider(null, false)).isEqualTo(LocationManager.GPS_PROVIDER);
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
    PendingIntent someLocationListenerPendingIntent =
        PendingIntent.getBroadcast(context, 0, someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, someLocationListenerPendingIntent);

    assertThat(shadowLocationManager.getRequestLocationUdpateProviderPendingIntents().get(someLocationListenerPendingIntent)).isEqualTo(GPS_PROVIDER);
  }

  @Test
  public void shouldRegisterLocationUpdatesWhenCriteriaGiven() throws Exception {
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    shadowLocationManager.setBestProvider(LocationManager.NETWORK_PROVIDER, true);
    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_COARSE);

    Intent someIntent = new Intent("some_action");
    PendingIntent someLocationListenerPendingIntent =
        PendingIntent.getBroadcast(context, 0, someIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    Criteria someCriteria = new Criteria();
    someCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
    locationManager.requestLocationUpdates(0, 0, someCriteria, someLocationListenerPendingIntent);

    assertThat(shadowLocationManager.getRequestLocationUdpateCriteriaPendingIntents().get(someLocationListenerPendingIntent)).isEqualTo(someCriteria);
  }

  @Test
  public void simulateLocation_shouldNotNotifyListenerIfLessThanFastestInterval() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    shadowLocationManager.requestLocationUpdates(GPS_PROVIDER, 2000, 0, listener);
    long time = System.currentTimeMillis();

    Location location1 = new Location(GPS_PROVIDER);
    location1.setTime(time);

    Location location2 = new Location(GPS_PROVIDER);
    location2.setTime(time + 1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);
    assertThat(listener.location.getTime()).isEqualTo(location1.getTime());
  }

  @Test
  public void simulateLocation_shouldNotNotifyListenerIfLessThanMinimumDistance() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 200000, listener);

    Location location1 = new Location(GPS_PROVIDER);
    location1.setLatitude(1);
    location1.setLongitude(2);
    location1.setTime(0);

    Location location2 = new Location(GPS_PROVIDER);
    location2.setLatitude(1.5);
    location2.setLongitude(2.5);
    location2.setTime(1000);

    shadowLocationManager.simulateLocation(location1);
    shadowLocationManager.simulateLocation(location2);

    assertThat(listener.location.getLatitude()).isEqualTo(1.0d);
    assertThat(listener.location.getLongitude()).isEqualTo(2.0d);
  }

  @Test
  public void shouldNotThrowExceptionIfLocationListenerRemovedInsideOnLocationChanged() throws Exception {
    TestLocationListenerSelfRemoval listener = new TestLocationListenerSelfRemoval(locationManager);
    shadowLocationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener);

    Location location = new Location(GPS_PROVIDER);
    location.setLatitude(0);
    location.setLongitude(0);

    shadowLocationManager.simulateLocation(location);
    assertThat(shadowLocationManager.getRequestLocationUpdateListeners().size()).isEqualTo(0);
  }

  @Test
  public void requestLocationUpdates_shouldNotRegisterDuplicateListeners() throws Exception {
    TestLocationListener listener = new TestLocationListener();
    shadowLocationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener);
    shadowLocationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener);
    shadowLocationManager.simulateLocation(new Location(GPS_PROVIDER));
    assertThat(listener.updateCount).isEqualTo(1);
  }

  private Listener addGpsListenerToLocationManager() {
    Listener listener = new TestGpsListener();
    locationManager.addGpsStatusListener(listener);
    return listener;
  }

  private static class TestLocationListener implements LocationListener {
    boolean providerEnabled;
    Location location;
    int updateCount;

    @Override
    public void onLocationChanged(Location location) {
      this.location = location;
      updateCount++;
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

  private static class TestLocationListenerSelfRemoval implements LocationListener {
    LocationManager locationManager;

    public TestLocationListenerSelfRemoval(LocationManager locationManager) {
      this.locationManager = locationManager;
    }

    @Override
    public void onLocationChanged(Location location) {
      locationManager.removeUpdates(this);
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

  private static class TestGpsListener implements Listener {

    @Override
    public void onGpsStatusChanged(int event) {

    }
  }
}
