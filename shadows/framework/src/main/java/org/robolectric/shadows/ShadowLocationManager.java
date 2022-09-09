package org.robolectric.shadows;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static android.os.Build.VERSION_CODES.P;
import static android.provider.Settings.Secure.LOCATION_MODE;
import static android.provider.Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
import static android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
import static android.provider.Settings.Secure.LOCATION_MODE_OFF;
import static android.provider.Settings.Secure.LOCATION_MODE_SENSORS_ONLY;
import static android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GnssAntennaInfo;
import android.location.GnssMeasurementsEvent;
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
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.ShadowSettings.ShadowSecure;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow for {@link LocationManager}. Note that the default state of location on Android devices is
 * location on, gps provider enabled, network provider disabled.
 */
@SuppressWarnings("deprecation")
@Implements(value = LocationManager.class, looseSignatures = true)
public class ShadowLocationManager {

  private static final long GET_CURRENT_LOCATION_TIMEOUT_MS = 30 * 1000;
  private static final long MAX_CURRENT_LOCATION_AGE_MS = 10 * 1000;

  /**
   * ProviderProperties is not public prior to S, so a new class is required to represent it prior
   * to that platform.
   */
  public static class ProviderProperties {
    @Nullable private final Object properties;

    private final boolean requiresNetwork;
    private final boolean requiresSatellite;
    private final boolean requiresCell;
    private final boolean hasMonetaryCost;
    private final boolean supportsAltitude;
    private final boolean supportsSpeed;
    private final boolean supportsBearing;
    private final int powerRequirement;
    private final int accuracy;

    @RequiresApi(VERSION_CODES.S)
    ProviderProperties(android.location.provider.ProviderProperties properties) {
      this.properties = Objects.requireNonNull(properties);
      this.requiresNetwork = false;
      this.requiresSatellite = false;
      this.requiresCell = false;
      this.hasMonetaryCost = false;
      this.supportsAltitude = false;
      this.supportsSpeed = false;
      this.supportsBearing = false;
      this.powerRequirement = 0;
      this.accuracy = 0;
    }

    public ProviderProperties(
        boolean requiresNetwork,
        boolean requiresSatellite,
        boolean requiresCell,
        boolean hasMonetaryCost,
        boolean supportsAltitude,
        boolean supportsSpeed,
        boolean supportsBearing,
        int powerRequirement,
        int accuracy) {
      if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S) {
        properties =
            new android.location.provider.ProviderProperties.Builder()
                .setHasNetworkRequirement(requiresNetwork)
                .setHasSatelliteRequirement(requiresSatellite)
                .setHasCellRequirement(requiresCell)
                .setHasMonetaryCost(hasMonetaryCost)
                .setHasAltitudeSupport(supportsAltitude)
                .setHasSpeedSupport(supportsSpeed)
                .setHasBearingSupport(supportsBearing)
                .setPowerUsage(powerRequirement)
                .setAccuracy(accuracy)
                .build();
      } else {
        properties = null;
      }

      this.requiresNetwork = requiresNetwork;
      this.requiresSatellite = requiresSatellite;
      this.requiresCell = requiresCell;
      this.hasMonetaryCost = hasMonetaryCost;
      this.supportsAltitude = supportsAltitude;
      this.supportsSpeed = supportsSpeed;
      this.supportsBearing = supportsBearing;
      this.powerRequirement = powerRequirement;
      this.accuracy = accuracy;
    }

    public ProviderProperties(Criteria criteria) {
      this(
          false,
          false,
          false,
          criteria.isCostAllowed(),
          criteria.isAltitudeRequired(),
          criteria.isSpeedRequired(),
          criteria.isBearingRequired(),
          criteria.getPowerRequirement(),
          criteria.getAccuracy());
    }

    @RequiresApi(VERSION_CODES.S)
    android.location.provider.ProviderProperties getProviderProperties() {
      return (android.location.provider.ProviderProperties) Objects.requireNonNull(properties);
    }

    Object getLegacyProviderProperties() {
      try {
        return ReflectionHelpers.callConstructor(
            Class.forName("com.android.internal.location.ProviderProperties"),
            ClassParameter.from(boolean.class, requiresNetwork),
            ClassParameter.from(boolean.class, requiresSatellite),
            ClassParameter.from(boolean.class, requiresCell),
            ClassParameter.from(boolean.class, hasMonetaryCost),
            ClassParameter.from(boolean.class, supportsAltitude),
            ClassParameter.from(boolean.class, supportsSpeed),
            ClassParameter.from(boolean.class, supportsBearing),
            ClassParameter.from(int.class, powerRequirement),
            ClassParameter.from(int.class, accuracy));
      } catch (ClassNotFoundException c) {
        throw new RuntimeException("Unable to load old ProviderProperties class", c);
      }
    }

    public boolean hasNetworkRequirement() {
      if (properties != null) {
        return ((android.location.provider.ProviderProperties) properties).hasNetworkRequirement();
      } else {
        return requiresNetwork;
      }
    }

    public boolean hasSatelliteRequirement() {
      if (properties != null) {
        return ((android.location.provider.ProviderProperties) properties)
            .hasSatelliteRequirement();
      } else {
        return requiresSatellite;
      }
    }

    public boolean isRequiresCell() {
      if (properties != null) {
        return ((android.location.provider.ProviderProperties) properties).hasCellRequirement();
      } else {
        return requiresCell;
      }
    }

    public boolean isHasMonetaryCost() {
      if (properties != null) {
        return ((android.location.provider.ProviderProperties) properties).hasMonetaryCost();
      } else {
        return hasMonetaryCost;
      }
    }

    public boolean hasAltitudeSupport() {
      if (properties != null) {
        return ((android.location.provider.ProviderProperties) properties).hasAltitudeSupport();
      } else {
        return supportsAltitude;
      }
    }

    public boolean hasSpeedSupport() {
      if (properties != null) {
        return ((android.location.provider.ProviderProperties) properties).hasSpeedSupport();
      } else {
        return supportsSpeed;
      }
    }

    public boolean hasBearingSupport() {
      if (properties != null) {
        return ((android.location.provider.ProviderProperties) properties).hasBearingSupport();
      } else {
        return supportsBearing;
      }
    }

    public int getPowerUsage() {
      if (properties != null) {
        return ((android.location.provider.ProviderProperties) properties).getPowerUsage();
      } else {
        return powerRequirement;
      }
    }

    public int getAccuracy() {
      if (properties != null) {
        return ((android.location.provider.ProviderProperties) properties).getAccuracy();
      } else {
        return accuracy;
      }
    }

