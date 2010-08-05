package com.xtremelabs.droidsugar.fakes;

import com.xtremelabs.droidsugar.ProxyDelegatingHandler;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeGeoPoint {
    public int lat;
    public int lng;

    public void __constructor__(int lat, int lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public int getLatitudeE6() {
        return lat;
    }

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
}
