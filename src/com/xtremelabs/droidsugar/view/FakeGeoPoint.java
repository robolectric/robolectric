package com.xtremelabs.droidsugar.view;

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
}
