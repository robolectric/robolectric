package com.xtremelabs.robolectric.shadows;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * A Shadow of {@code MapController} that tracks its own state and keeps the state of the {@code MapView} it controlls
 * up to date.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapController.class)
public class ShadowMapController {
    private ShadowMapView shadowMapView;
    private GeoPoint geoPointAnimatedTo;

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

    /**
     * Non-Android accessor that returns the {@code MapView} that is being controlled
     *
     * @return the {@code MapView} that is being controlled
     */
    public ShadowMapView getShadowMapView() {
        return shadowMapView;
    }

    /**
     * Non-Android accessor that returns the most recent value set by a call to either version of {@code animateTo()}
     *
     * @return the most recent value set by a call to either version of {@code animateTo()}
     */
    public GeoPoint getGeoPointAnimatedTo() {
        return geoPointAnimatedTo;
    }

    /**
     * Non-Android accessor that allows the {@code MapView} being controlled to be set explicitly.
     *
     * @param shadowMapView the {@link ShadowMapView} to be controlled (either created explicitly or obtained via a call
     *                      to {@link com.xtremelabs.robolectric.RobolectricForMaps.shadowOf(com.google.android.maps.MapView)})
     */
    void setShadowMapView(ShadowMapView shadowMapView) {
        this.shadowMapView = shadowMapView;
    }
}
