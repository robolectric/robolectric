package org.robolectric.shadows;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.MODE_CHANGED_ACTION;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static android.location.LocationManager.PROVIDERS_CHANGED_ACTION;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.R;
import static android.provider.Settings.Secure.LOCATION_MODE;
import static android.provider.Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
import static android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
import static android.provider.Settings.Secure.LOCATION_MODE_OFF;
import static android.provider.Settings.Secure.LOCATION_MODE_SENSORS_ONLY;
import static android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED;
import static androidx.test.ext.truth.location.LocationCorrespondences.equality;
import static androidx.test.ext.truth.location.LocationSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.GnssAntennaInfo;
import android.location.GnssAntennaInfo.Listener;
import android.location.GnssAntennaInfo.PhaseCenterOffset;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.LocationRequest;
import android.location.OnNmeaMessageListener;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.shadows.ShadowLocationManager.ProviderProperties;

/** Tests for {@link ShadowLocationManager}. */
@SuppressWarnings("deprecation")
@RunWith(AndroidJUnit4.class)
@LooperMode(Mode.PAUSED)
public class ShadowLocationManagerTest {

  private static final String MY_PROVIDER = "myProvider";

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
  @Config(maxSdk = VERSION_CODES.O)
  public void testInitializationState_PreP() {
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_SENSORS_ONLY);
    assertThat(getProvidersAllowed()).containsExactly(GPS_PROVIDER);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void testInitializationState_PPlus() {
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(locationManager.isLocationEnabled()).isTrue();
    assertThat(getLocationMode()).isNotEqualTo(LOCATION_MODE_OFF);
    assertThat(getProvidersAllowed()).containsExactly(GPS_PROVIDER);
  }

  @Test
  public void testGetAllProviders() {
    assertThat(locationManager.getAllProviders())
        .containsExactly(GPS_PROVIDER, NETWORK_PROVIDER, PASSIVE_PROVIDER);
    shadowLocationManager.setProviderProperties(MY_PROVIDER, null);
    assertThat(locationManager.getAllProviders())
        .containsExactly(MY_PROVIDER, GPS_PROVIDER, NETWORK_PROVIDER, PASSIVE_PROVIDER);
  }

  @Test
  @Config(minSdk = VERSION_CODES.KITKAT)
  public void testGetProvider() {
    LocationProvider p;

    p = locationManager.getProvider(GPS_PROVIDER);
    assertThat(p).isNotNull();
    assertThat(p.getName()).isEqualTo(GPS_PROVIDER);

    p = locationManager.getProvider(NETWORK_PROVIDER);
    assertThat(p).isNotNull();
    assertThat(p.getName()).isEqualTo(NETWORK_PROVIDER);

    p = locationManager.getProvider(PASSIVE_PROVIDER);
    assertThat(p).isNotNull();
    assertThat(p.getName()).isEqualTo(PASSIVE_PROVIDER);

    shadowLocationManager.setProviderProperties(
        MY_PROVIDER,
        new ProviderProperties(
            true,
            false,
            true,
            false,
            true,
            false,
            true,
            Criteria.POWER_HIGH,
            Criteria.ACCURACY_COARSE));

    p = locationManager.getProvider(MY_PROVIDER);
    assertThat(p).isNotNull();
    assertThat(p.getName()).isEqualTo(MY_PROVIDER);
    assertThat(p.requiresNetwork()).isTrue();
    assertThat(p.requiresSatellite()).isFalse();
    assertThat(p.requiresCell()).isTrue();
    assertThat(p.hasMonetaryCost()).isFalse();
    assertThat(p.supportsAltitude()).isTrue();
    assertThat(p.supportsSpeed()).isFalse();
    assertThat(p.supportsBearing()).isTrue();
    assertThat(p.getPowerRequirement()).isEqualTo(Criteria.POWER_HIGH);
    assertThat(p.getAccuracy()).isEqualTo(Criteria.ACCURACY_COARSE);

    p = locationManager.getProvider("noProvider");
    assertThat(p).isNull();
  }

  @Test
  public void testGetProviders_Enabled() {
    assertThat(locationManager.getProviders(false))
        .containsExactly(GPS_PROVIDER, NETWORK_PROVIDER, PASSIVE_PROVIDER);
    assertThat(locationManager.getProviders(true)).containsExactly(GPS_PROVIDER, PASSIVE_PROVIDER);

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);

