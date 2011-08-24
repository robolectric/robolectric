package com.xtremelabs.robolectric.shadows;

import android.location.Criteria;
import android.location.GpsStatus.Listener;
import android.location.LocationManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Shadow of {@code LocationManager} that provides for the simulation of different location providers being enabled and
 * disabled.
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(LocationManager.class)
public class ShadowLocationManager {
    private final Map<String, Boolean> providersEnabled = new HashMap<String, Boolean>();
    
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
	public boolean addGpsStatusListener(Listener listener) {
		
		if(!listeners.contains(listener))
			listeners.add(listener);
		
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

    public Criteria getLastBestProviderCriteria() {
        return lastBestProviderCriteria;
    }

    public boolean getLastBestProviderEnabledOnly() {
        return lastBestProviderEnabledOnly;
    }

    public void setBestProvider(String provider) {
        bestProvider = provider;
    }
}
