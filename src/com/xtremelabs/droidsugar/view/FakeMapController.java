package com.xtremelabs.droidsugar.view;

import com.google.android.maps.GeoPoint;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeMapController {

    public GeoPoint geoPointAnimatedTo;
    public int zoomedLatSpan;
    public int zoomedLngSpan;

    public void animateTo(com.google.android.maps.GeoPoint geoPoint, java.lang.Runnable runnable) {
        geoPointAnimatedTo = geoPoint;
        runnable.run();
    }

    public void zoomToSpan(int latSpan, int lngSpan) {
        zoomedLatSpan = latSpan;
        zoomedLngSpan = lngSpan;
    }
}
