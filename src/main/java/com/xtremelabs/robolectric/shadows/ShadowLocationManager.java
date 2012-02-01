package com.xtremelabs.robolectric.shadows;

import java.util.LinkedHashSet;
import java.util.Set;
import android.location.Criteria;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shadow of {@code LocationManager} that provides for the simulation of different location providers being enabled and
 * disabled.
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(LocationManager.class)
public class ShadowLocationManager {
    private final Map<String, Boolean> providersEnabled = new HashMap<String, Boolean>();
    private final Map<String, Location> lastKnownLocations = new HashMap<String, Location>();

    private final ArrayList<Listener> gpsStatusListeners = new ArrayList<Listener>();
    private Criteria lastBestProviderCriteria;
    private boolean lastBestProviderEnabledOnly;
    private String bestProvider;
    private List<LocationListener> requestLocationUdpateListeners = new ArrayList<LocationListener>();

    @Implementation
    public boolean isProviderEnabled(String provider) {
        Boolean isEnabled = providersEnabled.get(provider);
        return isEnabled == null ? true : isEnabled;
    }

    @Implementation
    public List<String> getAllProviders() {
        Set<String> allKnownProviders = new LinkedHashSet<String>(providersEnabled.keySet());
        allKnownProviders.add(LocationManager.GPS_PROVIDER);
        allKnownProviders.add(LocationManager.NETWORK_PROVIDER);
        allKnownProviders.add(LocationManager.PASSIVE_PROVIDER);

        return new ArrayList<String>(allKnownProviders);
    }

    /**
     * Sets the value to return from {@link #isProviderEnabled(String)} for the given {@code provider}
     *
     * @param provider  name of the provider whose status to set
     * @param isEnabled whether that provider should appear enabled
     */
    public void setProviderEnabled(String provider, boolean isEnabled) {
        providersEnabled.put(provider, isEnabled);
    }

    @Implementation
    public List<String> getProviders(boolean enabledOnly) {
        ArrayList<String> enabledProviders = new ArrayList<String>();
        for (Map.Entry<String, Boolean> entry : providersEnabled.entrySet()) {
            if (entry.getValue()) {
                enabledProviders.add(entry.getKey());
            }
        }
        return enabledProviders;
    }

    @Implementation
    public Location getLastKnownLocation(String provider) {
        return lastKnownLocations.get(provider);
    }

    @Implementation
    public boolean addGpsStatusListener(Listener listener) {
        if (!gpsStatusListeners.contains(listener)) {
            gpsStatusListeners.add(listener);
        }
        return true;
    }

    @Implementation
    public void removeGpsStatusListener(Listener listener) {
        gpsStatusListeners.remove(listener);
    }

    @Implementation
    public String getBestProvider(android.location.Criteria criteria, boolean enabledOnly) {
        lastBestProviderCriteria = criteria;
        lastBestProviderEnabledOnly = enabledOnly;
        return bestProvider;
    }

    @Implementation
    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener) {
        requestLocationUdpateListeners.add(listener);
    }

    @Implementation
    public void removeUpdates(LocationListener listener) {
        while (requestLocationUdpateListeners.remove(listener));
    }


    public boolean hasGpsStatusListener(Listener listener) {
        return gpsStatusListeners.contains(listener);
    }

    /**
     * Non-Android accessor.
     * <p/>
     * Gets the criteria value used in the last call to {@link #getBestProvider(android.location.Criteria, boolean)}
     *
     * @return the criteria used to find the best provider
     */
    public Criteria getLastBestProviderCriteria() {
        return lastBestProviderCriteria;
    }

    /**
     * Non-Android accessor.
     * <p/>
     * Gets the enabled value used in the last call to {@link #getBestProvider(android.location.Criteria, boolean)}
     *
     * @return the enabled value used to find the best provider
     */
    public boolean getLastBestProviderEnabledOnly() {
        return lastBestProviderEnabledOnly;
    }

    /**
     * Sets the value to return from {@link #getBestProvider(android.location.Criteria, boolean)}
     * for the given {@code provider}
     *
     * @param provider name of the provider who should be considered best
     */
    public void setBestProvider(String provider) {
        bestProvider = provider;
    }

    /**
     * Sets the value to return from {@link #getLastKnownLocation(String)} for the given {@code provider}
     *
     * @param provider name of the provider whose location to set
     * @param location the last known location for the provider
     */
    public void setLastKnownLocation(String provider, Location location) {
        lastKnownLocations.put(provider, location);
    }

    /**
     * Non-Android accessor.
     *
     * @return lastRequestedLocationUpdatesLocationListener
     */
    public List<LocationListener> getRequestLocationUpdateListeners() {
        return requestLocationUdpateListeners;
    }
}
