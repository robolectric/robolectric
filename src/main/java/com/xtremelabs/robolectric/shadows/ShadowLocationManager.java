package com.xtremelabs.robolectric.shadows;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.*;

/**
 * Shadow of {@code LocationManager} that provides for the simulation of different location providers being enabled and
 * disabled.
 */
@Implements(LocationManager.class)
public class ShadowLocationManager {
    private final Map<String, LocationProviderEntry> providersEnabled = new LinkedHashMap<String, LocationProviderEntry>();
    private final Map<String, Location> lastKnownLocations = new HashMap<String, Location>();
    private final Map<PendingIntent, Criteria> requestLocationUdpateCriteriaPendingIntents = new HashMap<PendingIntent, Criteria>();
    private final Map<PendingIntent, String> requestLocationUdpateProviderPendingIntents = new HashMap<PendingIntent, String>();

    private final ArrayList<Listener> gpsStatusListeners = new ArrayList<Listener>();
    private Criteria lastBestProviderCriteria;
    private boolean lastBestProviderEnabled;
    private String bestEnabledProvider, bestDisabledProvider;
    private final Map<LocationListener, Set<String>> requestLocationUpdateListenersMap = new LinkedHashMap<LocationListener, Set<String>>();

    @Implementation
    public boolean isProviderEnabled(String provider) {
        LocationProviderEntry map = providersEnabled.get(provider);
        if (map != null) {
            Boolean isEnabled = map.getKey();
            return isEnabled == null ? true : isEnabled;
        }
        return false;
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
     * @param provider
     *            name of the provider whose status to set
     * @param isEnabled
     *            whether that provider should appear enabled
     */
    public void setProviderEnabled(String provider, boolean isEnabled) {
        setProviderEnabled(provider, isEnabled, null);
    }

    public void setProviderEnabled(String provider, boolean isEnabled, List<Criteria> criteria) {
        LocationProviderEntry providerEntry = providersEnabled.get(provider);
        if (providerEntry == null) {
            providerEntry = new LocationProviderEntry();
        }
        providerEntry.enabled = isEnabled;
        providerEntry.criteria = criteria;
        providersEnabled.put(provider, providerEntry);
        List<LocationListener> locationUpdateListeners = new ArrayList<LocationListener>(requestLocationUpdateListenersMap.keySet());
        for (LocationListener locationUpdateListener : locationUpdateListeners) {
            if (isEnabled) {
                locationUpdateListener.onProviderEnabled(provider);
            } else {
                locationUpdateListener.onProviderDisabled(provider);
            }
        }
        // Send intent to notify about provider status
        final Intent intent = new Intent();
        intent.putExtra(LocationManager.KEY_PROVIDER_ENABLED, isEnabled);
        Robolectric.getShadowApplication().sendBroadcast(intent);
        Set<PendingIntent> requestLocationUdpatePendingIntentSet = requestLocationUdpateCriteriaPendingIntents
                .keySet();
        for (PendingIntent requestLocationUdpatePendingIntent : requestLocationUdpatePendingIntentSet) {
            try {
                requestLocationUdpatePendingIntent.send();
            } catch (CanceledException e) {
                requestLocationUdpateCriteriaPendingIntents
                        .remove(requestLocationUdpatePendingIntent);
            }
        }
        // if this provider gets disabled and it was the best active provider, then it's not anymore
        if (provider.equals(bestEnabledProvider) && !isEnabled) {
            bestEnabledProvider = null;
        }
    }

    @Implementation
    public List<String> getProviders(boolean enabledOnly) {
        ArrayList<String> enabledProviders = new ArrayList<String>();
        for (String provider : providersEnabled.keySet()) {
            if (!enabledOnly || providersEnabled.get(provider).getKey()) {
                enabledProviders.add(provider);
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

    /**
     * Returns the best provider with respect to the passed criteria (if any) and its status. If no criteria are passed
     *
     * NB: Gps is considered the best provider for fine accuracy and high power consumption, network is considered the
     * best provider for coarse accuracy and low power consumption.
     *
     * @param criteria
     * @param enabled
     * @return
     */
    @Implementation
    public String getBestProvider(Criteria criteria, boolean enabled) {
        lastBestProviderCriteria = criteria;
        lastBestProviderEnabled = enabled;

        if (criteria == null) {
            return getBestProviderWithNoCriteria(enabled);
        }

        return getBestProviderWithCriteria(criteria, enabled);
    }

    private String getBestProviderWithCriteria(Criteria criteria, boolean enabled) {
        List<String> providers = getProviders(enabled);
        int powerRequirement = criteria.getPowerRequirement();
        int accuracy = criteria.getAccuracy();
        for (String provider : providers) {
            LocationProviderEntry locationProviderEntry = providersEnabled.get(provider);
            if (locationProviderEntry == null) {
                continue;
            }
            List<Criteria> criteriaList = locationProviderEntry.getValue();
            if (criteriaList == null) {
                continue;
            }
            for (Criteria criteriaListItem : criteriaList) {
                if (criteria.equals(criteriaListItem)) {
                    return provider;
                } else if (criteriaListItem.getAccuracy() == accuracy) {
                    return provider;
                } else if (criteriaListItem.getPowerRequirement() == powerRequirement) {
                    return provider;
                }
            }
        }
        // TODO: these conditions are incomplete
        for (String provider : providers) {
            if (provider.equals(LocationManager.NETWORK_PROVIDER) && (accuracy == Criteria.ACCURACY_COARSE || powerRequirement == Criteria.POWER_LOW)) {
                return provider;
            } else if (provider.equals(LocationManager.GPS_PROVIDER) && accuracy == Criteria.ACCURACY_FINE && powerRequirement != Criteria.POWER_LOW) {
                return provider;
            }
        }

        // No enabled provider found with the desired criteria, then return the the first registered provider(?)
        return providers.isEmpty()? null : providers.get(0);
    }

    private String getBestProviderWithNoCriteria(boolean enabled) {
        List<String> providers = getProviders(enabled);

        if (enabled && bestEnabledProvider != null) {
            return bestEnabledProvider;
        } else if (bestDisabledProvider != null) {
            return bestDisabledProvider;
        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        return null;
    }

    @Implementation
    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener) {
        addLocationListener(provider, listener);
    }

    private void addLocationListener(String provider, LocationListener listener) {
        if (!requestLocationUpdateListenersMap.containsKey(listener)) {
            requestLocationUpdateListenersMap.put(listener, new HashSet<String>());
        }
        requestLocationUpdateListenersMap.get(listener).add(provider);
    }

    @Implementation
    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener,
            Looper looper) {
        addLocationListener(provider, listener);
    }

    @Implementation
    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            throw new IllegalStateException("Intent must not be null");
        }
        if (getBestProvider(criteria, true) == null) {
            throw new IllegalArgumentException("no providers found for criteria");
        }
        requestLocationUdpateCriteriaPendingIntents.put(pendingIntent, criteria);
    }

    @Implementation
    public void requestLocationUpdates(String provider, long minTime, float minDistance,
            PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            throw new IllegalStateException("Intent must not be null");
        }
        if (!providersEnabled.containsKey(provider)) {
            throw new IllegalArgumentException("no providers found");
        }

        requestLocationUdpateProviderPendingIntents.put(pendingIntent, provider);
    }

