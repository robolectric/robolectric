package com.xtremelabs.robolectric.shadows;

import android.location.Criteria;
import android.location.GpsStatus.Listener;
import android.location.Location;
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

    private final ArrayList<Listener> listeners = new ArrayList<Listener>();
    private Criteria lastBestProviderCriteria;
    private boolean lastBestProviderEnabledOnly;
    private String bestProvider;

    @Implementation
    public boolean isProviderEnabled(String provider) {
        Boolean isEnabled = providersEnabled.get(provider);
        return isEnabled == null ? true : isEnabled;
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
		if(!listeners.contains(listener)) {
			listeners.add(listener);
        }
		return true;
	}
	
	@Implementation
	public void removeGpsStatusListener(Listener listener) {
		listeners.remove(listener);
	}

    @Implementation
    public java.lang.String getBestProvider(android.location.Criteria criteria, boolean enabledOnly) {
        lastBestProviderCriteria = criteria;
        lastBestProviderEnabledOnly = enabledOnly;
        return bestProvider;
    }

    public boolean hasListener(Listener listener) {
        return listeners.contains(listener);
    }

    /**
     * Non-Android accessor.
     *
     * Gets the criteria value used in the last call to {@link #getBestProvider(android.location.Criteria, boolean)}
     *
     * @return the criteria used to find the best provider
     */
    public Criteria getLastBestProviderCriteria() {
        return lastBestProviderCriteria;
    }

    /**
     * Non-Android accessor.
     *
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
     * @param provider  name of the provider who should be considered best
     */
    public void setBestProvider(String provider) {
        bestProvider = provider;
    }

    /**
     * Sets the value to return from {@link #getLastKnownLocation(String)} for the given {@code provider}
     *
     * @param provider  name of the provider whose location to set
     * @param location  the last known location for the provider
     */
    public void setLastKnownLocation(String provider, Location location) {
        lastKnownLocations.put(provider, location);
    }
}
