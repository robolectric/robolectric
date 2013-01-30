package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.widget.ZoomButtonsController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.RobolectricForMaps.shadowOf;

/**
 * Shadow of {@code MapView} that simulates the internal state of a {@code MapView}. Supports {@code Projection}s,
 * {@code Overlay}s, and {@code TouchEvent}s
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapView.class)
public class ShadowMapView extends ShadowViewGroup {
    private boolean satelliteOn;
    private MapController mapController;
    private List<Overlay> overlays = new ArrayList<Overlay>();
    GeoPoint mapCenter = new GeoPoint(10, 10);
    int longitudeSpan = 20;
    int latitudeSpan = 30;
    int zoomLevel = 1;
    private ShadowMapController shadowMapController;
    private ZoomButtonsController zoomButtonsController;
    private MapView realMapView;
    private Projection projection;
    private boolean useBuiltInZoomMapControls;
    private boolean mouseDownOnMe = false;
    private Point lastTouchEventPoint;
    private GeoPoint mouseDownCenter;
    private boolean preLoadWasCalled;
    private boolean canCoverCenter = true;

    public ShadowMapView(MapView mapView) {
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

    @Implementation
    public void setSatellite(boolean satelliteOn) {
        this.satelliteOn = satelliteOn;
    }

    @Implementation
    public boolean isSatellite() {
        return satelliteOn;
    }

    @Implementation
    public boolean canCoverCenter() {
        return canCoverCenter;
    }

    @Implementation
    public MapController getController() {
        if (mapController == null) {
            try {
                mapController = Robolectric.newInstanceOf(MapController.class);
                shadowMapController = shadowOf(mapController);
                shadowMapController.setShadowMapView(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return mapController;
    }

    @Implementation
    public ZoomButtonsController getZoomButtonsController() {
        return zoomButtonsController;
    }

    @Implementation
    public void setBuiltInZoomControls(boolean useBuiltInZoomMapControls) {
        this.useBuiltInZoomMapControls = useBuiltInZoomMapControls;
    }

    @Implementation
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

    @Implementation
    public List<Overlay> getOverlays() {
        return overlays;
    }

    @Implementation
    public GeoPoint getMapCenter() {
        return mapCenter;
    }

    @Implementation
    public int getLatitudeSpan() {
        return latitudeSpan;
    }

    @Implementation
    public int getLongitudeSpan() {
        return longitudeSpan;
    }

    @Implementation
    public int getZoomLevel() {
        return zoomLevel;
    }

    @Implementation
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

    @Implementation
    public void preLoad() {
        preLoadWasCalled = true;
    }

    private void moveByPixels(int x, int y) {
        Point center = getProjection().toPixels(mapCenter, null);
        center.offset(x, y);
        mapCenter = getProjection().fromPixels(center.x, center.y);
    }

    /**
     * Non-Android accessor.
     *
     * @return whether to use built in zoom map controls
     */
    public boolean getUseBuiltInZoomMapControls() {
        return useBuiltInZoomMapControls;
    }

    /**
     * Non-Android accessor.
     *
     * @return whether {@link #preLoad()} has been called on this {@code MapView}
     */
    public boolean preLoadWasCalled() {
        return preLoadWasCalled;
    }

    /**
     * Non-Android accessor to set the latitude span (the absolute value of the difference between the Northernmost and
     * Southernmost latitudes visible on the map) of this {@code MapView}
     *
     * @param latitudeSpan the new latitude span for this {@code MapView}
     */
    public void setLatitudeSpan(int latitudeSpan) {
        this.latitudeSpan = latitudeSpan;
    }

    /**
     * Non-Android accessor to set the longitude span (the absolute value of the difference between the Easternmost and
     * Westernmost longitude visible on the map) of this {@code MapView}
     *
     * @param longitudeSpan the new latitude span for this {@code MapView}
     */
    public void setLongitudeSpan(int longitudeSpan) {
        this.longitudeSpan = longitudeSpan;
    }

    /**
     * Non-Android accessor that controls the value to be returned by {@link #canCoverCenter()}
     *
     * @param canCoverCenter the value to be returned by {@link #canCoverCenter()}
     */
    public void setCanCoverCenter(boolean canCoverCenter) {
        this.canCoverCenter = canCoverCenter;
    }
}
