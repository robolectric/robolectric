package com.xtremelabs.droidsugar.fakes;

import com.google.android.maps.GeoPoint;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

import static com.xtremelabs.droidsugar.fakes.FakeMapView.fromE6;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(GeoPoint.class)
public class FakeGeoPoint {
    public int lat;
    public int lng;

    public void __constructor__(int lat, int lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Implementation
    public int getLatitudeE6() {
        return lat;
    }

    @Implementation
    public int getLongitudeE6() {
        return lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        FakeGeoPoint that = (FakeGeoPoint) ProxyDelegatingHandler.getInstance().proxyFor(o);

        if (lat != that.lat) return false;
        if (lng != that.lng) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lat;
        result = 31 * result + lng;
        return result;
    }

    @Override public String toString() {
        return "FakeGeoPoint{" +
                "lat=" + fromE6(lat) +
                ", lng=" + fromE6(lng) +
                '}';
    }
}