    assertThat(locationManager.getProviders(false))
        .containsExactly(MY_PROVIDER, GPS_PROVIDER, NETWORK_PROVIDER, PASSIVE_PROVIDER);
    assertThat(locationManager.getProviders(true))
        .containsExactly(MY_PROVIDER, NETWORK_PROVIDER, PASSIVE_PROVIDER);
  }

  @Test
  public void testGetProviders_Criteria() {
    Criteria network = new Criteria();
    network.setPowerRequirement(Criteria.POWER_LOW);

    Criteria gps = new Criteria();
    gps.setAccuracy(Criteria.ACCURACY_FINE);

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);

    assertThat(locationManager.getProviders(network, true)).containsExactly(NETWORK_PROVIDER);
    assertThat(locationManager.getProviders(gps, true)).containsExactly(GPS_PROVIDER);

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, false);
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);

    assertThat(locationManager.getProviders(network, true)).isEmpty();
    assertThat(locationManager.getProviders(gps, true)).isEmpty();
    assertThat(locationManager.getProviders(network, false)).containsExactly(NETWORK_PROVIDER);
    assertThat(locationManager.getProviders(gps, false)).containsExactly(GPS_PROVIDER);
  }

  @Test
  public void testGetBestProvider() {
    Criteria all = new Criteria();

    Criteria none = new Criteria();
    none.setPowerRequirement(Criteria.POWER_LOW);
    none.setAccuracy(Criteria.ACCURACY_FINE);

    shadowLocationManager.setProviderProperties(MY_PROVIDER, new ProviderProperties(all));
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    assertThat(locationManager.getBestProvider(all, true)).isEqualTo(GPS_PROVIDER);
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);
    assertThat(locationManager.getBestProvider(all, true)).isEqualTo(NETWORK_PROVIDER);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, false);
    assertThat(locationManager.getBestProvider(all, true)).isEqualTo(MY_PROVIDER);

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    assertThat(locationManager.getBestProvider(none, true)).isEqualTo(GPS_PROVIDER);
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);
    assertThat(locationManager.getBestProvider(none, true)).isEqualTo(NETWORK_PROVIDER);
    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, false);
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, false);
    assertThat(locationManager.getBestProvider(none, true)).isEqualTo(PASSIVE_PROVIDER);
  }

  @Test
  public void testIsProviderEnabled() {
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isFalse();
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isTrue();
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, false);
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isFalse();
  }

  @Test
  public void testIsProviderEnabled_Passive() {
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
    shadowLocationManager.setProviderEnabled(PASSIVE_PROVIDER, false);
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isFalse();
    shadowLocationManager.setProviderEnabled(PASSIVE_PROVIDER, true);
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
  }

  @Test
  @Config(maxSdk = VERSION_CODES.O)
  public void testSetProviderEnabled_Mode() {
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);
    assertBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertNotBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);
    assertBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, false);
    assertNotBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertNotBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_OFF);

    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    assertBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, false);
    assertNotBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertNotBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_SENSORS_ONLY);

    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);
    assertBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    assertBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_BATTERY_SAVING);

    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    assertBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    assertNotBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertNotBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_HIGH_ACCURACY);
  }

  @Test
  @Config(minSdk = R)
  public void testSetProviderEnabled_RPlus() {
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);
    assertBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertNotBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);
    assertBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertNotBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setProviderEnabled(NETWORK_PROVIDER, true);
    assertBroadcast(new Intent(PROVIDERS_CHANGED_ACTION));
    assertNotBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();
  }

  @Test
  public void testSetProviderEnabled_Listeners() {
    TestLocationListener myListener = new TestLocationListener();
    TestLocationReceiver gpsListener = new TestLocationReceiver(context);

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, myListener, null);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, gpsListener.pendingIntent);

    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, false);
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);
    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(myListener.providerEnableds).containsExactly(false, true, false, true).inOrder();
    assertThat(gpsListener.providerEnableds).containsExactly(false).inOrder();
  }

  @Test
  public void testRemoveProvider() {
    TestLocationListener myListener = new TestLocationListener();

    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);
    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, myListener, null);

    shadowLocationManager.removeProvider(MY_PROVIDER);

    shadowLocationManager.simulateLocation(createLocation(MY_PROVIDER));
    assertThat(myListener.locations).isEmpty();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void testSetLocationEnabled() {
    shadowLocationManager.setLocationEnabled(false);
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setLocationEnabled(false);
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void testIsLocationEnabled_POnly() {
    assertThat(locationManager.isLocationEnabled()).isTrue();
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);

    shadowLocationManager.setLocationEnabled(false);
    assertThat(locationManager.isLocationEnabled()).isFalse();
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_OFF);

    shadowLocationManager.setLocationEnabled(true);
    assertThat(locationManager.isLocationEnabled()).isTrue();
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isTrue();
    assertThat(getLocationMode()).isNotEqualTo(LOCATION_MODE_OFF);

    locationManager.setLocationEnabledForUser(false, Process.myUserHandle());
    assertThat(locationManager.isLocationEnabled()).isFalse();
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_OFF);

    locationManager.setLocationEnabledForUser(true, Process.myUserHandle());
    assertThat(locationManager.isLocationEnabled()).isTrue();
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isTrue();
    assertThat(getLocationMode()).isNotEqualTo(LOCATION_MODE_OFF);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void testIsLocationEnabled_QPlus() {
    assertThat(locationManager.isLocationEnabled()).isTrue();
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);

    shadowLocationManager.setLocationEnabled(false);
    assertThat(locationManager.isLocationEnabled()).isFalse();
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_OFF);

    shadowLocationManager.setLocationEnabled(true);
    assertThat(locationManager.isLocationEnabled()).isTrue();
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(getLocationMode()).isNotEqualTo(LOCATION_MODE_OFF);

    locationManager.setLocationEnabledForUser(false, Process.myUserHandle());
    assertThat(locationManager.isLocationEnabled()).isFalse();
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_OFF);

    locationManager.setLocationEnabledForUser(true, Process.myUserHandle());
    assertThat(locationManager.isLocationEnabled()).isTrue();
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(getLocationMode()).isNotEqualTo(LOCATION_MODE_OFF);
  }

  @Test
  @Config(maxSdk = VERSION_CODES.O)
  public void testSetLocationMode() {
    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);

    shadowLocationManager.setLocationMode(LOCATION_MODE_OFF);
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_OFF);
    assertThat(getProvidersAllowed()).containsExactly(MY_PROVIDER);
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setLocationMode(LOCATION_MODE_SENSORS_ONLY);
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isFalse();
    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_SENSORS_ONLY);
    assertThat(getProvidersAllowed()).containsExactly(MY_PROVIDER, GPS_PROVIDER);
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setLocationMode(LOCATION_MODE_BATTERY_SAVING);
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isFalse();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isTrue();
    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_BATTERY_SAVING);
    assertThat(getProvidersAllowed()).containsExactly(MY_PROVIDER, NETWORK_PROVIDER);
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();

    shadowLocationManager.setLocationMode(LOCATION_MODE_HIGH_ACCURACY);
    assertThat(locationManager.isProviderEnabled(MY_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(PASSIVE_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(GPS_PROVIDER)).isTrue();
    assertThat(locationManager.isProviderEnabled(NETWORK_PROVIDER)).isTrue();
    assertThat(getLocationMode()).isEqualTo(LOCATION_MODE_HIGH_ACCURACY);
    assertThat(getProvidersAllowed()).containsExactly(MY_PROVIDER, GPS_PROVIDER, NETWORK_PROVIDER);
    assertBroadcast(new Intent(MODE_CHANGED_ACTION));
    shadowOf(context).clearBroadcastIntents();
  }

  @Test
  public void testGetLastKnownLocation() {
    Location loc;

    assertThat(locationManager.getLastKnownLocation(MY_PROVIDER)).isNull();
    assertThat(locationManager.getLastKnownLocation(PASSIVE_PROVIDER)).isNull();
    assertThat(locationManager.getLastKnownLocation(GPS_PROVIDER)).isNull();
    assertThat(locationManager.getLastKnownLocation(NETWORK_PROVIDER)).isNull();

    loc = createLocation(GPS_PROVIDER);
    shadowLocationManager.simulateLocation(loc);
    assertThat(locationManager.getLastKnownLocation(MY_PROVIDER)).isNull();
    assertThat(locationManager.getLastKnownLocation(PASSIVE_PROVIDER)).isEqualTo(loc);
    assertThat(locationManager.getLastKnownLocation(GPS_PROVIDER)).isEqualTo(loc);
    assertThat(locationManager.getLastKnownLocation(NETWORK_PROVIDER)).isNull();

    loc = createLocation(MY_PROVIDER);
    shadowLocationManager.simulateLocation(loc);
    assertThat(locationManager.getLastKnownLocation(MY_PROVIDER)).isEqualTo(loc);
    assertThat(locationManager.getLastKnownLocation(PASSIVE_PROVIDER)).isEqualTo(loc);
    assertThat(locationManager.getLastKnownLocation(GPS_PROVIDER)).isNotEqualTo(loc);
    assertThat(locationManager.getLastKnownLocation(NETWORK_PROVIDER)).isNull();

    shadowLocationManager.setLastKnownLocation(PASSIVE_PROVIDER, null);
    assertThat(locationManager.getLastKnownLocation(MY_PROVIDER)).isEqualTo(loc);
    assertThat(locationManager.getLastKnownLocation(PASSIVE_PROVIDER)).isNull();
    assertThat(locationManager.getLastKnownLocation(GPS_PROVIDER)).isNotEqualTo(loc);
    assertThat(locationManager.getLastKnownLocation(NETWORK_PROVIDER)).isNull();

    loc = createLocation(NETWORK_PROVIDER);
    shadowLocationManager.setLastKnownLocation(NETWORK_PROVIDER, loc);
    assertThat(locationManager.getLastKnownLocation(MY_PROVIDER)).isNotEqualTo(loc);
    assertThat(locationManager.getLastKnownLocation(PASSIVE_PROVIDER)).isNull();
    assertThat(locationManager.getLastKnownLocation(GPS_PROVIDER)).isNotEqualTo(loc);
    assertThat(locationManager.getLastKnownLocation(NETWORK_PROVIDER)).isEqualTo(loc);
  }

  @Test
  @Config(minSdk = R)
  public void testGetCurrentLocation() {
    Location loc = createLocation(MY_PROVIDER);

    TestLocationConsumer consumer = new TestLocationConsumer();

    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);

    locationManager.getCurrentLocation(MY_PROVIDER, null, Runnable::run, consumer);

    shadowLocationManager.simulateLocation(loc);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(consumer.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc)
        .inOrder();
  }

  @Test
  @Config(minSdk = R)
  public void testGetCurrentLocation_ProviderDisabled() {
    TestLocationConsumer consumer1 = new TestLocationConsumer();
    TestLocationConsumer consumer2 = new TestLocationConsumer();

    shadowLocationManager.setProviderEnabled(MY_PROVIDER, false);

    locationManager.getCurrentLocation(GPS_PROVIDER, null, Runnable::run, consumer1);
    locationManager.getCurrentLocation(MY_PROVIDER, null, Runnable::run, consumer2);

    shadowLocationManager.setProviderEnabled(GPS_PROVIDER, false);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(consumer1.locations)
        .comparingElementsUsing(equality())
        .containsExactly((Location) null)
        .inOrder();
    assertThat(consumer2.locations)
        .comparingElementsUsing(equality())
        .containsExactly((Location) null)
        .inOrder();
  }

  @Test
  @Config(minSdk = R)
  public void testGetCurrentLocation_Timeout() {
    TestLocationConsumer consumer = new TestLocationConsumer();

    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);

    locationManager.getCurrentLocation(MY_PROVIDER, null, Runnable::run, consumer);

    shadowOf(Looper.getMainLooper())
        .idleFor(shadowOf(Looper.getMainLooper()).getLastScheduledTaskTime());

    assertThat(consumer.locations)
        .comparingElementsUsing(equality())
        .containsExactly((Location) null)
        .inOrder();
  }

  @Test
  @Config(minSdk = R)
  public void testGetCurrentLocation_Cancel() {
    Location loc = createLocation(MY_PROVIDER);

    TestLocationConsumer consumer = new TestLocationConsumer();

    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);

    CancellationSignal cs = new CancellationSignal();
    locationManager.getCurrentLocation(MY_PROVIDER, cs, Runnable::run, consumer);

    cs.cancel();
    shadowLocationManager.simulateLocation(loc);

    assertThat(consumer.locations).isEmpty();
  }

  @Test
  public void testRequestSingleUpdate_Provider_Listener() {
    Location loc1 = createLocation(GPS_PROVIDER);
    Location loc2 = createLocation(MY_PROVIDER);

    TestLocationListener gpsListener = new TestLocationListener();
    TestLocationListener myListener = new TestLocationListener();
    TestLocationListener passiveListener = new TestLocationListener();

    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);

    locationManager.requestSingleUpdate(GPS_PROVIDER, gpsListener, null);
    locationManager.requestSingleUpdate(MY_PROVIDER, myListener, null);
    locationManager.requestSingleUpdate(PASSIVE_PROVIDER, passiveListener, null);

    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(gpsListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1)
        .inOrder();
    assertThat(passiveListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1)
        .inOrder();
    assertThat(myListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc2)
        .inOrder();
  }

  @Test
  public void testRequestSingleUpdate_Provider_PendingIntent() {
    Location loc1 = createLocation(GPS_PROVIDER);
    Location loc2 = createLocation(MY_PROVIDER);

    TestLocationReceiver gpsListener = new TestLocationReceiver(context);
    TestLocationReceiver myListener = new TestLocationReceiver(context);
    TestLocationReceiver passiveListener = new TestLocationReceiver(context);

    shadowLocationManager.setProviderEnabled(MY_PROVIDER, true);

    locationManager.requestSingleUpdate(GPS_PROVIDER, gpsListener.pendingIntent);
    locationManager.requestSingleUpdate(MY_PROVIDER, myListener.pendingIntent);
    locationManager.requestSingleUpdate(PASSIVE_PROVIDER, passiveListener.pendingIntent);

    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(gpsListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1)
        .inOrder();
    assertThat(passiveListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1)
        .inOrder();
    assertThat(myListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc2)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_Provider_Listener() {
    Location loc1 = createLocation(NETWORK_PROVIDER);
    Location loc2 = createLocation(NETWORK_PROVIDER);
    Location loc3 = createLocation(NETWORK_PROVIDER);
    TestLocationListener networkListener = new TestLocationListener();
    TestLocationListener passiveListener = new TestLocationListener();

    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, networkListener, null);
    locationManager.requestLocationUpdates(PASSIVE_PROVIDER, 0, 0, passiveListener, null);

    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    locationManager.removeUpdates(networkListener);
    shadowLocationManager.simulateLocation(loc3);
    locationManager.removeUpdates(passiveListener);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(networkListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2)
        .inOrder();
    assertThat(passiveListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2, loc3)
        .inOrder();
  }

  @Test
  @Config(minSdk = R)
  public void testRequestLocationUpdates_Provider_Listener_Executor() {
    Location loc1 = createLocation(NETWORK_PROVIDER);
    Location loc2 = createLocation(NETWORK_PROVIDER);
    Location loc3 = createLocation(NETWORK_PROVIDER);
    TestLocationListener networkListener = new TestLocationListener();
    TestLocationListener passiveListener = new TestLocationListener();

    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, Runnable::run, networkListener);
    locationManager.requestLocationUpdates(PASSIVE_PROVIDER, 0, 0, Runnable::run, passiveListener);

    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    locationManager.removeUpdates(networkListener);
    shadowLocationManager.simulateLocation(loc3);
    locationManager.removeUpdates(passiveListener);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(networkListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2)
        .inOrder();
    assertThat(passiveListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2, loc3)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_Provider_PendingIntent() {
    Location loc1 = createLocation(NETWORK_PROVIDER);
    Location loc2 = createLocation(NETWORK_PROVIDER);
    Location loc3 = createLocation(NETWORK_PROVIDER);
    TestLocationReceiver networkListener = new TestLocationReceiver(context);
    TestLocationReceiver passiveListener = new TestLocationReceiver(context);

    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, networkListener.pendingIntent);
    locationManager.requestLocationUpdates(PASSIVE_PROVIDER, 0, 0, passiveListener.pendingIntent);

    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    locationManager.removeUpdates(networkListener.pendingIntent);
    shadowLocationManager.simulateLocation(loc3);
    locationManager.removeUpdates(passiveListener.pendingIntent);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(networkListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2)
        .inOrder();
    assertThat(passiveListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2, loc3)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_Criteria_Listener() {
    Location loc = createLocation(GPS_PROVIDER);
    TestLocationListener gpsListener = new TestLocationListener();
    Criteria gps = new Criteria();
    gps.setAccuracy(Criteria.ACCURACY_FINE);

    locationManager.requestLocationUpdates(0, 0, gps, gpsListener, null);

    shadowLocationManager.simulateLocation(loc);
    locationManager.removeUpdates(gpsListener);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(gpsListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_Criteria_PendingIntent() {
    Location loc = createLocation(GPS_PROVIDER);
    TestLocationReceiver gpsReceiver = new TestLocationReceiver(context);
    Criteria gps = new Criteria();
    gps.setAccuracy(Criteria.ACCURACY_FINE);

    locationManager.requestLocationUpdates(0, 0, gps, gpsReceiver.pendingIntent);

    shadowLocationManager.simulateLocation(loc);
    locationManager.removeUpdates(gpsReceiver.pendingIntent);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(gpsReceiver.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc)
        .inOrder();
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void testRequestLocationUpdates_LocationRequest() {
    Location loc1 = createLocation(NETWORK_PROVIDER);
    Location loc2 = createLocation(NETWORK_PROVIDER);
    Location loc3 = createLocation(NETWORK_PROVIDER);
    TestLocationListener networkListener = new TestLocationListener();

    locationManager.requestLocationUpdates(
        LocationRequest.createFromDeprecatedProvider(NETWORK_PROVIDER, 0, 0, false),
        networkListener,
        null);

    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    locationManager.removeUpdates(networkListener);
    shadowLocationManager.simulateLocation(loc3);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(networkListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2)
        .inOrder();
  }

  @Test
  @Config(minSdk = R)
  public void testRequestLocationUpdates_LocationRequest_Executor() {
    Location loc1 = createLocation(NETWORK_PROVIDER);
    Location loc2 = createLocation(NETWORK_PROVIDER);
    Location loc3 = createLocation(NETWORK_PROVIDER);
    TestLocationListener networkListener = new TestLocationListener();

    locationManager.requestLocationUpdates(
        LocationRequest.createFromDeprecatedProvider(NETWORK_PROVIDER, 0, 0, false),
        Runnable::run,
        networkListener);

    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    locationManager.removeUpdates(networkListener);
    shadowLocationManager.simulateLocation(loc3);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(networkListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_MultipleProviders_Listener() {
    Location loc1 = createLocation(MY_PROVIDER);
    Location loc2 = createLocation(GPS_PROVIDER);
    Location loc3 = createLocation(NETWORK_PROVIDER);

    TestLocationListener myListener = new TestLocationListener();
    TestLocationListener passiveListener = new TestLocationListener();

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, myListener, null);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, myListener, null);
    locationManager.requestLocationUpdates(PASSIVE_PROVIDER, 0, 0, passiveListener, null);
    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    shadowLocationManager.simulateLocation(loc3);
    locationManager.removeUpdates(myListener);
    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    locationManager.removeUpdates(passiveListener);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(myListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2)
        .inOrder();
    assertThat(passiveListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2, loc3, loc1, loc2)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_MultipleProviders_PendingIntent() {
    Location loc1 = createLocation(MY_PROVIDER);
    Location loc2 = createLocation(GPS_PROVIDER);
    Location loc3 = createLocation(NETWORK_PROVIDER);

    TestLocationReceiver myListener = new TestLocationReceiver(context);
    TestLocationReceiver passiveListener = new TestLocationReceiver(context);

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, myListener.pendingIntent);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, myListener.pendingIntent);
    locationManager.requestLocationUpdates(PASSIVE_PROVIDER, 0, 0, passiveListener.pendingIntent);
    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    shadowLocationManager.simulateLocation(loc3);
    locationManager.removeUpdates(myListener.pendingIntent);
    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    locationManager.removeUpdates(passiveListener.pendingIntent);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(myListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2)
        .inOrder();
    assertThat(passiveListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2, loc3, loc1, loc2)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_MultipleProviders_Mixed() {
    Location loc1 = createLocation(MY_PROVIDER);
    Location loc2 = createLocation(GPS_PROVIDER);
    Location loc3 = createLocation(NETWORK_PROVIDER);

    TestLocationReceiver myListener1 = new TestLocationReceiver(context);
    TestLocationListener myListener2 = new TestLocationListener();

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, myListener1.pendingIntent);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, myListener1.pendingIntent);
    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, myListener2, null);
    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, myListener2, null);
    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    shadowLocationManager.simulateLocation(loc3);
    locationManager.removeUpdates(myListener1.pendingIntent);
    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    shadowLocationManager.simulateLocation(loc3);
    locationManager.removeUpdates(myListener2);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(myListener1.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc2)
        .inOrder();
    assertThat(myListener2.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1, loc3, loc1, loc3)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_DoublePassive() {
    Location loc = createLocation(PASSIVE_PROVIDER);
    TestLocationListener passiveListener = new TestLocationListener();

    locationManager.requestLocationUpdates(PASSIVE_PROVIDER, 0, 0, passiveListener);
    shadowLocationManager.simulateLocation(loc);
    locationManager.removeUpdates(passiveListener);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(passiveListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_Duplicate() {
    Location loc = createLocation(GPS_PROVIDER);
    TestLocationListener passiveListener = new TestLocationListener();

    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, passiveListener);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, passiveListener);
    shadowLocationManager.simulateLocation(loc);
    locationManager.removeUpdates(passiveListener);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(passiveListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc)
        .inOrder();
  }

  @Test
  public void testRequestLocationUpdates_SelfRemoval() {
    Location loc = createLocation(NETWORK_PROVIDER);

    TestLocationListener listener = new TestLocationListenerSelfRemoval(locationManager);

    locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, listener);
    shadowLocationManager.simulateLocation(loc);
    shadowOf(Looper.getMainLooper()).idle();
    shadowLocationManager.simulateLocation(loc);
    locationManager.removeUpdates(listener);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(listener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc)
        .inOrder();
  }

  @Test
  public void testSimulateLocation_FastestInterval() {
    Location loc1 = createLocation(MY_PROVIDER);
    loc1.setTime(1);
    Location loc2 = createLocation(MY_PROVIDER);
    loc2.setTime(10);

    TestLocationListener myListener = new TestLocationListener();

    locationManager.requestLocationUpdates(MY_PROVIDER, 10, 0, myListener);
    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    locationManager.removeUpdates(myListener);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(myListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1)
        .inOrder();
    assertThat(locationManager.getLastKnownLocation(MY_PROVIDER)).isEqualTo(loc2);
  }

  @Test
  public void testSimulateLocation_MinDistance() {
    Location loc1 = createLocation(MY_PROVIDER, 1, 2);
    Location loc2 = createLocation(MY_PROVIDER, 1.5, 2.5);

    TestLocationListener myListener = new TestLocationListener();

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 200000, myListener);
    shadowLocationManager.simulateLocation(loc1);
    shadowLocationManager.simulateLocation(loc2);
    locationManager.removeUpdates(myListener);

    shadowOf(Looper.getMainLooper()).idle();

    assertThat(myListener.locations)
        .comparingElementsUsing(equality())
        .containsExactly(loc1)
        .inOrder();
    assertThat(locationManager.getLastKnownLocation(MY_PROVIDER)).isEqualTo(loc2);
  }

  @Test
  public void testLocationUpdates_NullListener() {
    try {
      locationManager.requestSingleUpdate(GPS_PROVIDER, null, null);
      fail();
    } catch (Exception e) {
      // pass
    }

    try {
      locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, (LocationListener) null, null);
      fail();
    } catch (Exception e) {
      // pass
    }
  }

  @Test
  public void testLocationUpdates_NullPendingIntent() {
    try {
      locationManager.requestSingleUpdate(GPS_PROVIDER, null);
      fail();
    } catch (Exception e) {
      // pass
    }

    try {
      locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, (PendingIntent) null);
      fail();
    } catch (Exception e) {
      // pass
    }
  }

  @Test
  public void testGetLocationUpdateListeners() {
    TestLocationListener listener1 = new TestLocationListener();
    TestLocationListener listener2 = new TestLocationListener();

    assertThat(shadowLocationManager.getLocationUpdateListeners()).isEmpty();

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, listener1);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener1);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener2);
    assertThat(shadowLocationManager.getLocationUpdateListeners())
        .containsExactly(listener1, listener2);
  }

  @Test
  public void testGetLocationUpdateListeners_Provider() {
    TestLocationListener listener1 = new TestLocationListener();
    TestLocationListener listener2 = new TestLocationListener();

    assertThat(shadowLocationManager.getLocationUpdateListeners(MY_PROVIDER)).isEmpty();
    assertThat(shadowLocationManager.getLocationUpdateListeners(GPS_PROVIDER)).isEmpty();

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, listener1);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener1);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener2);
    assertThat(shadowLocationManager.getLocationUpdateListeners(MY_PROVIDER))
        .containsExactly(listener1);
    assertThat(shadowLocationManager.getLocationUpdateListeners(GPS_PROVIDER))
        .containsExactly(listener1, listener2);

    locationManager.removeUpdates(listener1);
    locationManager.removeUpdates(listener2);
    assertThat(shadowLocationManager.getLocationUpdateListeners(MY_PROVIDER)).isEmpty();
    assertThat(shadowLocationManager.getLocationUpdateListeners(GPS_PROVIDER)).isEmpty();
  }

  @Test
  public void testGetLocationUpdatePendingIntents() {
    TestLocationReceiver listener1 = new TestLocationReceiver(context);
    TestLocationReceiver listener2 = new TestLocationReceiver(context);

    assertThat(shadowLocationManager.getLocationUpdatePendingIntents()).isEmpty();

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, listener1.pendingIntent);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener1.pendingIntent);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener2.pendingIntent);
    assertThat(shadowLocationManager.getLocationUpdatePendingIntents())
        .containsExactly(listener1.pendingIntent, listener2.pendingIntent);
  }

  @Test
  public void testGetLocationUpdatePendingIntents_Duplicate() {
    Intent intent = new Intent("myAction");
    PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, intent, 0);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, intent, 0);

    assertThat(shadowLocationManager.getLocationUpdatePendingIntents()).isEmpty();

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, pendingIntent1);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, pendingIntent1);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, pendingIntent2);
    assertThat(shadowLocationManager.getLocationUpdatePendingIntents())
        .containsExactly(pendingIntent2);

    locationManager.removeUpdates(pendingIntent1);
    assertThat(shadowLocationManager.getLocationUpdatePendingIntents()).isEmpty();
  }

  @Test
  public void testGetLocationUpdatePendingIntents_Provider() {
    TestLocationReceiver listener1 = new TestLocationReceiver(context);
    TestLocationReceiver listener2 = new TestLocationReceiver(context);

    assertThat(shadowLocationManager.getLocationUpdatePendingIntents(MY_PROVIDER)).isEmpty();
    assertThat(shadowLocationManager.getLocationUpdatePendingIntents(GPS_PROVIDER)).isEmpty();

    locationManager.requestLocationUpdates(MY_PROVIDER, 0, 0, listener1.pendingIntent);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener1.pendingIntent);
    locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, listener2.pendingIntent);
    assertThat(shadowLocationManager.getLocationUpdatePendingIntents(MY_PROVIDER))
        .containsExactly(listener1.pendingIntent);
    assertThat(shadowLocationManager.getLocationUpdatePendingIntents(GPS_PROVIDER))
        .containsExactly(listener1.pendingIntent, listener2.pendingIntent);

    locationManager.removeUpdates(listener1.pendingIntent);
    locationManager.removeUpdates(listener2.pendingIntent);
    assertThat(shadowLocationManager.getLocationUpdatePendingIntents(MY_PROVIDER)).isEmpty();
    assertThat(shadowLocationManager.getLocationUpdatePendingIntents(GPS_PROVIDER)).isEmpty();
  }

  @Test
  public void testAddGpsStatusListener() {
    GpsStatus.Listener listener = new TestGpsListener();

    assertThat(shadowLocationManager.getGpsStatusListeners()).isEmpty();
    locationManager.addGpsStatusListener(listener);
    assertThat(shadowLocationManager.getGpsStatusListeners()).containsExactly(listener);
    locationManager.removeGpsStatusListener(listener);
    assertThat(shadowLocationManager.getGpsStatusListeners()).isEmpty();
  }

  @Test
  @Config(minSdk = R)
  public void testRegisterGnssStatusCallback() {
    TestGnssCallback callback = new TestGnssCallback();
    GnssStatus status1 = new GnssStatus.Builder().build();
    GnssStatus status2 = new GnssStatus.Builder().build();

    shadowLocationManager.sendGnssStatus(status1);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(callback.gnssStatuses).isEmpty();

    locationManager.registerGnssStatusCallback(callback, new Handler(Looper.getMainLooper()));

    shadowLocationManager.sendGnssStatus(status1);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(callback.gnssStatuses).containsExactly(status1).inOrder();

    shadowLocationManager.sendGnssStatus(status2);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(callback.gnssStatuses).containsExactly(status1, status2).inOrder();

    locationManager.unregisterGnssStatusCallback(callback);
    shadowLocationManager.sendGnssStatus(status1);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(callback.gnssStatuses).containsExactly(status1, status2).inOrder();
  }

  @Test
  @Config(minSdk = R)
  public void testRegisterGnssStatusCallback_executor() {
    TestGnssCallback callback = new TestGnssCallback();
    GnssStatus status1 = new GnssStatus.Builder().build();
    GnssStatus status2 = new GnssStatus.Builder().build();

    shadowLocationManager.sendGnssStatus(status1);
    assertThat(callback.gnssStatuses).isEmpty();

    locationManager.registerGnssStatusCallback(Runnable::run, callback);

    shadowLocationManager.sendGnssStatus(status1);
    assertThat(callback.gnssStatuses).containsExactly(status1).inOrder();

    shadowLocationManager.sendGnssStatus(status2);
    assertThat(callback.gnssStatuses).containsExactly(status1, status2).inOrder();

    locationManager.unregisterGnssStatusCallback(callback);
    assertThat(callback.gnssStatuses).containsExactly(status1, status2).inOrder();
  }

  @Test
  @Config(minSdk = N)
  public void testAddNmeaListener() {
    TestOnNmeaMessageListener callback = new TestOnNmeaMessageListener();

    shadowLocationManager.sendNmeaMessage("message", 0);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(callback.nmeaMessages).isEmpty();

    locationManager.addNmeaListener(callback, new Handler(Looper.getMainLooper()));

    shadowLocationManager.sendNmeaMessage("message1", 0);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(callback.nmeaMessages).containsExactly("message1").inOrder();

    shadowLocationManager.sendNmeaMessage("message2", 0);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(callback.nmeaMessages).containsExactly("message1", "message2").inOrder();

    locationManager.removeNmeaListener(callback);

    shadowLocationManager.sendNmeaMessage("message1", 0);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(callback.nmeaMessages).containsExactly("message1", "message2").inOrder();
  }

  @Test
  @Config(minSdk = R)
  public void testAddNmeaListener_executor() {
    TestOnNmeaMessageListener callback = new TestOnNmeaMessageListener();

    shadowLocationManager.sendNmeaMessage("message", 0);
    assertThat(callback.nmeaMessages).isEmpty();

    locationManager.addNmeaListener(Runnable::run, callback);

    shadowLocationManager.sendNmeaMessage("message1", 0);
    assertThat(callback.nmeaMessages).containsExactly("message1").inOrder();

    shadowLocationManager.sendNmeaMessage("message2", 0);
    assertThat(callback.nmeaMessages).containsExactly("message1", "message2").inOrder();

    locationManager.removeNmeaListener(callback);

    shadowLocationManager.sendNmeaMessage("message1", 0);
    assertThat(callback.nmeaMessages).containsExactly("message1", "message2").inOrder();
  }

  @Test
  @Config(minSdk = R)
  public void testRegisterAntennaInfoListener() {
    TestGnssAntennaInfoListener callback = new TestGnssAntennaInfoListener();
    List<GnssAntennaInfo> events1 =
        Collections.singletonList(
            new GnssAntennaInfo.Builder()
                .setPhaseCenterOffset(new PhaseCenterOffset(0, 0, 0, 0, 0, 0))
                .build());
    List<GnssAntennaInfo> events2 =
        Collections.singletonList(
            new GnssAntennaInfo.Builder()
                .setPhaseCenterOffset(new PhaseCenterOffset(0, 0, 0, 0, 0, 0))
                .build());

    shadowLocationManager.sendGnssAntennaInfo(events1);
    assertThat(callback.antennaInfos).isEmpty();

    locationManager.registerAntennaInfoListener(Runnable::run, callback);

    shadowLocationManager.sendGnssAntennaInfo(events1);
    assertThat(callback.antennaInfos).containsExactly(events1).inOrder();

    shadowLocationManager.sendGnssAntennaInfo(events2);
    assertThat(callback.antennaInfos).containsExactly(events1, events2).inOrder();

    locationManager.unregisterAntennaInfoListener(callback);

    shadowLocationManager.sendGnssAntennaInfo(events1);
    assertThat(callback.antennaInfos).containsExactly(events1, events2).inOrder();
  }

  private static final Random random = new Random(101);

  private static Location createLocation(String provider) {
    return createLocation(provider, random.nextDouble() * 180 - 90, random.nextDouble() * 180 - 90);
  }

  private static Location createLocation(String provider, double lat, double lon) {
    Location location = new Location(provider);
    location.setLatitude(lat);
    location.setLongitude(lon);
    return location;
  }

  private int getLocationMode() {
    return Secure.getInt(context.getContentResolver(), LOCATION_MODE, LOCATION_MODE_OFF);
  }

  private Set<String> getProvidersAllowed() {
    String providersAllowed =
        Secure.getString(context.getContentResolver(), LOCATION_PROVIDERS_ALLOWED);
    if (TextUtils.isEmpty(providersAllowed)) {
      return Collections.emptySet();
    }

    return new HashSet<>(Arrays.asList(providersAllowed.split(",")));
  }

  private void assertBroadcast(Intent... intents) {
    for (Intent intent : intents) {
      boolean found = false;
      for (Intent broadcast : shadowOf(context).getBroadcastIntents()) {
        if (broadcast.filterEquals(intent)) {
          found = true;
          break;
        }
      }

      if (!found) {
        assertThat(shadowOf(context).getBroadcastIntents()).contains(intent);
      }
    }
  }

  private void assertNotBroadcast(Intent... intents) {
    for (Intent intent : intents) {
      for (Intent broadcast : shadowOf(context).getBroadcastIntents()) {
        if (broadcast.filterEquals(intent)) {
          assertThat(shadowOf(context).getBroadcastIntents()).doesNotContain(broadcast);
        }
      }
    }
  }

  private static class TestLocationReceiver extends BroadcastReceiver {
    private final PendingIntent pendingIntent;
    private final ArrayList<Boolean> providerEnableds = new ArrayList<>();
    private final ArrayList<Location> locations = new ArrayList<>();

    private TestLocationReceiver(Context context) {
      Intent intent = new Intent(Integer.toString(random.nextInt()));
      pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
      context.registerReceiver(this, new IntentFilter(intent.getAction()));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
        locations.add(intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED));
      }
      if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)) {
        providerEnableds.add(intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false));
      }
    }
  }

  private static class TestLocationConsumer implements Consumer<Location> {
    final ArrayList<Location> locations = new ArrayList<>();

    @Override
    public void accept(Location location) {
      locations.add(location);
    }
  }

  private static class TestLocationListener implements LocationListener {
    final ArrayList<Boolean> providerEnableds = new ArrayList<>();
    final ArrayList<Location> locations = new ArrayList<>();

    @Override
    public void onLocationChanged(Location location) {
      locations.add(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
      providerEnableds.add(true);
    }

    @Override
    public void onProviderDisabled(String s) {
      providerEnableds.add(false);
    }
  }

  private static class TestLocationListenerSelfRemoval extends TestLocationListener {

    private final LocationManager locationManager;

    public TestLocationListenerSelfRemoval(LocationManager locationManager) {
      this.locationManager = locationManager;
    }

    @Override
    public void onLocationChanged(Location location) {
      locationManager.removeUpdates(this);
      super.onLocationChanged(location);
    }
  }

  private static class TestGpsListener implements GpsStatus.Listener {

    @Override
    public void onGpsStatusChanged(int event) {}
  }

  private static class TestGnssCallback extends GnssStatus.Callback {
    final ArrayList<GnssStatus> gnssStatuses = new ArrayList<>();

    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {
      gnssStatuses.add(status);
    }
  }

  private static class TestOnNmeaMessageListener implements OnNmeaMessageListener {
    final ArrayList<String> nmeaMessages = new ArrayList<>();

    @Override
    public void onNmeaMessage(String message, long timestamp) {
      nmeaMessages.add(message);
    }
  }

  private static class TestGnssAntennaInfoListener implements Listener {
    final ArrayList<List<GnssAntennaInfo>> antennaInfos = new ArrayList<>();

    @Override
    public void onGnssAntennaInfoReceived(@NonNull List<GnssAntennaInfo> gnssAntennaInfos) {
      antennaInfos.add(gnssAntennaInfos);
    }
  }
}