    boolean meetsCriteria(Criteria criteria) {
      if (criteria.getAccuracy() != Criteria.NO_REQUIREMENT
          && criteria.getAccuracy() < getAccuracy()) {
        return false;
      }
      if (criteria.getPowerRequirement() != Criteria.NO_REQUIREMENT
          && criteria.getPowerRequirement() < getPowerUsage()) {
        return false;
      }
      if (criteria.isAltitudeRequired() && !hasAltitudeSupport()) {
        return false;
      }
      if (criteria.isSpeedRequired() && !hasSpeedSupport()) {
        return false;
      }
      if (criteria.isBearingRequired() && !hasBearingSupport()) {
        return false;
      }
      if (!criteria.isCostAllowed() && hasMonetaryCost) {
        return false;
      }
      return true;
    }
  }

  @GuardedBy("ShadowLocationManager.class")
  @Nullable
  private static Constructor<LocationProvider> locationProviderConstructor;

  @RealObject private LocationManager realLocationManager;

  @GuardedBy("providers")
  private final HashSet<ProviderEntry> providers = new HashSet<>();

  @GuardedBy("gpsStatusListeners")
  private final HashSet<GpsStatus.Listener> gpsStatusListeners = new HashSet<>();

  @GuardedBy("gnssStatusTransports")
  private final CopyOnWriteArrayList<GnssStatusCallbackTransport> gnssStatusTransports =
      new CopyOnWriteArrayList<>();

  @GuardedBy("nmeaMessageTransports")
  private final CopyOnWriteArrayList<OnNmeaMessageListenerTransport> nmeaMessageTransports =
      new CopyOnWriteArrayList<>();

  @GuardedBy("gnssMeasurementTransports")
  private final CopyOnWriteArrayList<GnssMeasurementsEventCallbackTransport>
      gnssMeasurementTransports = new CopyOnWriteArrayList<>();

  @GuardedBy("gnssAntennaInfoTransports")
  private final CopyOnWriteArrayList<GnssAntennaInfoListenerTransport> gnssAntennaInfoTransports =
      new CopyOnWriteArrayList<>();

  @Nullable private String gnssHardwareModelName;

  private int gnssYearOfHardware;

  public ShadowLocationManager() {
    // create default providers
    providers.add(
        new ProviderEntry(
            GPS_PROVIDER,
            new ProviderProperties(
                true,
                true,
                false,
                false,
                true,
                true,
                true,
                Criteria.POWER_HIGH,
                Criteria.ACCURACY_FINE)));
    providers.add(
        new ProviderEntry(
            NETWORK_PROVIDER,
            new ProviderProperties(
                false,
                false,
                false,
                false,
                true,
                true,
                true,
                Criteria.POWER_LOW,
                Criteria.ACCURACY_COARSE)));
    providers.add(
        new ProviderEntry(
            PASSIVE_PROVIDER,
            new ProviderProperties(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                Criteria.POWER_LOW,
                Criteria.ACCURACY_COARSE)));
  }

  @Implementation
  protected List<String> getAllProviders() {
    ArrayList<String> allProviders = new ArrayList<>();
    for (ProviderEntry providerEntry : getProviderEntries()) {
      allProviders.add(providerEntry.getName());
    }
    return allProviders;
  }

  @Implementation
  @Nullable
  protected LocationProvider getProvider(String name) {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.KITKAT) {
      // jelly bean has no way to properly construct a LocationProvider, we give up
      return null;
    }

    ProviderEntry providerEntry = getProviderEntry(name);
    if (providerEntry == null) {
      return null;
    }

    ProviderProperties properties = providerEntry.getProperties();
    if (properties == null) {
      return null;
    }

    try {
      synchronized (ShadowLocationManager.class) {
        if (locationProviderConstructor == null) {
          if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S) {
            locationProviderConstructor =
                LocationProvider.class.getConstructor(
                    String.class, android.location.provider.ProviderProperties.class);
          } else {
            locationProviderConstructor =
                LocationProvider.class.getConstructor(
                    String.class,
                    Class.forName("com.android.internal.location.ProviderProperties"));
          }
          locationProviderConstructor.setAccessible(true);
        }

        if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S) {
          return locationProviderConstructor.newInstance(name, properties.getProviderProperties());
        } else {
          return locationProviderConstructor.newInstance(
              name, properties.getLegacyProviderProperties());
        }
      }
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
  }

  @Implementation
  protected List<String> getProviders(boolean enabledOnly) {
    return getProviders(null, enabledOnly);
  }

  @Implementation
  protected List<String> getProviders(@Nullable Criteria criteria, boolean enabled) {
    ArrayList<String> matchingProviders = new ArrayList<>();
    for (ProviderEntry providerEntry : getProviderEntries()) {
      if (enabled && !isProviderEnabled(providerEntry.getName())) {
        continue;
      }
      if (criteria != null && !providerEntry.meetsCriteria(criteria)) {
        continue;
      }
      matchingProviders.add(providerEntry.getName());
    }
    return matchingProviders;
  }

  @Implementation
  @Nullable
  protected String getBestProvider(Criteria criteria, boolean enabled) {
    List<String> providers = getProviders(criteria, enabled);
    if (providers.isEmpty()) {
      providers = getProviders(null, enabled);
    }

    if (!providers.isEmpty()) {
      if (providers.contains(GPS_PROVIDER)) {
        return GPS_PROVIDER;
      } else if (providers.contains(NETWORK_PROVIDER)) {
        return NETWORK_PROVIDER;
      } else {
        return providers.get(0);
      }
    }

    return null;
  }

  @Implementation(minSdk = VERSION_CODES.S)
  @Nullable
  protected Object getProviderProperties(Object providerStr) {
    String provider = (String) providerStr;
    if (provider == null) {
      throw new IllegalArgumentException();
    }

    ProviderEntry providerEntry = getProviderEntry(provider);
    if (providerEntry == null) {
      return null;
    }

    ProviderProperties properties = providerEntry.getProperties();
    if (properties == null) {
      return null;
    }

    return properties.getProviderProperties();
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected boolean hasProvider(String provider) {
    if (provider == null) {
      throw new IllegalArgumentException();
    }

    return getProviderEntry(provider) != null;
  }

  @Implementation
  protected boolean isProviderEnabled(String provider) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.P) {
      if (!isLocationEnabled()) {
        return false;
      }
    }

    ProviderEntry entry = getProviderEntry(provider);
    return entry != null && entry.isEnabled();
  }

  /** Completely removes a provider. */
  public void removeProvider(String name) {
    removeProviderEntry(name);
  }

  /**
   * Sets the properties of the given provider. The provider will be created if it doesn't exist
   * already. This overload functions for all Android SDK levels.
   */
  public void setProviderProperties(String name, @Nullable ProviderProperties properties) {
    getOrCreateProviderEntry(Objects.requireNonNull(name)).setProperties(properties);
  }

  /**
   * Sets the given provider enabled or disabled. The provider will be created if it doesn't exist
   * already. On P and above, location must also be enabled via {@link #setLocationEnabled(boolean)}
   * in order for a provider to be considered enabled.
   */
  public void setProviderEnabled(String name, boolean enabled) {
    getOrCreateProviderEntry(name).setEnabled(enabled);
  }

  // @SystemApi
  @Implementation(minSdk = VERSION_CODES.P)
  protected boolean isLocationEnabledForUser(UserHandle userHandle) {
    return isLocationEnabled();
  }

  @Implementation(minSdk = P)
  protected boolean isLocationEnabled() {
    return getLocationMode() != LOCATION_MODE_OFF;
  }

  // @SystemApi
  @Implementation(minSdk = VERSION_CODES.P)
  protected void setLocationEnabledForUser(boolean enabled, UserHandle userHandle) {
    setLocationModeInternal(enabled ? LOCATION_MODE_HIGH_ACCURACY : LOCATION_MODE_OFF);
  }

  /**
   * On P and above, turns location on or off. On pre-P devices, sets the location mode to {@link
   * android.provider.Settings.Secure#LOCATION_MODE_HIGH_ACCURACY} or {@link
   * android.provider.Settings.Secure#LOCATION_MODE_OFF}.
   */
  public void setLocationEnabled(boolean enabled) {
    setLocationEnabledForUser(enabled, Process.myUserHandle());
  }

  private int getLocationMode() {
    return Secure.getInt(getContext().getContentResolver(), LOCATION_MODE, LOCATION_MODE_OFF);
  }

  /**
   * On pre-P devices, sets the device location mode. For P and above, use {@link
   * #setLocationEnabled(boolean)} and {@link #setProviderEnabled(String, boolean)} in combination
   * to achieve the desired effect.
   */
  public void setLocationMode(int locationMode) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.P) {
      throw new AssertionError(
          "Tests may not set location mode directly on P and above. Instead, use"
              + " setLocationEnabled() and setProviderEnabled() in combination to achieve the"
              + " desired result.");
    }

    setLocationModeInternal(locationMode);
  }

  private void setLocationModeInternal(int locationMode) {
    Secure.putInt(getContext().getContentResolver(), LOCATION_MODE, locationMode);
  }

  @Implementation
  @Nullable
  protected Location getLastKnownLocation(String provider) {
    ProviderEntry providerEntry = getProviderEntry(provider);
    if (providerEntry == null) {
      return null;
    }

    return providerEntry.getLastLocation();
  }

  /**
   * @deprecated Use {@link #simulateLocation(Location)} to update the last location for a provider.
   */
  @Deprecated
  public void setLastKnownLocation(String provider, @Nullable Location location) {
    getOrCreateProviderEntry(provider).setLastLocation(location);
  }

  @RequiresApi(api = VERSION_CODES.R)
  @Implementation(minSdk = VERSION_CODES.R)
  protected void getCurrentLocation(
      String provider,
      @Nullable CancellationSignal cancellationSignal,
      Executor executor,
      Consumer<Location> consumer) {
    getCurrentLocationInternal(
        provider, LocationRequest.create(), cancellationSignal, executor, consumer);
  }

  @RequiresApi(api = VERSION_CODES.S)
  @Implementation(minSdk = VERSION_CODES.S)
  protected void getCurrentLocation(
      String provider,
      LocationRequest request,
      @Nullable CancellationSignal cancellationSignal,
      Executor executor,
      Consumer<Location> consumer) {
    getCurrentLocationInternal(provider, request, cancellationSignal, executor, consumer);
  }

  @RequiresApi(api = VERSION_CODES.R)
  private void getCurrentLocationInternal(
      String provider,
      LocationRequest request,
      @Nullable CancellationSignal cancellationSignal,
      Executor executor,
      Consumer<Location> consumer) {
    if (cancellationSignal != null) {
      cancellationSignal.throwIfCanceled();
    }

    final Location location = getLastKnownLocation(provider);
    if (location != null) {
      long locationAgeMs =
          SystemClock.elapsedRealtime() - NANOSECONDS.toMillis(location.getElapsedRealtimeNanos());
      if (locationAgeMs < MAX_CURRENT_LOCATION_AGE_MS) {
        executor.execute(() -> consumer.accept(location));
        return;
      }
    }

    CurrentLocationTransport listener = new CurrentLocationTransport(executor, consumer);
    requestLocationUpdatesInternal(
        provider, new RoboLocationRequest(request), Runnable::run, listener);

    if (cancellationSignal != null) {
      cancellationSignal.setOnCancelListener(listener::cancel);
    }

    listener.startTimeout(GET_CURRENT_LOCATION_TIMEOUT_MS);
  }

  @Implementation
  protected void requestSingleUpdate(
      String provider, LocationListener listener, @Nullable Looper looper) {
    if (looper == null) {
      looper = Looper.myLooper();
      if (looper == null) {
        // forces appropriate exception
        new Handler();
      }
    }
    requestLocationUpdatesInternal(
        provider,
        new RoboLocationRequest(provider, 0, 0, true),
        new HandlerExecutor(new Handler(looper)),
        listener);
  }

  @Implementation
  protected void requestSingleUpdate(
      Criteria criteria, LocationListener listener, @Nullable Looper looper) {
    String bestProvider = getBestProvider(criteria, true);
    if (bestProvider == null) {
      throw new IllegalArgumentException("no providers found for criteria");
    }
    if (looper == null) {
      looper = Looper.myLooper();
      if (looper == null) {
        // forces appropriate exception
        new Handler();
      }
    }
    requestLocationUpdatesInternal(
        bestProvider,
        new RoboLocationRequest(bestProvider, 0, 0, true),
        new HandlerExecutor(new Handler(looper)),
        listener);
  }

  @Implementation
  protected void requestSingleUpdate(String provider, PendingIntent pendingIntent) {
    requestLocationUpdatesInternal(
        provider, new RoboLocationRequest(provider, 0, 0, true), pendingIntent);
  }

  @Implementation
  protected void requestSingleUpdate(Criteria criteria, PendingIntent pendingIntent) {
    String bestProvider = getBestProvider(criteria, true);
    if (bestProvider == null) {
      throw new IllegalArgumentException("no providers found for criteria");
    }
    requestLocationUpdatesInternal(
        bestProvider, new RoboLocationRequest(bestProvider, 0, 0, true), pendingIntent);
  }

  @Implementation
  protected void requestLocationUpdates(
      String provider, long minTime, float minDistance, LocationListener listener) {
    requestLocationUpdatesInternal(
        provider,
        new RoboLocationRequest(provider, minTime, minDistance, false),
        new HandlerExecutor(new Handler()),
        listener);
  }

  @Implementation
  protected void requestLocationUpdates(
      String provider,
      long minTime,
      float minDistance,
      LocationListener listener,
      @Nullable Looper looper) {
    if (looper == null) {
      looper = Looper.myLooper();
      if (looper == null) {
        // forces appropriate exception
        new Handler();
      }
    }
    requestLocationUpdatesInternal(
        provider,
        new RoboLocationRequest(provider, minTime, minDistance, false),
        new HandlerExecutor(new Handler(looper)),
        listener);
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected void requestLocationUpdates(
      String provider,
      long minTime,
      float minDistance,
      Executor executor,
      LocationListener listener) {
    requestLocationUpdatesInternal(
        provider,
        new RoboLocationRequest(provider, minTime, minDistance, false),
        executor,
        listener);
  }

  @Implementation
  protected void requestLocationUpdates(
      long minTime,
      float minDistance,
      Criteria criteria,
      LocationListener listener,
      @Nullable Looper looper) {
    String bestProvider = getBestProvider(criteria, true);
    if (bestProvider == null) {
      throw new IllegalArgumentException("no providers found for criteria");
    }
    if (looper == null) {
      looper = Looper.myLooper();
      if (looper == null) {
        // forces appropriate exception
        new Handler();
      }
    }
    requestLocationUpdatesInternal(
        bestProvider,
        new RoboLocationRequest(bestProvider, minTime, minDistance, false),
        new HandlerExecutor(new Handler(looper)),
        listener);
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected void requestLocationUpdates(
      long minTime,
      float minDistance,
      Criteria criteria,
      Executor executor,
      LocationListener listener) {
    String bestProvider = getBestProvider(criteria, true);
    if (bestProvider == null) {
      throw new IllegalArgumentException("no providers found for criteria");
    }
    requestLocationUpdatesInternal(
        bestProvider,
        new RoboLocationRequest(bestProvider, minTime, minDistance, false),
        executor,
        listener);
  }

  @Implementation
  protected void requestLocationUpdates(
      String provider, long minTime, float minDistance, PendingIntent pendingIntent) {
    requestLocationUpdatesInternal(
        provider, new RoboLocationRequest(provider, minTime, minDistance, false), pendingIntent);
  }

  @Implementation
  protected void requestLocationUpdates(
      long minTime, float minDistance, Criteria criteria, PendingIntent pendingIntent) {
    String bestProvider = getBestProvider(criteria, true);
    if (bestProvider == null) {
      throw new IllegalArgumentException("no providers found for criteria");
    }
    requestLocationUpdatesInternal(
        bestProvider,
        new RoboLocationRequest(bestProvider, minTime, minDistance, false),
        pendingIntent);
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected void requestLocationUpdates(
      @Nullable LocationRequest request, Executor executor, LocationListener listener) {
    if (request == null) {
      request = LocationRequest.create();
    }
    requestLocationUpdatesInternal(
        request.getProvider(), new RoboLocationRequest(request), executor, listener);
  }

  @Implementation(minSdk = VERSION_CODES.KITKAT)
  protected void requestLocationUpdates(
      @Nullable LocationRequest request, LocationListener listener, Looper looper) {
    if (request == null) {
      request = LocationRequest.create();
    }
    if (looper == null) {
      looper = Looper.myLooper();
      if (looper == null) {
        // forces appropriate exception
        new Handler();
      }
    }
    requestLocationUpdatesInternal(
        request.getProvider(),
        new RoboLocationRequest(request),
        new HandlerExecutor(new Handler(looper)),
        listener);
  }

  @Implementation(minSdk = VERSION_CODES.KITKAT)
  protected void requestLocationUpdates(
      @Nullable LocationRequest request, PendingIntent pendingIntent) {
    if (request == null) {
      request = LocationRequest.create();
    }
    requestLocationUpdatesInternal(
        request.getProvider(), new RoboLocationRequest(request), pendingIntent);
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected void requestLocationUpdates(
      String provider, LocationRequest request, Executor executor, LocationListener listener) {
    requestLocationUpdatesInternal(provider, new RoboLocationRequest(request), executor, listener);
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected void requestLocationUpdates(
      String provider, LocationRequest request, PendingIntent pendingIntent) {
    requestLocationUpdatesInternal(provider, new RoboLocationRequest(request), pendingIntent);
  }

  private void requestLocationUpdatesInternal(
      String provider, RoboLocationRequest request, Executor executor, LocationListener listener) {
    if (provider == null || request == null || executor == null || listener == null) {
      throw new IllegalArgumentException();
    }
    getOrCreateProviderEntry(provider).addListener(listener, request, executor);
  }

  private void requestLocationUpdatesInternal(
      String provider, RoboLocationRequest request, PendingIntent pendingIntent) {
    if (provider == null || request == null || pendingIntent == null) {
      throw new IllegalArgumentException();
    }
    getOrCreateProviderEntry(provider).addListener(pendingIntent, request);
  }

  @Implementation
  protected void removeUpdates(LocationListener listener) {
    removeUpdatesInternal(listener);
  }

  @Implementation
  protected void removeUpdates(PendingIntent pendingIntent) {
    removeUpdatesInternal(pendingIntent);
  }

  private void removeUpdatesInternal(Object key) {
    for (ProviderEntry providerEntry : getProviderEntries()) {
      providerEntry.removeListener(key);
    }
  }

  /**
   * Returns the list of {@link LocationRequest} currently registered under the given provider.
   * Clients compiled against the public Android SDK should only use this method on S+, clients
   * compiled against the system Android SDK may only use this method on Kitkat+.
   *
   * <p>Prior to Android S {@link LocationRequest} equality is not well defined, so prefer using
   * {@link #getLegacyLocationRequests(String)} instead if equality is required for testing.
   */
  @RequiresApi(VERSION_CODES.KITKAT)
  public List<LocationRequest> getLocationRequests(String provider) {
    ProviderEntry providerEntry = getProviderEntry(provider);
    if (providerEntry == null) {
      return ImmutableList.of();
    }

    return ImmutableList.copyOf(
        Iterables.transform(
            providerEntry.getTransports(),
            transport -> transport.getRequest().getLocationRequest()));
  }

  /**
   * Returns the list of {@link RoboLocationRequest} currently registered under the given provider.
   * Since {@link LocationRequest} was not publicly visible prior to S, and did not exist prior to
   * Kitkat, {@link RoboLocationRequest} allows querying the location requests prior to those
   * platforms, and also implements proper equality comparisons for testing.
   */
  public List<RoboLocationRequest> getLegacyLocationRequests(String provider) {
    ProviderEntry providerEntry = getProviderEntry(provider);
    if (providerEntry == null) {
      return ImmutableList.of();
    }

    return ImmutableList.copyOf(
        Iterables.transform(providerEntry.getTransports(), LocationTransport::getRequest));
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected boolean injectLocation(Location location) {
    return false;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @Nullable
  protected String getGnssHardwareModelName() {
    return gnssHardwareModelName;
  }

  /**
   * Sets the GNSS hardware model name returned by {@link
   * LocationManager#getGnssHardwareModelName()}.
   */
  public void setGnssHardwareModelName(@Nullable String gnssHardwareModelName) {
    this.gnssHardwareModelName = gnssHardwareModelName;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected int getGnssYearOfHardware() {
    return gnssYearOfHardware;
  }

  /** Sets the GNSS year of hardware returned by {@link LocationManager#getGnssYearOfHardware()}. */
  public void setGnssYearOfHardware(int gnssYearOfHardware) {
    this.gnssYearOfHardware = gnssYearOfHardware;
  }

  @Implementation
  protected boolean addGpsStatusListener(GpsStatus.Listener listener) {
    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.R) {
      throw new UnsupportedOperationException(
          "GpsStatus APIs not supported, please use GnssStatus APIs instead");
    }

    synchronized (gpsStatusListeners) {
      gpsStatusListeners.add(listener);
    }

    return true;
  }

  @Implementation
  protected void removeGpsStatusListener(GpsStatus.Listener listener) {
    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.R) {
      throw new UnsupportedOperationException(
          "GpsStatus APIs not supported, please use GnssStatus APIs instead");
    }

    synchronized (gpsStatusListeners) {
      gpsStatusListeners.remove(listener);
    }
  }

  /** Returns the list of currently registered {@link GpsStatus.Listener}s. */
  public List<GpsStatus.Listener> getGpsStatusListeners() {
    synchronized (gpsStatusListeners) {
      return new ArrayList<>(gpsStatusListeners);
    }
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected boolean registerGnssStatusCallback(GnssStatus.Callback callback, Handler handler) {
    if (handler == null) {
      handler = new Handler();
    }

    return registerGnssStatusCallback(new HandlerExecutor(handler), callback);
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected boolean registerGnssStatusCallback(Executor executor, GnssStatus.Callback listener) {
    synchronized (gnssStatusTransports) {
      Iterables.removeIf(gnssStatusTransports, transport -> transport.getListener() == listener);
      gnssStatusTransports.add(new GnssStatusCallbackTransport(executor, listener));
    }

    return true;
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void unregisterGnssStatusCallback(GnssStatus.Callback listener) {
    synchronized (gnssStatusTransports) {
      Iterables.removeIf(gnssStatusTransports, transport -> transport.getListener() == listener);
    }
  }

  /** Simulates a GNSS status started event. */
  @RequiresApi(VERSION_CODES.N)
  public void simulateGnssStatusStarted() {
    List<GnssStatusCallbackTransport> transports;
    synchronized (gnssStatusTransports) {
      transports = gnssStatusTransports;
    }

    for (GnssStatusCallbackTransport transport : transports) {
      transport.onStarted();
    }
  }

  /** Simulates a GNSS status first fix event. */
  @RequiresApi(VERSION_CODES.N)
  public void simulateGnssStatusFirstFix(int ttff) {
    List<GnssStatusCallbackTransport> transports;
    synchronized (gnssStatusTransports) {
      transports = gnssStatusTransports;
    }

    for (GnssStatusCallbackTransport transport : transports) {
      transport.onFirstFix(ttff);
    }
  }

  /** Simulates a GNSS status event. */
  @RequiresApi(VERSION_CODES.N)
  public void simulateGnssStatus(GnssStatus status) {
    List<GnssStatusCallbackTransport> transports;
    synchronized (gnssStatusTransports) {
      transports = gnssStatusTransports;
    }

    for (GnssStatusCallbackTransport transport : transports) {
      transport.onSatelliteStatusChanged(status);
    }
  }

  /**
   * @deprecated Use {@link #simulateGnssStatus(GnssStatus)} instead.
   */
  @Deprecated
  @RequiresApi(VERSION_CODES.N)
  public void sendGnssStatus(GnssStatus status) {
    simulateGnssStatus(status);
  }

  /** Simulates a GNSS status stopped event. */
  @RequiresApi(VERSION_CODES.N)
  public void simulateGnssStatusStopped() {
    List<GnssStatusCallbackTransport> transports;
    synchronized (gnssStatusTransports) {
      transports = gnssStatusTransports;
    }

    for (GnssStatusCallbackTransport transport : transports) {
      transport.onStopped();
    }
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected boolean addNmeaListener(OnNmeaMessageListener listener, Handler handler) {
    if (handler == null) {
      handler = new Handler();
    }

    return addNmeaListener(new HandlerExecutor(handler), listener);
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected boolean addNmeaListener(Executor executor, OnNmeaMessageListener listener) {
    synchronized (nmeaMessageTransports) {
      Iterables.removeIf(nmeaMessageTransports, transport -> transport.getListener() == listener);
      nmeaMessageTransports.add(new OnNmeaMessageListenerTransport(executor, listener));
    }

    return true;
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void removeNmeaListener(OnNmeaMessageListener listener) {
    synchronized (nmeaMessageTransports) {
      Iterables.removeIf(nmeaMessageTransports, transport -> transport.getListener() == listener);
    }
  }

  /** Simulates a NMEA message. */
  @RequiresApi(api = VERSION_CODES.N)
  public void simulateNmeaMessage(String message, long timestamp) {
    List<OnNmeaMessageListenerTransport> transports;
    synchronized (nmeaMessageTransports) {
      transports = nmeaMessageTransports;
    }

    for (OnNmeaMessageListenerTransport transport : transports) {
      transport.onNmeaMessage(message, timestamp);
    }
  }

  /**
   * @deprecated Use {@link #simulateNmeaMessage(String, long)} instead.
   */
  @Deprecated
  @RequiresApi(api = VERSION_CODES.N)
  public void sendNmeaMessage(String message, long timestamp) {
    simulateNmeaMessage(message, timestamp);
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected boolean registerGnssMeasurementsCallback(
      GnssMeasurementsEvent.Callback listener, Handler handler) {
    if (handler == null) {
      handler = new Handler();
    }

    return registerGnssMeasurementsCallback(new HandlerExecutor(handler), listener);
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected boolean registerGnssMeasurementsCallback(
      Executor executor, GnssMeasurementsEvent.Callback listener) {
    synchronized (gnssMeasurementTransports) {
      Iterables.removeIf(
          gnssMeasurementTransports, transport -> transport.getListener() == listener);
      gnssMeasurementTransports.add(new GnssMeasurementsEventCallbackTransport(executor, listener));
    }

    return true;
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void unregisterGnssMeasurementsCallback(GnssMeasurementsEvent.Callback listener) {
    synchronized (gnssMeasurementTransports) {
      Iterables.removeIf(
          gnssMeasurementTransports, transport -> transport.getListener() == listener);
    }
  }

  /** Simulates a GNSS measurements event. */
  @RequiresApi(api = VERSION_CODES.N)
  public void simulateGnssMeasurementsEvent(GnssMeasurementsEvent event) {
    List<GnssMeasurementsEventCallbackTransport> transports;
    synchronized (gnssMeasurementTransports) {
      transports = gnssMeasurementTransports;
    }

    for (GnssMeasurementsEventCallbackTransport transport : transports) {
      transport.onGnssMeasurementsReceived(event);
    }
  }

  /**
   * @deprecated Use {@link #simulateGnssMeasurementsEvent(GnssMeasurementsEvent)} instead.
   */
  @Deprecated
  @RequiresApi(api = VERSION_CODES.N)
  public void sendGnssMeasurementsEvent(GnssMeasurementsEvent event) {
    simulateGnssMeasurementsEvent(event);
  }

  /** Simulates a GNSS measurements status change. */
  @RequiresApi(api = VERSION_CODES.N)
  public void simulateGnssMeasurementsStatus(int status) {
    List<GnssMeasurementsEventCallbackTransport> transports;
    synchronized (gnssMeasurementTransports) {
      transports = gnssMeasurementTransports;
    }

    for (GnssMeasurementsEventCallbackTransport transport : transports) {
      transport.onStatusChanged(status);
    }
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected Object registerAntennaInfoListener(Object executor, Object listener) {
    synchronized (gnssAntennaInfoTransports) {
      Iterables.removeIf(
          gnssAntennaInfoTransports, transport -> transport.getListener() == listener);
      gnssAntennaInfoTransports.add(
          new GnssAntennaInfoListenerTransport(
              (Executor) executor, (GnssAntennaInfo.Listener) listener));
    }
    return true;
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected void unregisterAntennaInfoListener(Object listener) {
    synchronized (gnssAntennaInfoTransports) {
      Iterables.removeIf(
          gnssAntennaInfoTransports, transport -> transport.getListener() == listener);
    }
  }

  /** Simulates a GNSS antenna info event. */
  @RequiresApi(api = VERSION_CODES.R)
  public void simulateGnssAntennaInfo(List<GnssAntennaInfo> antennaInfos) {
    List<GnssAntennaInfoListenerTransport> transports;
    synchronized (gnssAntennaInfoTransports) {
      transports = gnssAntennaInfoTransports;
    }

    for (GnssAntennaInfoListenerTransport transport : transports) {
      transport.onGnssAntennaInfoReceived(new ArrayList<>(antennaInfos));
    }
  }

  /**
   * @deprecated Use {@link #simulateGnssAntennaInfo(List)} instead.
   */
  @Deprecated
  @RequiresApi(api = VERSION_CODES.R)
  public void sendGnssAntennaInfo(List<GnssAntennaInfo> antennaInfos) {
    simulateGnssAntennaInfo(antennaInfos);
  }

  /**
   * A convenience function equivalent to invoking {@link #simulateLocation(String, Location)} with
   * the provider of the given location.
   */
  public void simulateLocation(Location location) {
    simulateLocation(location.getProvider(), location);
  }

  /**
   * Delivers to the given provider (which will be created if necessary) a new location which will
   * be delivered to appropriate listeners and updates state accordingly. Delivery will ignore the
   * enabled/disabled state of providers, unlike location on a real device.
   *
   * <p>The location will also be delivered to the passive provider.
   */
  public void simulateLocation(String provider, Location location) {
    ProviderEntry providerEntry = getOrCreateProviderEntry(provider);
    if (!PASSIVE_PROVIDER.equals(providerEntry.getName())) {
      providerEntry.simulateLocation(location);
    }

    ProviderEntry passiveProviderEntry = getProviderEntry(PASSIVE_PROVIDER);
    if (passiveProviderEntry != null) {
      passiveProviderEntry.simulateLocation(location);
    }
  }

  /**
   * @deprecated Do not test listeners, instead use {@link #simulateLocation(Location)} and test the
   *     results of those listeners being invoked.
   */
  @Deprecated
  public List<LocationListener> getRequestLocationUpdateListeners() {
    return getLocationUpdateListeners();
  }

  /**
   * @deprecated Do not test listeners, instead use {@link #simulateLocation(Location)} and test the
   *     results of those listeners being invoked.
   */
  @Deprecated
  public List<LocationListener> getLocationUpdateListeners() {
    HashSet<LocationListener> listeners = new HashSet<>();
    for (ProviderEntry providerEntry : getProviderEntries()) {
      Iterables.addAll(
          listeners,
          Iterables.transform(
              Iterables.filter(providerEntry.getTransports(), LocationListenerTransport.class),
              LocationTransport::getKey));
    }
    return new ArrayList<>(listeners);
  }

  /**
   * @deprecated Do not test listeners, instead use {@link #simulateLocation(Location)} and test the
   *     results of those listeners being invoked.
   */
  @Deprecated
  public List<LocationListener> getLocationUpdateListeners(String provider) {
    ProviderEntry providerEntry = getProviderEntry(provider);
    if (providerEntry == null) {
      return Collections.emptyList();
    }

    HashSet<LocationListener> listeners = new HashSet<>();
    Iterables.addAll(
        listeners,
        Iterables.transform(
            Iterables.filter(providerEntry.getTransports(), LocationListenerTransport.class),
            LocationTransport::getKey));
    return new ArrayList<>(listeners);
  }

  /**
   * @deprecated Do not test pending intents, instead use {@link #simulateLocation(Location)} and
   *     test the results of those pending intent being invoked.
   */
  @Deprecated
  public List<PendingIntent> getLocationUpdatePendingIntents() {
    HashSet<PendingIntent> listeners = new HashSet<>();
    for (ProviderEntry providerEntry : getProviderEntries()) {
      Iterables.addAll(
          listeners,
          Iterables.transform(
              Iterables.filter(providerEntry.getTransports(), LocationPendingIntentTransport.class),
              LocationTransport::getKey));
    }
    return new ArrayList<>(listeners);
  }

  /**
   * Retrieves a list of all currently registered pending intents for the given provider.
   *
   * @deprecated Do not test pending intents, instead use {@link #simulateLocation(Location)} and
   *     test the results of those pending intent being invoked.
   */
  @Deprecated
  public List<PendingIntent> getLocationUpdatePendingIntents(String provider) {
    ProviderEntry providerEntry = getProviderEntry(provider);
    if (providerEntry == null) {
      return Collections.emptyList();
    }

    HashSet<PendingIntent> listeners = new HashSet<>();
    Iterables.addAll(
        listeners,
        Iterables.transform(
            Iterables.filter(providerEntry.getTransports(), LocationPendingIntentTransport.class),
            LocationTransport::getKey));
    return new ArrayList<>(listeners);
  }

  private Context getContext() {
    return ReflectionHelpers.getField(realLocationManager, "mContext");
  }

  private ProviderEntry getOrCreateProviderEntry(String name) {
    if (name == null) {
      throw new IllegalArgumentException("cannot use a null provider");
    }

    synchronized (providers) {
      ProviderEntry providerEntry = getProviderEntry(name);
      if (providerEntry == null) {
        providerEntry = new ProviderEntry(name, null);
        providers.add(providerEntry);
      }
      return providerEntry;
    }
  }

  @Nullable
  private ProviderEntry getProviderEntry(String name) {
    if (name == null) {
      return null;
    }

    synchronized (providers) {
      for (ProviderEntry providerEntry : providers) {
        if (name.equals(providerEntry.getName())) {
          return providerEntry;
        }
      }
    }

    return null;
  }

  private Set<ProviderEntry> getProviderEntries() {
    synchronized (providers) {
      return providers;
    }
  }

  private void removeProviderEntry(String name) {
    synchronized (providers) {
      providers.remove(getProviderEntry(name));
    }
  }

  // provider enabled logic is complicated due to many changes over different versions of android. a
  // brief explanation of how the logic works in this shadow (which is subtly different and more
  // complicated from how the logic works in real android):
  //
  // 1) prior to P, the source of truth for whether a provider is enabled must be the
  //    LOCATION_PROVIDERS_ALLOWED setting, so that direct writes into that setting are respected.
  //    changes to the network and gps providers must change LOCATION_MODE appropriately as well.
  // 2) for P, providers are considered enabled if the LOCATION_MODE setting is not off AND they are
  //    enabled via LOCATION_PROVIDERS_ALLOWED. direct writes into LOCATION_PROVIDERS_ALLOWED should
  //    be respected (if the LOCATION_MODE is not off). changes to LOCATION_MODE will change the
  //    state of the network and gps providers.
  // 3) for Q/R, providers are considered enabled if the LOCATION_MODE settings is not off AND they
  //    are enabled, but the store for the enabled state may not be LOCATION_PROVIDERS_ALLOWED, as
  //    writes into LOCATION_PROVIDERS_ALLOWED should not be respected. LOCATION_PROVIDERS_ALLOWED
  //    should still be updated so that provider state changes can be listened to via that setting.
  //    changes to LOCATION_MODE should not change the state of the network and gps provider.
  // 5) the passive provider is always special-cased at all API levels - it's state is controlled
  //    programmatically, and should never be determined by LOCATION_PROVIDERS_ALLOWED.
  private final class ProviderEntry {

    private final String name;

    @GuardedBy("this")
    private final CopyOnWriteArrayList<LocationTransport<?>> locationTransports =
        new CopyOnWriteArrayList<>();

    @GuardedBy("this")
    @Nullable
    private ProviderProperties properties;

    @GuardedBy("this")
    private boolean enabled;

    @GuardedBy("this")
    @Nullable
    private Location lastLocation;

    ProviderEntry(String name, @Nullable ProviderProperties properties) {
      this.name = name;

      this.properties = properties;

      switch (name) {
        case PASSIVE_PROVIDER:
          // passive provider always starts enabled
          enabled = true;
          break;
        case GPS_PROVIDER:
          enabled = ShadowSecure.INITIAL_GPS_PROVIDER_STATE;
          break;
        case NETWORK_PROVIDER:
          enabled = ShadowSecure.INITIAL_NETWORK_PROVIDER_STATE;
          break;
        default:
          enabled = false;
          break;
      }
    }

    public String getName() {
      return name;
    }

    public synchronized List<LocationTransport<?>> getTransports() {
      return locationTransports;
    }

    @Nullable
    public synchronized ProviderProperties getProperties() {
      return properties;
    }

    public synchronized void setProperties(@Nullable ProviderProperties properties) {
      this.properties = properties;
    }

    public boolean isEnabled() {
      if (PASSIVE_PROVIDER.equals(name) || RuntimeEnvironment.getApiLevel() >= VERSION_CODES.Q) {
        synchronized (this) {
          return enabled;
        }
      } else {
        String allowedProviders =
            Secure.getString(getContext().getContentResolver(), LOCATION_PROVIDERS_ALLOWED);
        if (TextUtils.isEmpty(allowedProviders)) {
          return false;
        } else {
          return Arrays.asList(allowedProviders.split(",")).contains(name);
        }
      }
    }

    public void setEnabled(boolean enabled) {
      List<LocationTransport<?>> transports;
      synchronized (this) {
        if (PASSIVE_PROVIDER.equals(name)) {
          // the passive provider cannot be disabled, but the passive provider didn't exist in
          // previous versions of this shadow. for backwards compatibility, we let the passive
          // provider be disabled. this also help emulate the situation where an app only has COARSE
          // permissions, which this shadow normally can't emulate.
          this.enabled = enabled;
          return;
        }

        int oldLocationMode = getLocationMode();
        int newLocationMode = oldLocationMode;
        if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.P) {
          if (GPS_PROVIDER.equals(name)) {
            if (enabled) {
              switch (oldLocationMode) {
                case LOCATION_MODE_OFF:
                  newLocationMode = LOCATION_MODE_SENSORS_ONLY;
                  break;
                case LOCATION_MODE_BATTERY_SAVING:
                  newLocationMode = LOCATION_MODE_HIGH_ACCURACY;
                  break;
                default:
                  break;
              }
            } else {
              switch (oldLocationMode) {
                case LOCATION_MODE_SENSORS_ONLY:
                  newLocationMode = LOCATION_MODE_OFF;
                  break;
                case LOCATION_MODE_HIGH_ACCURACY:
                  newLocationMode = LOCATION_MODE_BATTERY_SAVING;
                  break;
                default:
                  break;
              }
            }
          } else if (NETWORK_PROVIDER.equals(name)) {
            if (enabled) {
              switch (oldLocationMode) {
                case LOCATION_MODE_OFF:
                  newLocationMode = LOCATION_MODE_BATTERY_SAVING;
                  break;
                case LOCATION_MODE_SENSORS_ONLY:
                  newLocationMode = LOCATION_MODE_HIGH_ACCURACY;
                  break;
                default:
                  break;
              }
            } else {
              switch (oldLocationMode) {
                case LOCATION_MODE_BATTERY_SAVING:
                  newLocationMode = LOCATION_MODE_OFF;
                  break;
                case LOCATION_MODE_HIGH_ACCURACY:
                  newLocationMode = LOCATION_MODE_SENSORS_ONLY;
                  break;
                default:
                  break;
              }
            }
          }
        }

        if (newLocationMode != oldLocationMode) {
          // this sets LOCATION_MODE and LOCATION_PROVIDERS_ALLOWED
          setLocationModeInternal(newLocationMode);
        } else if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.Q) {
          if (enabled == this.enabled) {
            return;
          }

          this.enabled = enabled;
          // set LOCATION_PROVIDERS_ALLOWED directly, without setting LOCATION_MODE. do this even
          // though LOCATION_PROVIDERS_ALLOWED is not the source of truth - we keep it up to date,
          // but ignore any direct writes to it
          ShadowSettings.ShadowSecure.updateEnabledProviders(
              getContext().getContentResolver(), name, enabled);
        } else {
          if (enabled == this.enabled) {
            return;
          }

          this.enabled = enabled;
          // set LOCATION_PROVIDERS_ALLOWED directly, without setting LOCATION_MODE
          ShadowSettings.ShadowSecure.updateEnabledProviders(
              getContext().getContentResolver(), name, enabled);
        }

        transports = locationTransports;
      }

      for (LocationTransport<?> transport : transports) {
        if (!transport.invokeOnProviderEnabled(name, enabled)) {
          synchronized (this) {
            Iterables.removeIf(locationTransports, current -> current == transport);
          }
        }
      }
    }

    @Nullable
    public synchronized Location getLastLocation() {
      return lastLocation;
    }

    public synchronized void setLastLocation(@Nullable Location location) {
      lastLocation = location;
    }

    public void simulateLocation(Location location) {
      List<LocationTransport<?>> transports;
      synchronized (this) {
        lastLocation = new Location(location);
        transports = locationTransports;
      }

      for (LocationTransport<?> transport : transports) {
        if (!transport.invokeOnLocation(location)) {
          synchronized (this) {
            Iterables.removeIf(locationTransports, current -> current == transport);
          }
        }
      }
    }

    public synchronized boolean meetsCriteria(Criteria criteria) {
      if (PASSIVE_PROVIDER.equals(name)) {
        return false;
      }

      if (properties == null) {
        return false;
      }
      return properties.meetsCriteria(criteria);
    }

    public void addListener(
        LocationListener listener, RoboLocationRequest request, Executor executor) {
      addListenerInternal(new LocationListenerTransport(listener, request, executor));
    }

    public void addListener(PendingIntent pendingIntent, RoboLocationRequest request) {
      addListenerInternal(new LocationPendingIntentTransport(getContext(), pendingIntent, request));
    }

    private void addListenerInternal(LocationTransport<?> transport) {
      boolean invokeOnProviderEnabled;
      synchronized (this) {
        Iterables.removeIf(locationTransports, current -> current.getKey() == transport.getKey());
        locationTransports.add(transport);
        invokeOnProviderEnabled = !enabled;
      }

      if (invokeOnProviderEnabled) {
        if (!transport.invokeOnProviderEnabled(name, false)) {
          synchronized (this) {
            Iterables.removeIf(locationTransports, current -> current == transport);
          }
        }
      }
    }

    public synchronized void removeListener(Object key) {
      Iterables.removeIf(locationTransports, transport -> transport.getKey() == key);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ProviderEntry) {
        ProviderEntry that = (ProviderEntry) o;
        return Objects.equals(name, that.name);
      }

      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name);
    }
  }

  /**
   * LocationRequest doesn't exist prior to Kitkat, and is not public prior to S, so a new class is
   * required to represent it prior to those platforms.
   */
  public static final class RoboLocationRequest {
    @Nullable private final Object locationRequest;

    // all these parameters are meaningless if locationRequest is set
    private final long intervalMillis;
    private final float minUpdateDistanceMeters;
    private final boolean singleShot;

    @RequiresApi(VERSION_CODES.KITKAT)
    public RoboLocationRequest(LocationRequest locationRequest) {
      this.locationRequest = Objects.requireNonNull(locationRequest);
      intervalMillis = 0;
      minUpdateDistanceMeters = 0;
      singleShot = false;
    }

    public RoboLocationRequest(
        String provider, long intervalMillis, float minUpdateDistanceMeters, boolean singleShot) {
      if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.KITKAT) {
        locationRequest =
            LocationRequest.createFromDeprecatedProvider(
                provider, intervalMillis, minUpdateDistanceMeters, singleShot);
      } else {
        locationRequest = null;
      }

      this.intervalMillis = intervalMillis;
      this.minUpdateDistanceMeters = minUpdateDistanceMeters;
      this.singleShot = singleShot;
    }

    @RequiresApi(VERSION_CODES.KITKAT)
    public LocationRequest getLocationRequest() {
      return (LocationRequest) Objects.requireNonNull(locationRequest);
    }

    public long getIntervalMillis() {
      if (locationRequest != null) {
        return ((LocationRequest) locationRequest).getInterval();
      } else {
        return intervalMillis;
      }
    }

    public float getMinUpdateDistanceMeters() {
      if (locationRequest != null) {
        return ((LocationRequest) locationRequest).getSmallestDisplacement();
      } else {
        return minUpdateDistanceMeters;
      }
    }

    public boolean isSingleShot() {
      if (locationRequest != null) {
        return ((LocationRequest) locationRequest).getNumUpdates() == 1;
      } else {
        return singleShot;
      }
    }

    long getMinUpdateIntervalMillis() {
      if (locationRequest != null) {
        return ((LocationRequest) locationRequest).getFastestInterval();
      } else {
        return intervalMillis;
      }
    }

    int getMaxUpdates() {
      if (locationRequest != null) {
        return ((LocationRequest) locationRequest).getNumUpdates();
      } else {
        return singleShot ? 1 : Integer.MAX_VALUE;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof RoboLocationRequest) {
        RoboLocationRequest that = (RoboLocationRequest) o;

        // location request equality is not well-defined prior to S
        if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S) {
          return Objects.equals(locationRequest, that.locationRequest);
        } else {
          if (intervalMillis != that.intervalMillis
              || singleShot != that.singleShot
              || Float.compare(that.minUpdateDistanceMeters, minUpdateDistanceMeters) != 0
              || (locationRequest == null) != (that.locationRequest == null)) {
            return false;
          }

          if (locationRequest != null) {
            LocationRequest lr = (LocationRequest) locationRequest;
            LocationRequest thatLr = (LocationRequest) that.locationRequest;

            if (lr.getQuality() != thatLr.getQuality()
                || lr.getInterval() != thatLr.getInterval()
                || lr.getFastestInterval() != thatLr.getFastestInterval()
                || lr.getExpireAt() != thatLr.getExpireAt()
                || lr.getNumUpdates() != thatLr.getNumUpdates()
                || lr.getSmallestDisplacement() != thatLr.getSmallestDisplacement()
                || lr.getHideFromAppOps() != thatLr.getHideFromAppOps()
                || !Objects.equals(lr.getProvider(), thatLr.getProvider())) {
              return false;
            }

            // allow null worksource to match empty worksource
            WorkSource workSource =
                lr.getWorkSource() == null ? new WorkSource() : lr.getWorkSource();
            WorkSource thatWorkSource =
                thatLr.getWorkSource() == null ? new WorkSource() : thatLr.getWorkSource();
            if (!workSource.equals(thatWorkSource)) {
              return false;
            }

            if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.Q) {
              if (lr.isLowPowerMode() != thatLr.isLowPowerMode()
                  || lr.isLocationSettingsIgnored() != thatLr.isLocationSettingsIgnored()) {
                return false;
              }
            }
          }

          return true;
        }
      }

      return false;
    }

    @Override
    public int hashCode() {
      if (locationRequest != null) {
        return locationRequest.hashCode();
      } else {
        return Objects.hash(intervalMillis, singleShot, minUpdateDistanceMeters);
      }
    }

    @Override
    public String toString() {
      if (locationRequest != null) {
        return locationRequest.toString();
      } else {
        return "Request[interval="
            + intervalMillis
            + ", minUpdateDistance="
            + minUpdateDistanceMeters
            + ", singleShot="
            + singleShot
            + "]";
      }
    }
  }

  private abstract static class LocationTransport<KeyT> {

    private final KeyT key;
    private final RoboLocationRequest request;

    private Location lastDeliveredLocation;
    private int numDeliveries;

    LocationTransport(KeyT key, RoboLocationRequest request) {
      if (key == null) {
        throw new IllegalArgumentException();
      }

      this.key = key;
      this.request = request;
    }

    public KeyT getKey() {
      return key;
    }

    public RoboLocationRequest getRequest() {
      return request;
    }

    // return false if this listener should be removed by this invocation
    public boolean invokeOnLocation(Location location) {
      if (lastDeliveredLocation != null) {
        if (location.getTime() - lastDeliveredLocation.getTime()
            < request.getMinUpdateIntervalMillis()) {
          return true;
        }
        if (distanceBetween(location, lastDeliveredLocation)
            < request.getMinUpdateDistanceMeters()) {
          return true;
        }
      }

      lastDeliveredLocation = new Location(location);

      boolean needsRemoval = false;

      if (++numDeliveries >= request.getMaxUpdates()) {
        needsRemoval = true;
      }

      try {
        onLocation(location);
      } catch (CanceledException e) {
        needsRemoval = true;
      }

      return !needsRemoval;
    }

    // return false if this listener should be removed by this invocation
    public boolean invokeOnProviderEnabled(String provider, boolean enabled) {
      try {
        onProviderEnabled(provider, enabled);
        return true;
      } catch (CanceledException e) {
        return false;
      }
    }

    abstract void onLocation(Location location) throws CanceledException;

    abstract void onProviderEnabled(String provider, boolean enabled) throws CanceledException;
  }

  private static final class LocationListenerTransport extends LocationTransport<LocationListener> {

    private final Executor executor;

    LocationListenerTransport(
        LocationListener key, RoboLocationRequest request, Executor executor) {
      super(key, request);
      this.executor = executor;
    }

    @Override
    public void onLocation(Location location) {
      executor.execute(() -> getKey().onLocationChanged(new Location(location)));
    }

    @Override
    public void onProviderEnabled(String provider, boolean enabled) {
      executor.execute(
          () -> {
            if (enabled) {
              getKey().onProviderEnabled(provider);
            } else {
              getKey().onProviderDisabled(provider);
            }
          });
    }
  }

  private static final class LocationPendingIntentTransport
      extends LocationTransport<PendingIntent> {

    private final Context context;

    LocationPendingIntentTransport(
        Context context, PendingIntent key, RoboLocationRequest request) {
      super(key, request);
      this.context = context;
    }

    @Override
    public void onLocation(Location location) throws CanceledException {
      Intent intent = new Intent();
      intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, new Location(location));
      getKey().send(context, 0, intent);
    }

    @Override
    public void onProviderEnabled(String provider, boolean enabled) throws CanceledException {
      Intent intent = new Intent();
      intent.putExtra(LocationManager.KEY_PROVIDER_ENABLED, enabled);
      getKey().send(context, 0, intent);
    }
  }

  /**
   * Returns the distance between the two locations in meters. Adapted from:
   * http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
   */
  static float distanceBetween(Location location1, Location location2) {
    double earthRadius = 3958.75;
    double latDifference = Math.toRadians(location2.getLatitude() - location1.getLatitude());
    double lonDifference = Math.toRadians(location2.getLongitude() - location1.getLongitude());
    double a =
        Math.sin(latDifference / 2) * Math.sin(latDifference / 2)
            + Math.cos(Math.toRadians(location1.getLatitude()))
                * Math.cos(Math.toRadians(location2.getLatitude()))
                * Math.sin(lonDifference / 2)
                * Math.sin(lonDifference / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double dist = Math.abs(earthRadius * c);

    int meterConversion = 1609;

    return (float) (dist * meterConversion);
  }

  @Resetter
  public static synchronized void reset() {
    locationProviderConstructor = null;
  }

  @RequiresApi(api = VERSION_CODES.N)
  private final class CurrentLocationTransport implements LocationListener {

    private final Executor executor;
    private final Consumer<Location> consumer;
    private final Handler timeoutHandler;

    @GuardedBy("this")
    private boolean triggered;

    @Nullable Runnable timeoutRunnable;

    CurrentLocationTransport(Executor executor, Consumer<Location> consumer) {
      this.executor = executor;
      this.consumer = consumer;
      timeoutHandler = new Handler(Looper.getMainLooper());
    }

    public void cancel() {
      synchronized (this) {
        if (triggered) {
          return;
        }
        triggered = true;
      }

      cleanup();
    }

    public void startTimeout(long timeoutMs) {
      synchronized (this) {
        if (triggered) {
          return;
        }

        timeoutRunnable =
            () -> {
              timeoutRunnable = null;
              onLocationChanged((Location) null);
            };
        timeoutHandler.postDelayed(timeoutRunnable, timeoutMs);
      }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {
      onLocationChanged((Location) null);
    }

    @Override
    public void onLocationChanged(@Nullable Location location) {
      synchronized (this) {
        if (triggered) {
          return;
        }
        triggered = true;
      }

      executor.execute(() -> consumer.accept(location));

      cleanup();
    }

    private void cleanup() {
      removeUpdates(this);
      if (timeoutRunnable != null) {
        timeoutHandler.removeCallbacks(timeoutRunnable);
        timeoutRunnable = null;
      }
    }
  }

  private static final class GnssStatusCallbackTransport {

    private final Executor executor;
    private final GnssStatus.Callback listener;

    GnssStatusCallbackTransport(Executor executor, GnssStatus.Callback listener) {
      this.executor = Objects.requireNonNull(executor);
      this.listener = Objects.requireNonNull(listener);
    }

    GnssStatus.Callback getListener() {
      return listener;
    }

    @RequiresApi(api = VERSION_CODES.N)
    public void onStarted() {
      executor.execute(listener::onStarted);
    }

    @RequiresApi(api = VERSION_CODES.N)
    public void onFirstFix(int ttff) {
      executor.execute(() -> listener.onFirstFix(ttff));
    }

    @RequiresApi(api = VERSION_CODES.N)
    public void onSatelliteStatusChanged(GnssStatus status) {
      executor.execute(() -> listener.onSatelliteStatusChanged(status));
    }

    @RequiresApi(api = VERSION_CODES.N)
    public void onStopped() {
      executor.execute(listener::onStopped);
    }
  }

  private static final class OnNmeaMessageListenerTransport {

    private final Executor executor;
    private final OnNmeaMessageListener listener;

    OnNmeaMessageListenerTransport(Executor executor, OnNmeaMessageListener listener) {
      this.executor = Objects.requireNonNull(executor);
      this.listener = Objects.requireNonNull(listener);
    }

    OnNmeaMessageListener getListener() {
      return listener;
    }

    @RequiresApi(api = VERSION_CODES.N)
    public void onNmeaMessage(String message, long timestamp) {
      executor.execute(() -> listener.onNmeaMessage(message, timestamp));
    }
  }

  private static final class GnssMeasurementsEventCallbackTransport {

    private final Executor executor;
    private final GnssMeasurementsEvent.Callback listener;

    GnssMeasurementsEventCallbackTransport(
        Executor executor, GnssMeasurementsEvent.Callback listener) {
      this.executor = Objects.requireNonNull(executor);
      this.listener = Objects.requireNonNull(listener);
    }

    GnssMeasurementsEvent.Callback getListener() {
      return listener;
    }

    @RequiresApi(api = VERSION_CODES.N)
    public void onStatusChanged(int status) {
      executor.execute(() -> listener.onStatusChanged(status));
    }

    @RequiresApi(api = VERSION_CODES.N)
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
      executor.execute(() -> listener.onGnssMeasurementsReceived(event));
    }
  }

  private static final class GnssAntennaInfoListenerTransport {

    private final Executor executor;
    private final GnssAntennaInfo.Listener listener;

    GnssAntennaInfoListenerTransport(Executor executor, GnssAntennaInfo.Listener listener) {
      this.executor = Objects.requireNonNull(executor);
      this.listener = Objects.requireNonNull(listener);
    }

    GnssAntennaInfo.Listener getListener() {
      return listener;
    }

    @RequiresApi(api = VERSION_CODES.R)
    public void onGnssAntennaInfoReceived(List<GnssAntennaInfo> antennaInfos) {
      executor.execute(() -> listener.onGnssAntennaInfoReceived(antennaInfos));
    }
  }

  private static final class HandlerExecutor implements Executor {
    private final Handler handler;

    HandlerExecutor(Handler handler) {
      this.handler = Objects.requireNonNull(handler);
    }

    @Override
    public void execute(Runnable command) {
      if (!handler.post(command)) {
        throw new RejectedExecutionException(handler + " is shutting down");
      }
    }
  }
}
