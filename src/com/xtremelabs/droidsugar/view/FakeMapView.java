package com.xtremelabs.droidsugar.view;

import android.view.ViewGroup;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeMapView extends FakeViewGroup {
    private boolean satelliteOn;
    public MapController mapController;
    private List<Overlay> overlays = new ArrayList<Overlay>();
    public GeoPoint mapCenter = new GeoPoint(10, 10);
    public int longitudeSpan = 20;
    public int latitudeSpan = 30;
    public int zoomLevel = 1;

    public FakeMapView(ViewGroup viewGroup) {
        super(viewGroup);
        mapController = mock(MapController.class);
    }

    public void setSatellite(boolean satelliteOn) {
        this.satelliteOn = satelliteOn;
    }

    public boolean isSatellite() {
        return satelliteOn;
    }

    public MapController getController() {
        return mapController;
    }

    public List<Overlay> getOverlays() {
       return overlays;
    }

    public GeoPoint getMapCenter() {
        return mapCenter;
    }

    public int getLatitudeSpan() {
        return latitudeSpan;
    }

    public int getLongitudeSpan() {
        return longitudeSpan;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }
}
