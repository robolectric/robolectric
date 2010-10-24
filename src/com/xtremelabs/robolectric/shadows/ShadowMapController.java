package com.xtremelabs.robolectric.shadows;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapController.class)
public class ShadowMapController {
    public ShadowMapView shadowMapView;
    public GeoPoint geoPointAnimatedTo;

    @Implementation
    public void animateTo(com.google.android.maps.GeoPoint geoPoint) {
        setCenter(geoPoint);
        geoPointAnimatedTo = geoPoint;
    }

    @Implementation
    public void animateTo(com.google.android.maps.GeoPoint geoPoint, java.lang.Runnable runnable) {
        animateTo(geoPoint);
        runnable.run();
    }

    @Implementation
    public void setCenter(com.google.android.maps.GeoPoint geoPoint) {
        shadowMapView.mapCenter = geoPoint;
    }

    @Implementation
    public void zoomToSpan(int latSpan, int lngSpan) {
        shadowMapView.latitudeSpan = latSpan;
        shadowMapView.longitudeSpan = lngSpan;
    }

    @Implementation
    public boolean zoomIn() {
        shadowMapView.zoomLevel++;
        return true;
    }

    @Implementation
    public boolean zoomOut() {
        shadowMapView.zoomLevel--;
        return true;
    }

    @Implementation
    public int setZoom(int i) {
        shadowMapView.zoomLevel = i;
        return i;
    }
}
