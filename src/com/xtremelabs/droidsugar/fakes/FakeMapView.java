package com.xtremelabs.droidsugar.fakes;

import android.graphics.Point;
import android.view.MotionEvent;
import android.widget.ZoomButtonsController;
import com.google.android.maps.*;
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
    private Projection projection;

    public FakeMapView(MapView mapView) {
        super(mapView);
        realMapView = mapView;
        zoomButtonsController = new ZoomButtonsController(mapView);
    }

    public static int toE6(double d) {
        return (int) (d * 1e6);
    }

    public static double fromE6(int i) {
        return i / 1e6;
    }

    public void setSatellite(boolean satelliteOn) {
        this.satelliteOn = satelliteOn;
    }

    public boolean isSatellite() {
        return satelliteOn;
    }

    public MapController getController() {
        if (mapController == null) {
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

    public com.google.android.maps.Projection getProjection() {
        if (projection == null) {
            projection = new Projection() {
                @Override public Point toPixels(GeoPoint geoPoint, Point point) {
                    if (point == null) {
                        point = new Point();
                    }

                    point.y = scaleLL(geoPoint.getLatitudeE6(), top, bottom, mapCenter.getLatitudeE6(), latitudeSpan);
                    point.x = scaleLL(geoPoint.getLongitudeE6(), left, right, mapCenter.getLongitudeE6(), longitudeSpan);
                    return point;
                }

                @Override public GeoPoint fromPixels(int x, int y) {
                    int lat = scalePixel(y, top, realMapView.getHeight(), mapCenter.getLatitudeE6(), latitudeSpan);
                    int lng = scalePixel(x, left, realMapView.getWidth(), mapCenter.getLongitudeE6(), longitudeSpan);
                    return new GeoPoint(lat, lng);
                }

                @Override public float metersToEquatorPixels(float v) {
                    return 0;
                }
            };
        }
        return projection;
    }

    private int scalePixel(int pixel, int minPixel, int maxPixel, int centerLl, int spanLl) {
        int offsetPixels = pixel - minPixel;
        double ratio = offsetPixels / ((double) maxPixel);
        int minLl = centerLl - spanLl / 2;
        return (int) (minLl + spanLl * ratio);
    }

    private int scaleLL(int ll, int minPixel, int maxPixel, int centerLL, int spanLL) {
        int minLl = centerLL - spanLL / 2;
        int offsetLls = ll - minLl;
        double ratio = offsetLls / ((double) spanLL);
        int spanPixels = maxPixel - minPixel;
        return (int) (minPixel + spanPixels * ratio);
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


        // todo: this is wrong
        mapCenter = getProjection().fromPixels((int) event.getX(), (int) event.getY());

        return super.dispatchTouchEvent(event);
    }
}
