package org.robolectric.shadows;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
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
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import com.google.common.base.Preconditions;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;
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

  /** Properties of a provider. */
  public static class ProviderProperties {
    private final boolean requiresNetwork;
    private final boolean requiresSatellite;
    private final boolean requiresCell;
    private final boolean hasMonetaryCost;
    private final boolean supportsAltitude;
    private final boolean supportsSpeed;
    private final boolean supportsBearing;
    private final int powerRequirement;
    private final int accuracy;

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
      this.requiresNetwork = false;
      this.requiresSatellite = false;
      this.requiresCell = false;
      this.hasMonetaryCost = criteria.isCostAllowed();
      this.supportsAltitude = criteria.isAltitudeRequired();
      this.supportsSpeed = criteria.isSpeedRequired();
      this.supportsBearing = criteria.isBearingRequired();
      this.powerRequirement = criteria.getPowerRequirement();
      this.accuracy = criteria.getAccuracy();
    }

    private boolean meetsCriteria(Criteria criteria) {
      if (criteria.getAccuracy() != Criteria.NO_REQUIREMENT && criteria.getAccuracy() < accuracy) {
        return false;
      }
      if (criteria.getPowerRequirement() != Criteria.NO_REQUIREMENT
          && criteria.getPowerRequirement() < powerRequirement) {
        return false;
      }
      if (criteria.isAltitudeRequired() && !supportsAltitude) {
        return false;
      }
      if (criteria.isSpeedRequired() && !supportsSpeed) {
        return false;
      }
      if (criteria.isBearingRequired() && !supportsBearing) {
        return false;
      }
      if (!criteria.isCostAllowed() && hasMonetaryCost) {
        return false;
      }
      return true;
    }
  }

  @Nullable private static Constructor<LocationProvider> locationProviderConstructor;

  @RealObject private LocationManager realLocationManager;

  @GuardedBy("providers")
  private final HashSet<ProviderEntry> providers = new HashSet<>();

  @GuardedBy("gpsStatusListeners")
  private final HashSet<GpsStatus.Listener> gpsStatusListeners = new HashSet<>();

  @GuardedBy("gnssStatusCallbacks")
  private final Map<GnssStatus.Callback, Executor> gnssStatusCallbacks = new LinkedHashMap<>();

  @GuardedBy("nmeaMessageListeners")
  private final Map<OnNmeaMessageListener, Executor> nmeaMessageListeners = new LinkedHashMap<>();

  @GuardedBy("gnssMeasurementListeners")
  private final Map<GnssMeasurementsEvent.Callback, Executor> gnssMeasurementListeners =
      new LinkedHashMap<>();

  @GuardedBy("gnssAntennaInfoListeners")
  private final Map<Object, Executor> gnssAntennaInfoListeners = new LinkedHashMap<>();

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
    synchronized (providers) {
      ArrayList<String> allProviders = new ArrayList<>(providers.size());
      for (ProviderEntry providerEntry : providers) {
        allProviders.add(providerEntry.name);
      }
      return allProviders;
    }
  }

  @Implementation
  @Nullable
  protected LocationProvider getProvider(String name) {
    if (RuntimeEnvironment.getApiLevel() < KITKAT) {
      // jelly bean has no way to properly construct a LocationProvider, we give up
      return null;
    }

    ProviderEntry providerEntry = getProviderEntry(name);
    if (providerEntry == null) {
      return null;
    }

    try {
      synchronized (ShadowLocationManager.class) {
        if (locationProviderConstructor == null) {
          locationProviderConstructor = loadLocationProviderConstructor();

          locationProviderConstructor.setAccessible(true);
        }
      }
      return locationProviderConstructor.newInstance(name, providerEntry.createRealProperties());
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
  }

  protected Constructor<LocationProvider> loadLocationProviderConstructor()
      throws ReflectiveOperationException {
    if (RuntimeEnvironment.getApiLevel() > R) {
      return LocationProvider.class.getConstructor(
          String.class, android.location.provider.ProviderProperties.class);
    } else {
      return LocationProvider.class.getConstructor(
          String.class, Class.forName("com.android.internal.location.ProviderProperties"));
    }
  }

  @Implementation
  protected List<String> getProviders(boolean enabledOnly) {
    return getProviders(null, enabledOnly);
  }

  @Implementation
  protected List<String> getProviders(@Nullable Criteria criteria, boolean enabled) {
    synchronized (providers) {
      ArrayList<String> matchingProviders = new ArrayList<>(providers.size());
      for (ProviderEntry providerEntry : providers) {
        if (enabled && !isProviderEnabled(providerEntry.name)) {
          continue;
        }
        if (criteria != null && !providerEntry.meetsCriteria(criteria)) {
          continue;
        }
        matchingProviders.add(providerEntry.name);
      }
      return matchingProviders;
    }
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

  @Implementation
  protected boolean isProviderEnabled(String provider) {
    if (RuntimeEnvironment.getApiLevel() >= P) {
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
   * already.
   */
  public void setProviderProperties(String name, @Nullable ProviderProperties properties) {
    if (name == null) {
      throw new NullPointerException();
    }
    getOrCreateProviderEntry(name).properties = properties;
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
  @Implementation(minSdk = P)
  protected boolean isLocationEnabledForUser(UserHandle userHandle) {
    return isLocationEnabled();
  }

  private boolean isLocationEnabled() {
    return getLocationMode() != LOCATION_MODE_OFF;
  }

  // @SystemApi
  @Implementation(minSdk = P)
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
    if (RuntimeEnvironment.getApiLevel() >= P) {
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

    return providerEntry.lastLocation;
  }

  /** Sets the last known location for the given provider. */
  public void setLastKnownLocation(String provider, @Nullable Location location) {
    getOrCreateProviderEntry(provider).lastLocation = location;
  }

  @Implementation(minSdk = R)
  protected void getCurrentLocation(
      String provider,
      @Nullable CancellationSignal cancellationSignal,
      Executor executor,
      Consumer<Location> consumer) {
    getCurrentLocationInternal(provider, cancellationSignal, executor, consumer);
  }

  private void getCurrentLocationInternal(
      String provider,
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

    CancellableLocationListener listener = new CancellableLocationListener(executor, consumer);
    requestLocationUpdatesInternal(
        provider,
        new LocationRequest(android.location.LocationRequest.create()),
        Runnable::run,
        listener);

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
        provider, new LocationRequest(true), new HandlerExecutor(new Handler(looper)), listener);
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
        new LocationRequest(true),
        new HandlerExecutor(new Handler(looper)),
        listener);
  }

  @Implementation
  protected void requestSingleUpdate(String provider, PendingIntent pendingIntent) {
    requestLocationUpdatesInternal(provider, new LocationRequest(true), pendingIntent);
  }

  @Implementation
  protected void requestSingleUpdate(Criteria criteria, PendingIntent pendingIntent) {
    String bestProvider = getBestProvider(criteria, true);
    if (bestProvider == null) {
      throw new IllegalArgumentException("no providers found for criteria");
    }
    requestLocationUpdatesInternal(bestProvider, new LocationRequest(true), pendingIntent);
  }

  @Implementation
  protected void requestLocationUpdates(
      String provider, long minTime, float minDistance, LocationListener listener) {
    requestLocationUpdatesInternal(
        provider,
        new LocationRequest(minTime, minDistance),
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
        new LocationRequest(minTime, minDistance),
        new HandlerExecutor(new Handler(looper)),
        listener);
  }

  @Implementation(minSdk = R)
  protected void requestLocationUpdates(
      String provider,
      long minTime,
      float minDistance,
      Executor executor,
      LocationListener listener) {
    requestLocationUpdatesInternal(
        provider, new LocationRequest(minTime, minDistance), executor, listener);
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
        new LocationRequest(minTime, minDistance),
        new HandlerExecutor(new Handler(looper)),
        listener);
  }

  @Implementation(minSdk = R)
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
        bestProvider, new LocationRequest(minTime, minDistance), executor, listener);
  }

  @Implementation
  protected void requestLocationUpdates(
      String provider, long minTime, float minDistance, PendingIntent pendingIntent) {
    requestLocationUpdatesInternal(
        provider, new LocationRequest(minTime, minDistance), pendingIntent);
  }

  @Implementation
  protected void requestLocationUpdates(
      long minTime, float minDistance, Criteria criteria, PendingIntent pendingIntent) {
    String bestProvider = getBestProvider(criteria, true);
    if (bestProvider == null) {
      throw new IllegalArgumentException("no providers found for criteria");
    }
    requestLocationUpdatesInternal(
        bestProvider, new LocationRequest(minTime, minDistance), pendingIntent);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void requestLocationUpdates(
      @Nullable Object request, Object executorOrListener, Object listenerOrLooper) {
    if (request == null) {
      request = android.location.LocationRequest.create();
    }
    if (executorOrListener instanceof Executor) {
      requestLocationUpdatesInternal(
          ((android.location.LocationRequest) request).getProvider(),
          new LocationRequest((android.location.LocationRequest) request),
          (Executor) executorOrListener,
          (LocationListener) listenerOrLooper);
    } else if (executorOrListener instanceof LocationListener) {
      if (listenerOrLooper == null) {
        listenerOrLooper = Looper.myLooper();
        if (listenerOrLooper == null) {
          // forces appropriate exception
          new Handler();
        }
      }
      requestLocationUpdatesInternal(
          ((android.location.LocationRequest) request).getProvider(),
          new LocationRequest((android.location.LocationRequest) request),
          new HandlerExecutor(new Handler((Looper) listenerOrLooper)),
          (LocationListener) executorOrListener);
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void requestLocationUpdates(@Nullable Object request, Object pendingIntent) {
    if (request == null) {
      request = android.location.LocationRequest.create();
    }
    requestLocationUpdatesInternal(
        ((android.location.LocationRequest) request).getProvider(),
        new LocationRequest((android.location.LocationRequest) request),
        (PendingIntent) pendingIntent);
  }

  private void requestLocationUpdatesInternal(
      String provider, LocationRequest request, Executor executor, LocationListener listener) {
    if (provider == null || request == null || executor == null || listener == null) {
      throw new IllegalArgumentException();
    }
    getOrCreateProviderEntry(provider).addListener(listener, request, executor);
  }

  private void requestLocationUpdatesInternal(
      String provider, LocationRequest request, PendingIntent pendingIntent) {
    if (provider == null || request == null || pendingIntent == null) {
      throw new IllegalArgumentException();
    }
    getOrCreateProviderEntry(provider).addListener(pendingIntent, request);
  }

  @Implementation
  protected void removeUpdates(LocationListener listener) {
    removeListenerInternal(listener);
  }

  @Implementation
  protected void removeUpdates(PendingIntent pendingIntent) {
    removeListenerInternal(pendingIntent);
  }

  @Implementation(minSdk = P)
  protected boolean injectLocation(Location location) {
    return false;
  }

  @Implementation
  protected boolean addGpsStatusListener(GpsStatus.Listener listener) {
    synchronized (gpsStatusListeners) {
      gpsStatusListeners.add(listener);
    }
    return true;
  }

  @Implementation
  protected void removeGpsStatusListener(GpsStatus.Listener listener) {
    synchronized (gpsStatusListeners) {
      gpsStatusListeners.remove(listener);
    }
  }

  public List<GpsStatus.Listener> getGpsStatusListeners() {
    synchronized (gpsStatusListeners) {
      return new ArrayList<>(gpsStatusListeners);
    }
  }

  @Implementation(minSdk = N)
  protected boolean registerGnssStatusCallback(GnssStatus.Callback callback, Handler handler) {
    if (handler == null) {
      handler = new Handler();
    }

    synchronized (gnssStatusCallbacks) {
      gnssStatusCallbacks.put(callback, new HandlerExecutor(handler));
    }
    return true;
  }

  @Implementation(minSdk = R)
  protected boolean registerGnssStatusCallback(Executor executor, GnssStatus.Callback callback) {
    synchronized (gnssStatusCallbacks) {
      gnssStatusCallbacks.put(callback, executor);
    }
    return true;
  }

  @Implementation(minSdk = N)
  protected void unregisterGnssStatusCallback(GnssStatus.Callback callback) {
    synchronized (gnssStatusCallbacks) {
      gnssStatusCallbacks.remove(callback);
    }
  }

  /** Sends a {@link GnssStatus} to all registered {@link GnssStatus.Callback}s. */
  public void sendGnssStatus(GnssStatus status) {
    Map<GnssStatus.Callback, Executor> callbacks;
    synchronized (gnssStatusCallbacks) {
      callbacks = new LinkedHashMap<>(gnssStatusCallbacks);
    }

    for (Map.Entry<GnssStatus.Callback, Executor> callback : callbacks.entrySet()) {
      callback.getValue().execute(() -> callback.getKey().onSatelliteStatusChanged(status));
    }
  }

  @Implementation(minSdk = N)
  protected boolean addNmeaListener(OnNmeaMessageListener listener, Handler handler) {
    if (handler == null) {
      handler = new Handler();
    }

    synchronized (nmeaMessageListeners) {
      nmeaMessageListeners.put(listener, new HandlerExecutor(handler));
    }
    return true;
  }

  @Implementation(minSdk = R)
  protected boolean addNmeaListener(Executor executor, OnNmeaMessageListener listener) {
    synchronized (nmeaMessageListeners) {
      nmeaMessageListeners.put(listener, executor);
    }
    return true;
  }

  @Implementation(minSdk = N)
  protected void removeNmeaListener(OnNmeaMessageListener listener) {
    synchronized (nmeaMessageListeners) {
      nmeaMessageListeners.remove(listener);
    }
  }

  /** Sends a NMEA message to all registered {@link OnNmeaMessageListener}s. */
  public void sendNmeaMessage(String message, long timestamp) {
    Map<OnNmeaMessageListener, Executor> listeners;
    synchronized (nmeaMessageListeners) {
      listeners = new LinkedHashMap<>(nmeaMessageListeners);
    }

    for (Map.Entry<OnNmeaMessageListener, Executor> listener : listeners.entrySet()) {
      listener.getValue().execute(() -> listener.getKey().onNmeaMessage(message, timestamp));
    }
  }

  @Implementation(minSdk = N)
  protected boolean registerGnssMeasurementsCallback(
      GnssMeasurementsEvent.Callback callback, Handler handler) {
    if (handler == null) {
      handler = new Handler();
    }

    synchronized (gnssMeasurementListeners) {
      gnssMeasurementListeners.put(callback, new HandlerExecutor(handler));
    }
    return true;
  }

  @Implementation(minSdk = R)
  protected boolean registerGnssMeasurementsCallback(
      Executor executor, GnssMeasurementsEvent.Callback callback) {
    synchronized (gnssMeasurementListeners) {
      gnssMeasurementListeners.put(callback, executor);
    }
    return true;
  }

  @Implementation(minSdk = N)
  protected void unregisterGnssMeasurementsCallback(GnssMeasurementsEvent.Callback callback) {
    synchronized (gnssMeasurementListeners) {
      gnssMeasurementListeners.remove(callback);
    }
  }

  /** Sends a GNSS measurement event to all registered {@link GnssMeasurementsEvent.Callback}s. */
  public void sendGnssMeasurementsEvent(GnssMeasurementsEvent event) {
    Map<GnssMeasurementsEvent.Callback, Executor> listeners;
    synchronized (gnssMeasurementListeners) {
      listeners = new LinkedHashMap<>(gnssMeasurementListeners);
    }

    for (Map.Entry<GnssMeasurementsEvent.Callback, Executor> listener : listeners.entrySet()) {
      listener.getValue().execute(() -> listener.getKey().onGnssMeasurementsReceived(event));
    }
  }

  @Implementation(minSdk = R)
  protected boolean registerAntennaInfoListener(Object executor, Object listener) {
    // We should use Object for all input parameter, although we only want to use Object
    // to replace one input parameter to avoid NoClassDefFoundError, otherwise this method
    // will not tagged as shadow method for
    // registerAntennaInfoListener(Executor executor, GnssAntennaInfo.Listener listener).
    Preconditions.checkArgument(listener instanceof GnssAntennaInfo.Listener);
    Preconditions.checkArgument(executor instanceof Executor);
    synchronized (gnssAntennaInfoListeners) {
      gnssAntennaInfoListeners.put((GnssAntennaInfo.Listener) listener, (Executor) executor);
    }
    return true;
  }

  @Implementation(minSdk = R)
  protected void unregisterAntennaInfoListener(Object listener) {
    Preconditions.checkArgument(listener instanceof GnssAntennaInfo.Listener);
    synchronized (gnssAntennaInfoListeners) {
      gnssAntennaInfoListeners.remove((GnssAntennaInfo.Listener) listener);
    }
  }

  /** Sends a GNSS antenna info to all registered {@link GnssAntennaInfo.Listener}s. */
  public void sendGnssAntennaInfo(List<?> antennaInfos) {
    if (RuntimeEnvironment.getApiLevel() < R) {
      // The GnssAntennaInfo is added from R, we don't need to support this method
      // for lower API.
      return;
    }
    if (antennaInfos == null || antennaInfos.size() == 0) {
      return;
    }
    Map<Object, Executor> listeners;
    synchronized (gnssAntennaInfoListeners) {
      listeners = new LinkedHashMap<>(gnssAntennaInfoListeners);
    }

    List<GnssAntennaInfo> castedAntennaInfos = new ArrayList<>(antennaInfos.size());
    for (Object antennaInfo : antennaInfos) {
      castedAntennaInfos.add((GnssAntennaInfo) antennaInfo);
    }

    for (Map.Entry<Object, Executor> listenerEntry : listeners.entrySet()) {
      // Note that if we use
      // final GnssAntennaInfo.Listener castedListener =
      //     (GnssAntennaInfo.Listener) listener.getKey();
      // to extract key as local field, the JVM will parse GnssAntennaInfo.Listener
      // on lower target SDK. If we get key when running, the class loader process
      // will not been broken by NoClassDefFoundError on lower target SDK.
      listenerEntry
          .getValue()
          .execute(
              () ->
                  ((GnssAntennaInfo.Listener) listenerEntry.getKey())
                      .onGnssAntennaInfoReceived(castedAntennaInfos));
    }
  }

  /** @deprecated Use {@link #getLocationUpdateListeners()} instead. */
  @Deprecated
  public List<LocationListener> getRequestLocationUpdateListeners() {
    return new ArrayList<>(getLocationUpdateListeners());
  }

  /**
   * Delivers a new location to the appropriate listeners and updates state accordingly. Delivery
   * will ignore the enabled/disabled state of providers, unlike location on a real device.
   */
  public void simulateLocation(Location location) {
    if (location == null) {
      throw new NullPointerException();
    }

    ProviderEntry providerEntry = getOrCreateProviderEntry(location.getProvider());
    if (!PASSIVE_PROVIDER.equals(providerEntry.name)) {
      providerEntry.simulateLocation(location);
    }

    ProviderEntry passiveProviderEntry = getProviderEntry(PASSIVE_PROVIDER);
    if (passiveProviderEntry != null) {
      passiveProviderEntry.simulateLocation(location);
    }
  }

  /**
   * Retrieves a list of all currently registered listeners.
   *
   * @deprecated Do not test listeners, instead use {@link #simulateLocation(Location)} and test the
   *     results of those listeners being invoked.
   */
  @Deprecated
  public List<LocationListener> getLocationUpdateListeners() {
    synchronized (providers) {
      HashSet<LocationListener> listeners = new HashSet<>();
      for (ProviderEntry providerEntry : providers) {
        for (ProviderEntry.ListenerEntry listenerEntry : providerEntry.listeners) {
          LocationTransport transport = listenerEntry.transport;
          if (transport instanceof ListenerTransport) {
            listeners.add(((ListenerTransport) transport).locationListener);
          }
        }
      }
      return new ArrayList<>(listeners);
    }
  }

  /**
   * Retrieves a list of all currently registered listeners for the given provider.
   *
   * @deprecated Do not test listeners, instead use {@link #simulateLocation(Location)} and test the
   *     results of those listeners being invoked.
   */
  @Deprecated
  public List<LocationListener> getLocationUpdateListeners(String provider) {
    ProviderEntry providerEntry = getProviderEntry(provider);
    if (providerEntry == null) {
      return Collections.emptyList();
    }

    ArrayList<LocationListener> listeners = new ArrayList<>(providerEntry.listeners.size());
    for (ProviderEntry.ListenerEntry listenerEntry : providerEntry.listeners) {
      LocationTransport transport = listenerEntry.transport;
      if (transport instanceof ListenerTransport) {
        listeners.add(((ListenerTransport) transport).locationListener);
      }
    }
    return listeners;
  }

  /**
   * Retrieves a list of all currently registered pending intents.
   *
   * @deprecated Do not test pending intents, instead use {@link #simulateLocation(Location)} and
   *     test the results of those pending intent being invoked.
   */
  @Deprecated
  public List<PendingIntent> getLocationUpdatePendingIntents() {
    synchronized (providers) {
      HashSet<PendingIntent> pendingIntents = new HashSet<>();
      for (ProviderEntry providerEntry : providers) {
        for (ProviderEntry.ListenerEntry listenerEntry : providerEntry.listeners) {
          LocationTransport transport = listenerEntry.transport;
          if (transport instanceof PendingIntentTransport) {
            pendingIntents.add(((PendingIntentTransport) transport).pendingIntent);
          }
        }
      }
      return new ArrayList<>(pendingIntents);
    }
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

    ArrayList<PendingIntent> pendingIntents = new ArrayList<>(providerEntry.listeners.size());
    for (ProviderEntry.ListenerEntry listenerEntry : providerEntry.listeners) {
      LocationTransport transport = listenerEntry.transport;
      if (transport instanceof PendingIntentTransport) {
        pendingIntents.add(((PendingIntentTransport) transport).pendingIntent);
      }
    }
    return pendingIntents;
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

  private void removeListenerInternal(Object key) {
    synchronized (providers) {
      for (ProviderEntry providerEntry : providers) {
        providerEntry.removeListener(key);
      }
    }
  }

  @Nullable
  private ProviderEntry getProviderEntry(String name) {
    if (name == null) {
      return null;
    }

    synchronized (providers) {
      for (ProviderEntry providerEntry : providers) {
        if (name.equals(providerEntry.name)) {
          return providerEntry;
        }
      }
    }

    return null;
  }

  private void removeProviderEntry(String name) {
    synchronized (providers) {
      ProviderEntry providerEntry = getProviderEntry(name);
      providers.remove(providerEntry);
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
    private final CopyOnWriteArraySet<ListenerEntry> listeners;

    @Nullable private volatile ProviderProperties properties;
    private boolean enabled;
    private Location lastLocation;

    private ProviderEntry(String name, @Nullable ProviderProperties properties) {
      this.name = name;
      listeners = new CopyOnWriteArraySet<>();
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

    public boolean isEnabled() {
      if (PASSIVE_PROVIDER.equals(name) || RuntimeEnvironment.getApiLevel() >= Q) {
        return enabled;
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
      if (RuntimeEnvironment.getApiLevel() < P) {
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
      } else if (RuntimeEnvironment.getApiLevel() >= Q) {
        if (enabled == this.enabled) {
          return;
        }

        this.enabled = enabled;
        // set LOCATION_PROVIDERS_ALLOWED directly, without setting LOCATION_MODE. do this even
        // though LOCATION_PROVIDERS_ALLOWED is not the source of truth - we keep it up to date, but
        // ignore any direct writes to it
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

      // fire listeners
      for (ProviderEntry.ListenerEntry listener : listeners) {
        listener.invokeOnProviderEnabled(name, enabled);
      }
    }

    public void simulateLocation(Location location) {
      lastLocation = new Location(location);

      for (ListenerEntry listenerEntry : listeners) {
        listenerEntry.simulateLocation(location);
      }
    }

    public boolean meetsCriteria(Criteria criteria) {
      if (PASSIVE_PROVIDER.equals(name)) {
        return false;
      }

      ProviderProperties myProperties = properties;
      if (myProperties == null) {
        return false;
      }
      return myProperties.meetsCriteria(criteria);
    }

    public Object createRealProperties() {
      ProviderProperties myProperties = properties;
      if (myProperties == null) {
        return null;
      } else {
        return ShadowLocationManager.this.createRealProperties(
            myProperties.requiresNetwork,
            myProperties.requiresSatellite,
            myProperties.requiresCell,
            myProperties.hasMonetaryCost,
            myProperties.supportsAltitude,
            myProperties.supportsSpeed,
            myProperties.supportsBearing,
            myProperties.powerRequirement,
            myProperties.accuracy);
      }
    }

    public void addListener(
        LocationListener listener,
        ShadowLocationManager.LocationRequest request,
        Executor executor) {
      add(new ListenerEntry(listener, request, new ListenerTransport(executor, listener)));
    }

    public void addListener(
        PendingIntent pendingIntent, ShadowLocationManager.LocationRequest request) {
      add(
          new ListenerEntry(
              pendingIntent, request, new PendingIntentTransport(pendingIntent, getContext())));
    }

    private void add(ListenerEntry entry) {
      if (!enabled) {
        entry.invokeOnProviderEnabled(name, false);
      }
      listeners.add(entry);
    }

    public void removeListener(Object key) {
      for (ListenerEntry listenerEntry : listeners) {
        if (listenerEntry.key == key) {
          listeners.remove(listenerEntry);
        }
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ProviderEntry)) {
        return false;
      }
      ProviderEntry that = (ProviderEntry) o;
      return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name);
    }

    private final class ListenerEntry {

      private final Object key;
      private final LocationTransport transport;
      private final ShadowLocationManager.LocationRequest request;

      private Location lastDeliveredLocation;
      private int numDeliveries;

      private ListenerEntry(
          Object key, ShadowLocationManager.LocationRequest request, LocationTransport transport) {
        if (key == null) {
          throw new IllegalArgumentException();
        }

        this.key = key;
        this.request = request;
        this.transport = transport;
      }

      public void simulateLocation(Location location) {
        if (lastDeliveredLocation != null) {
          if (location.getTime() - lastDeliveredLocation.getTime()
              < request.minUpdateIntervalMillis) {
            return;
          }
          if (distanceBetween(location, lastDeliveredLocation) < request.minUpdateDistanceMeters) {
            return;
          }
        }

        lastDeliveredLocation = new Location(location);

        if (++numDeliveries >= request.maxUpdates) {
          listeners.remove(this);
        }

        try {
          transport.onLocation(location);
        } catch (Exception e) {
          removeListener(key);
        }
      }

      public void invokeOnProviderEnabled(String provider, boolean enabled) {
        try {
          transport.onProviderEnabled(provider, enabled);
        } catch (Exception e) {
          removeListener(key);
        }
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (!(o instanceof ListenerEntry)) {
          return false;
        }
        ListenerEntry that = (ListenerEntry) o;
        return key == that.key;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(key);
      }
    }
  }

  protected Object createRealProperties(
      boolean requiresNetwork,
      boolean requiresSatellite,
      boolean requiresCell,
      boolean hasMonetaryCost,
      boolean supportsAltitude,
      boolean supportsSpeed,
      boolean supportsBearing,
      int powerRequirement,
      int accuracy) {
    if (RuntimeEnvironment.getApiLevel() > R) {
      return new android.location.provider.ProviderProperties.Builder()
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
  }

  // LocationRequest doesn't exist on JB, so we can't use the platform version
  private static class LocationRequest {

    private final long minUpdateIntervalMillis;
    private final int maxUpdates;
    private final float minUpdateDistanceMeters;

    LocationRequest(android.location.LocationRequest locationRequest) {
      minUpdateIntervalMillis = locationRequest.getFastestInterval();
      maxUpdates = locationRequest.getNumUpdates();
      minUpdateDistanceMeters = locationRequest.getSmallestDisplacement();
    }

    LocationRequest(long interval, float minUpdateDistanceMeters) {
      minUpdateIntervalMillis = interval;
      maxUpdates = Integer.MAX_VALUE;
      this.minUpdateDistanceMeters = minUpdateDistanceMeters;
    }

    LocationRequest(boolean singleShot) {
      minUpdateIntervalMillis = 0;
      maxUpdates = singleShot ? 1 : Integer.MAX_VALUE;
      minUpdateDistanceMeters = 0;
    }
  }

  private interface LocationTransport {
    void onLocation(Location location) throws Exception;

    void onProviderEnabled(String provider, boolean enabled) throws Exception;
  }

  private static final class ListenerTransport implements LocationTransport {

    private final Executor executor;
    private final LocationListener locationListener;

    ListenerTransport(Executor executor, LocationListener locationListener) {
      this.executor = executor;
      this.locationListener = locationListener;
    }

    @Override
    public void onLocation(Location location) {
      executor.execute(() -> locationListener.onLocationChanged(new Location(location)));
    }

    @Override
    public void onProviderEnabled(String provider, boolean enabled) {
      executor.execute(
          () -> {
            if (enabled) {
              locationListener.onProviderEnabled(provider);
            } else {
              locationListener.onProviderDisabled(provider);
            }
          });
    }
  }

  private static final class PendingIntentTransport implements LocationTransport {

    private final PendingIntent pendingIntent;
    private final Context context;

    PendingIntentTransport(PendingIntent pendingIntent, Context context) {
      this.pendingIntent = pendingIntent;
      this.context = context;
    }

    @Override
    public void onLocation(Location location) throws CanceledException {
      Intent intent = new Intent();
      intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, new Location(location));
      pendingIntent.send(context, 0, intent);
    }

    @Override
    public void onProviderEnabled(String provider, boolean enabled) throws CanceledException {
      Intent intent = new Intent();
      intent.putExtra(LocationManager.KEY_PROVIDER_ENABLED, enabled);
      pendingIntent.send(context, 0, intent);
    }
  }

  /**
   * Returns the distance between the two locations in meters. Adapted from:
   * http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
   */
  private static float distanceBetween(Location location1, Location location2) {
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
  public static void reset() {
    locationProviderConstructor = null;
  }

  private final class CancellableLocationListener implements LocationListener {

    private final Executor executor;
    private final Consumer<Location> consumer;
    private final Handler timeoutHandler;

    @GuardedBy("this")
    private boolean triggered;

    @Nullable Runnable timeoutRunnable;

    CancellableLocationListener(Executor executor, Consumer<Location> consumer) {
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
