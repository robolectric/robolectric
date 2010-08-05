package com.xtremelabs.droidsugar.fakes;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapController.class)
public class FakeMapController {

    public FakeMapView fakeMapView;
    public GeoPoint geoPointAnimatedTo;
    public int zoomedLatSpan;
    public int zoomedLngSpan;

    public void animateTo(com.google.android.maps.GeoPoint geoPoint, java.lang.Runnable runnable) {
        fakeMapView.mapCenter = geoPoint;
        geoPointAnimatedTo = geoPoint;
        runnable.run();
    }

    public void zoomToSpan(int latSpan, int lngSpan) {
        zoomedLatSpan = latSpan;
        zoomedLngSpan = lngSpan;
    }
}
