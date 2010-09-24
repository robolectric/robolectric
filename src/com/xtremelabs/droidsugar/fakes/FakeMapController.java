package com.xtremelabs.droidsugar.fakes;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapController.class)
public class FakeMapController {
    public FakeMapView fakeMapView;
    public GeoPoint geoPointAnimatedTo;

    public void animateTo(com.google.android.maps.GeoPoint geoPoint) {
        setCenter(geoPoint);
        geoPointAnimatedTo = geoPoint;
    }

    public void animateTo(com.google.android.maps.GeoPoint geoPoint, java.lang.Runnable runnable) {
        animateTo(geoPoint);
        runnable.run();
    }

    public void setCenter(com.google.android.maps.GeoPoint geoPoint) {
        fakeMapView.mapCenter = geoPoint;
    }

    public void zoomToSpan(int latSpan, int lngSpan) {
        fakeMapView.latitudeSpan = latSpan;
        fakeMapView.longitudeSpan = lngSpan;
    }

    public boolean zoomIn() {
        fakeMapView.zoomLevel++;
        return true;
    }

    public boolean zoomOut() {
        fakeMapView.zoomLevel--;
        return true;
    }

    public int setZoom(int i) {
        fakeMapView.zoomLevel = i;
        return i;
    }
}
