package com.xtremelabs.droidsugar.fakes;

import android.view.MotionEvent;
import android.widget.ZoomButtonsController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.FakeHelper;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapView.class)
public class FakeMapView extends FakeViewGroup {
    private boolean satelliteOn;
    public MapController mapController;
    private List<Overlay> overlays = new ArrayList<Overlay>();
    public GeoPoint mapCenter = new GeoPoint(10, 10);
    public int longitudeSpan = 20;
    public int latitudeSpan = 30;
    int zoomLevel = 1;
    FakeMapController fakeMapController;
    private ZoomButtonsController zoomButtonsController;
    private MapView realMapView;

    public FakeMapView(MapView mapView) {
        super(mapView);
        realMapView = mapView;
        zoomButtonsController = new ZoomButtonsController(mapView);
    }

    public void setSatellite(boolean satelliteOn) {
        this.satelliteOn = satelliteOn;
    }

    public boolean isSatellite() {
        return satelliteOn;
    }

    public MapController getController() {
        if(mapController == null) {
            try {
                mapController = FakeHelper.newInstanceOf(MapController.class);
                fakeMapController = ((FakeMapController) ProxyDelegatingHandler.getInstance().proxyFor(mapController));
                fakeMapController.fakeMapView = this;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return mapController;
    }

    public ZoomButtonsController getZoomButtonsController() {
        return zoomButtonsController;
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

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        for (Overlay overlay : overlays) {
            if (overlay.onTouchEvent(event, realMapView)) {
                return true;
            }
        }


//        mapCenter = new GeoPoint(lat, lng);

        return super.dispatchTouchEvent(event);
    }
}
