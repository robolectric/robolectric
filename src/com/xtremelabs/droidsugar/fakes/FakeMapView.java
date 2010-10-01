package com.xtremelabs.droidsugar.fakes;

import android.content.Context;
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
    public boolean useBuiltInZoomMapControls;
    private boolean mouseDownOnMe = false;
    private Point lastTouchEventPoint;
    private GeoPoint mouseDownCenter;

    public FakeMapView(MapView mapView) {
        super(mapView);
        realMapView = mapView;
        zoomButtonsController = new ZoomButtonsController(mapView);
    }

    public void __constructor__(Context context, String title) {
        super.__constructor__(context);
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

    public void setBuiltInZoomControls(boolean useBuiltInZoomMapControls) {
        this.useBuiltInZoomMapControls = useBuiltInZoomMapControls;
    }

    public com.google.android.maps.Projection getProjection() {
        if (projection == null) {
            projection = new Projection() {
                @Override public Point toPixels(GeoPoint geoPoint, Point point) {
                    if (point == null) {
                        point = new Point();
                    }

                    point.y = scaleDegree(geoPoint.getLatitudeE6(), bottom, top, mapCenter.getLatitudeE6(), latitudeSpan);
                    point.x = scaleDegree(geoPoint.getLongitudeE6(), left, right, mapCenter.getLongitudeE6(), longitudeSpan);
                    return point;
                }

                @Override public GeoPoint fromPixels(int x, int y) {
                    int lat = scalePixel(y, bottom, -realMapView.getHeight(), mapCenter.getLatitudeE6(), latitudeSpan);
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

    private int scalePixel(int pixel, int minPixel, int maxPixel, int centerDegree, int spanDegrees) {
        int offsetPixels = pixel - minPixel;
        double ratio = offsetPixels / ((double) maxPixel);
        int minDegrees = centerDegree - spanDegrees / 2;
        return (int) (minDegrees + spanDegrees * ratio);
    }

    private int scaleDegree(int degree, int minPixel, int maxPixel, int centerDegree, int spanDegrees) {
        int minDegree = centerDegree - spanDegrees / 2;
        int offsetDegrees = degree - minDegree;
        double ratio = offsetDegrees / ((double) spanDegrees);
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

        GeoPoint mouseGeoPoint = getProjection().fromPixels((int) event.getX(), (int) event.getY());
        int diffX = 0;
        int diffY = 0;
        if (mouseDownOnMe) {
            diffX = (int) event.getX() - lastTouchEventPoint.x;
            diffY = (int) event.getY() - lastTouchEventPoint.y;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mouseDownOnMe = true;
                mouseDownCenter = getMapCenter();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mouseDownOnMe) {
                    moveByPixels(-diffX, -diffY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mouseDownOnMe) {
                    moveByPixels(-diffX, -diffY);
                    mouseDownOnMe = false;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                getController().setCenter(mouseDownCenter);
                mouseDownOnMe = false;
                break;
        }

        lastTouchEventPoint = new Point((int) event.getX(), (int) event.getY());

        return super.dispatchTouchEvent(event);
    }

    private void moveByPixels(int x, int y) {
        Point center = getProjection().toPixels(mapCenter, null);
        center.offset(x, y);
        mapCenter = getProjection().fromPixels(center.x, center.y);
    }
}
