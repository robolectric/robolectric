package org.robolectric.shadows.maps;

import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.shadows.maps.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.ClassParameter;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ZoomButtonsController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowViewGroup;
import org.robolectric.util.ReflectionHelpers;

@Implements(MapView.class)
public class ShadowMapView extends ShadowViewGroup {
  @SuppressWarnings("UnusedDeclaration") @RealObject
  private MapView realMapView;
  private boolean satelliteOn;
  private MapController mapController;
  private List<Overlay> overlays = new ArrayList<>();
  GeoPoint mapCenter = new GeoPoint(10, 10);
  int longitudeSpan = 20;
  int latitudeSpan = 30;
  int zoomLevel = 1;
  private ZoomButtonsController zoomButtonsController;
  private Projection projection;
  private boolean useBuiltInZoomMapControls;
  private boolean mouseDownOnMe = false;
  private Point lastTouchEventPoint;
  private GeoPoint mouseDownCenter;
  private boolean preLoadWasCalled;
  private boolean canCoverCenter = true;

  public void __constructor__(Context context, AttributeSet attributeSet) {
    setContextOnRealView(context);
    this.attributeSet = attributeSet;
    zoomButtonsController = new ZoomButtonsController(realMapView);
    invokeConstructor(View.class, realView,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(AttributeSet.class, attributeSet),
        ClassParameter.from(int.class, 0));

    invokeConstructor(ViewGroup.class, realView,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(AttributeSet.class, attributeSet),
        ClassParameter.from(int.class, 0));
  }

  @Override public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
    setContextOnRealView(context);
    this.attributeSet = attributeSet;
    zoomButtonsController = new ZoomButtonsController(realMapView);
    invokeConstructor(View.class, realView,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(AttributeSet.class, attributeSet),
        ClassParameter.from(int.class, defStyle));

    invokeConstructor(ViewGroup.class, realView,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(AttributeSet.class, attributeSet),
        ClassParameter.from(int.class, defStyle));

    super.__constructor__(context, attributeSet, defStyle);
  }

  public static int toE6(double d) {
    return (int) (d * 0x1e6);
  }

  public static double fromE6(int i) {
    return i / 0x1e6;
  }

  @Implementation // todo 2.0-cleanup
  public boolean isOpaque() {
    return true;
  }

  @Implementation // todo 2.0-cleanup
  public void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
  }

  @Override @Implementation // todo 2.0-cleanup
  public boolean onTouchEvent(MotionEvent event) {
    return directlyOn(realView, View.class).onTouchEvent(event);
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
        mapController = Shadow.newInstanceOf(MapController.class);
        ShadowMapController shadowMapController = shadowOf(mapController);
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

          point.y = scaleDegree(geoPoint.getLatitudeE6(), realView.getBottom(), realView.getTop(), mapCenter.getLatitudeE6(), latitudeSpan);
          point.x = scaleDegree(geoPoint.getLongitudeE6(), realView.getLeft(), realView.getRight(), mapCenter.getLongitudeE6(), longitudeSpan);
          return point;
        }

        @Override public GeoPoint fromPixels(int x, int y) {
          int lat = scalePixel(y, realView.getBottom(), -realMapView.getHeight(), mapCenter.getLatitudeE6(), latitudeSpan);
          int lng = scalePixel(x, realView.getLeft(), realMapView.getWidth(), mapCenter.getLongitudeE6(), longitudeSpan);
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
  public boolean dispatchTouchEvent(MotionEvent event) {
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

    return realView.dispatchTouchEvent(event);
  }

  @Implementation
  public void preLoad() {
    preLoadWasCalled = true;
  }

  @Override @Implementation
  public void onLayout(boolean b, int i, int i1, int i2, int i3) {
  }

  private void moveByPixels(int x, int y) {
    Point center = getProjection().toPixels(mapCenter, null);
    center.offset(x, y);
    mapCenter = getProjection().fromPixels(center.x, center.y);
  }

  /**
   * @return whether to use built in zoom map controls
   */
  public boolean getUseBuiltInZoomMapControls() {
    return useBuiltInZoomMapControls;
  }

  /**
   * @return whether {@link #preLoad()} has been called on this {@code MapView}
   */
  public boolean preLoadWasCalled() {
    return preLoadWasCalled;
  }

  /**
   * Sets the latitude span (the absolute value of the difference between the Northernmost and
   * Southernmost latitudes visible on the map) of this {@code MapView}
   *
   * @param latitudeSpan the new latitude span for this {@code MapView}
   */
  public void setLatitudeSpan(int latitudeSpan) {
    this.latitudeSpan = latitudeSpan;
  }

  /**
   * Sets the longitude span (the absolute value of the difference between the Easternmost and
   * Westernmost longitude visible on the map) of this {@code MapView}
   *
   * @param longitudeSpan the new latitude span for this {@code MapView}
   */
  public void setLongitudeSpan(int longitudeSpan) {
    this.longitudeSpan = longitudeSpan;
  }

  /**
   * Controls the value to be returned by {@link #canCoverCenter()}
   *
   * @param canCoverCenter the value to be returned by {@link #canCoverCenter()}
   */
  public void setCanCoverCenter(boolean canCoverCenter) {
    this.canCoverCenter = canCoverCenter;
  }

  private void setContextOnRealView(Context context) {
    ReflectionHelpers.setField(realView, "mContext", context);
  }
}
