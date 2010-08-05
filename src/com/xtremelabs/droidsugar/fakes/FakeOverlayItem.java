package com.xtremelabs.droidsugar.fakes;

import com.google.android.maps.GeoPoint;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeOverlayItem {
    private GeoPoint geoPoint;

    public void __constructor__(GeoPoint geoPoint, String title, String snippet) {
        this.geoPoint = geoPoint;
    }

    public GeoPoint getPoint() {
        return geoPoint;
    }
}