    @Implementation
    public void removeUpdates(LocationListener listener) {
        requestLocationUpdateListenersMap.remove(listener);
    }

    @Implementation
    public void removeUpdates(PendingIntent pendingIntent) {
        while (requestLocationUdpateCriteriaPendingIntents.remove(pendingIntent) != null);
        while (requestLocationUdpateProviderPendingIntents.remove(pendingIntent) != null);
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
        return lastBestProviderEnabled;
    }

    /**
     * Sets the value to return from {@link #getBestProvider(android.location.Criteria, boolean)} for the given
     * {@code provider}
     *
     * @param provider
     *            name of the provider who should be considered best
     * @throws Exception
     *
     */
    public boolean setBestProvider(String provider, boolean enabled, List<Criteria> criteria) throws Exception {
        if (!getAllProviders().contains(provider)) {
            throw new IllegalStateException("Best provider is not a known provider");
        }
        // If provider is not enabled but it is supposed to be set as the best enabled provider don't set it.
        for (String prvdr : providersEnabled.keySet()) {
            if (provider.equals(prvdr) && providersEnabled.get(prvdr).enabled != enabled) {
                return false;
            }
        }

        if (enabled) {
            bestEnabledProvider = provider;
            if (provider.equals(bestDisabledProvider)) {
                bestDisabledProvider = null;
            }
        } else {
            bestDisabledProvider = provider;
            if (provider.equals(bestEnabledProvider)) {
                bestEnabledProvider = null;
            }
        }
        if (criteria == null) {
            return true;
        }
        LocationProviderEntry entry;
        if (!providersEnabled.containsKey(provider)) {
            entry = new LocationProviderEntry();
            entry.enabled = enabled;
            entry.criteria = criteria;
        } else {
            entry = providersEnabled.get(provider);
        }
        providersEnabled.put(provider, entry);

        return true;
    }

    public boolean setBestProvider(String provider, boolean enabled) throws Exception {
        return setBestProvider(provider, enabled, null);
    }

    /**
     * Sets the value to return from {@link #getLastKnownLocation(String)} for the given {@code provider}
     *
     * @param provider
     *            name of the provider whose location to set
     * @param location
     *            the last known location for the provider
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
        return new ArrayList<LocationListener>(requestLocationUpdateListenersMap.keySet());
    }

    public Map<PendingIntent, Criteria> getRequestLocationUdpateCriteriaPendingIntents() {
        return requestLocationUdpateCriteriaPendingIntents;
    }

    public Map<PendingIntent, String> getRequestLocationUdpateProviderPendingIntents() {
        return requestLocationUdpateProviderPendingIntents;
    }

    public Collection<String> getProvidersForListener(LocationListener listener) {
        Set<String> providers = requestLocationUpdateListenersMap.get(listener);
        return providers == null ? Collections.<String>emptyList() : new ArrayList<String>(providers);
    }

    final private class LocationProviderEntry implements Map.Entry<Boolean, List<Criteria>> {
        private Boolean enabled;
        private List<Criteria> criteria;

        @Override
        public Boolean getKey() {
            return enabled;
        }

        @Override
        public List<Criteria> getValue() {
            return criteria;
        }

        @Override
        public List<Criteria> setValue(List<Criteria> criteria) {
            List<Criteria> oldCriteria = this.criteria;
            this.criteria = criteria;
            return oldCriteria;
        }
    }

}
