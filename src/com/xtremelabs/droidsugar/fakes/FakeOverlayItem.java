package com.xtremelabs.droidsugar.fakes;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(OverlayItem.class)
public class FakeOverlayItem {
    private GeoPoint geoPoint;

    public void __constructor__(GeoPoint geoPoint, String title, String snippet) {
        this.geoPoint = geoPoint;
    }

    public GeoPoint getPoint() {
        return geoPoint;
    }
}
