package com.xtremelabs.robolectric.shadows;

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
    private Location simulatedLocation = null;

    /** Location listeners along with metadata on when they should be fired. */
    private static final class ListenerRegistration {
        final long minTime;
        final float minDistance;
        Location lastSeenLocation;
        long lastSeenTime;
        final LocationListener listener;

        ListenerRegistration(long minTime, float minDistance, Location locationAtCreation,
                LocationListener listener) {
            this.minTime = minTime;
            this.minDistance = minDistance;
            this.lastSeenTime = locationAtCreation == null ? 0 : locationAtCreation.getTime();
            this.lastSeenLocation = locationAtCreation;
            this.listener = listener;
        }
    }

    /** Mapped by provider. */
    private final Map<String, List<ListenerRegistration>> locationListeners =
            new HashMap<String, List<ListenerRegistration>>();

    @Implementation
    public List<String> getAllProviders() {
        return new ArrayList<String>(providersEnabled.keySet());
    }

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
    public Location getLastKnownLocation(String provider) {
        return simulatedLocation;
    }

    @Implementation
    public void requestLocationUpdates(String provider,
            long minTime, float minDistance, LocationListener listener) {
        List<ListenerRegistration> providerListeners = locationListeners.get(provider);
        if (providerListeners == null) {
            providerListeners = new ArrayList<ListenerRegistration>();
            locationListeners.put(provider, providerListeners);
        }
        providerListeners.add(new ListenerRegistration(
            minTime, minDistance, copyOf(simulatedLocation), listener));
    }

    public void simulateLocation(Location location) {
        simulatedLocation = location;

        List<ListenerRegistration> providerListeners = locationListeners.get(
            location.getProvider());
        if (providerListeners == null) return;

        for (ListenerRegistration listenerReg : providerListeners) {
            if(listenerReg.lastSeenLocation != null && simulatedLocation != null) {
              float distanceChange = distanceBetween(simulatedLocation, listenerReg.lastSeenLocation);
              boolean withinMinDistance = distanceChange < listenerReg.minDistance;
              boolean exceededMinTime = location.getTime() - listenerReg.lastSeenTime > listenerReg.minTime;
              if (withinMinDistance && !exceededMinTime) continue;
            }
            listenerReg.lastSeenLocation = copyOf(location);
            listenerReg.lastSeenTime = location == null ? 0 : location.getTime();
            listenerReg.listener.onLocationChanged(copyOf(location));
        }
    }

  private Location copyOf(Location location) {
      if (location == null) return null;
      Location copy = new Location(location);
      copy.setAccuracy(location.getAccuracy());
      copy.setAltitude(location.getAltitude());
      copy.setBearing(location.getBearing());
      copy.setExtras(location.getExtras());
      copy.setLatitude(location.getLatitude());
      copy.setLongitude(location.getLongitude());
      copy.setProvider(location.getProvider());
      copy.setSpeed(location.getSpeed());
      copy.setTime(location.getTime());
      return copy;
  }

  /**
   * Returns the distance between the two locations in meters.
   * Adapted from: http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
   */
  private static float distanceBetween(Location location1, Location location2) {
      double earthRadius = 3958.75;
      double latDifference = Math.toRadians(location2.getLatitude() - location1.getLatitude());
      double lonDifference = Math.toRadians(location2.getLongitude() - location2.getLongitude());
      double a = Math.sin(latDifference/2) * Math.sin(latDifference/2) +
                 Math.cos(Math.toRadians(location1.getLatitude())) * Math.cos(Math.toRadians(location2.getLatitude())) *
                 Math.sin(lonDifference/2) * Math.sin(lonDifference/2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
      double dist = Math.abs(earthRadius * c);

      int meterConversion = 1609;

      return new Float(dist * meterConversion).floatValue();
  }
}
