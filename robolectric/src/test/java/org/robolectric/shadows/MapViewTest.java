package org.robolectric.shadows;

import android.app.Activity;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.bytecode.ShadowingTest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.shadows.ShadowMapView.toE6;

@RunWith(TestRunners.WithDefaults.class)
public class MapViewTest {
  private MapView mapView;
  private MyOverlay overlay1;
  private MyOverlay overlay2;
  private MotionEvent sourceEvent;
  private MyOnTouchListener mapTouchListener;


  @Before public void setUp() throws Exception {
    mapView = new MapView(new Activity(), "foo");

    overlay1 = new MyOverlay();
    overlay2 = new MyOverlay();
    mapTouchListener = new MyOnTouchListener();

    mapView.getOverlays().add(overlay1);
    mapView.getOverlays().add(overlay2);
    mapView.setOnTouchListener(mapTouchListener);

    sourceEvent = MotionEvent.obtain(0, 0, 0, 0, 0, 0);
  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void shouldDispatchTouchEventsToOverlays() throws Exception {
    mapView.dispatchTouchEvent(sourceEvent);

    assertThat(overlay1.lastMotionEvent).isSameAs(sourceEvent);
    assertThat(overlay2.lastMotionEvent).isNull();
    assertThat(mapTouchListener.lastMotionEvent).isNull();
  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void shouldDispatchTouchEventsToOverlaysUntilEventIsConsumed() throws Exception {
    overlay1.shouldConsumeEvent = false;
    overlay2.shouldConsumeEvent = true;

    mapView.dispatchTouchEvent(sourceEvent);

    assertThat(overlay1.lastMotionEvent).isSameAs(sourceEvent);
    assertThat(overlay2.lastMotionEvent).isSameAs(sourceEvent);
    assertThat(mapTouchListener.lastMotionEvent).isNull();
  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void shouldDispatchTouchEventsToMapViewIfNoOverlayConsumesEvent() throws Exception {
    overlay1.shouldConsumeEvent = false;
    overlay2.shouldConsumeEvent = false;

    mapView.dispatchTouchEvent(sourceEvent);

    assertThat(overlay1.lastMotionEvent).isSameAs(sourceEvent);
    assertThat(overlay2.lastMotionEvent).isSameAs(sourceEvent);
    assertThat(mapTouchListener.lastMotionEvent).isSameAs(sourceEvent);
  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void dispatchTouchEvents_shouldDragMapByCorrectAmount() throws Exception {
    initMapForDrag();

    dispatchTouchEvent(MotionEvent.ACTION_DOWN, 10, 10);
    dispatchTouchEvent(MotionEvent.ACTION_UP, 11, 11);
    assertThat(mapView.getMapCenter()).isEqualTo(new GeoPoint(toE6(26), toE6(24)));
  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void dispatchTouchEvents_shouldDragMapByCorrectAmountInMultipleSteps() throws Exception {
    initMapForDrag();

    dispatchTouchEvent(MotionEvent.ACTION_DOWN, 10, 10);
    dispatchTouchEvent(MotionEvent.ACTION_MOVE, 11, 11);
    dispatchTouchEvent(MotionEvent.ACTION_MOVE, 12, 12);
    dispatchTouchEvent(MotionEvent.ACTION_UP, 11, 11);
    assertThat(mapView.getMapCenter()).isEqualTo(new GeoPoint(toE6(26), toE6(24)));
  }

  @Test
  public void dispatchTouchEvents_shouldMoveBackToCenterOnCancel() throws Exception {
    initMapForDrag();

    dispatchTouchEvent(MotionEvent.ACTION_DOWN, 10, 10);
    dispatchTouchEvent(MotionEvent.ACTION_MOVE, 11, 11);
    dispatchTouchEvent(MotionEvent.ACTION_MOVE, 12, 12);
    dispatchTouchEvent(MotionEvent.ACTION_CANCEL, 11, 11);
    assertThat(mapView.getMapCenter()).isEqualTo(new GeoPoint(toE6(25), toE6(25)));
  }

  private void initMapForDrag() {
    mapView = new MapView(Robolectric.application, "");
    mapView.layout(0, 0, 50, 50);
    mapView.getController().setCenter(new GeoPoint(toE6(25), toE6(25)));
    mapView.getController().zoomToSpan(toE6(50), toE6(50));
  }

  @Test
  public void getProjection_fromPixels_shouldPerformCorrectTranslations() throws Exception {
    int centerLat = 11;
    int centerLng = 16;
    int spanLat = 20;
    int spanLng = 30;

    mapView.getController().setCenter(new GeoPoint(toE6(centerLat), toE6(centerLng)));
    mapView.getController().zoomToSpan(toE6(spanLat), toE6(spanLng));
    mapView.layout(50, 60, 650, 460);

    assertThat(mapView.getProjection().fromPixels(50, 60)).isEqualTo(new GeoPoint(toE6(21), toE6(1)));
    assertThat(mapView.getProjection().fromPixels(350, 260)).isEqualTo(new GeoPoint(toE6(11), toE6(16)));
    assertThat(mapView.getProjection().fromPixels(650, 460)).isEqualTo(new GeoPoint(toE6(1), toE6(31)));
  }

  @Test
  public void getProjection_toPixels_shouldPerformCorrectTranslations() throws Exception {
    int centerLat = 11;
    int centerLng = 16;
    int spanLat = 20;
    int spanLng = 30;

    mapView.getController().setCenter(new GeoPoint(toE6(centerLat), toE6(centerLng)));
    mapView.getController().zoomToSpan(toE6(spanLat), toE6(spanLng));
    mapView.layout(50, 60, 650, 460);

    assertThat(mapView.getProjection().toPixels(new GeoPoint(toE6(21), toE6(1)), null)).isEqualTo(new Point(50, 60));
    assertThat(mapView.getProjection().toPixels(new GeoPoint(toE6(11), toE6(16)), null)).isEqualTo(new Point(350, 260));
    assertThat(mapView.getProjection().toPixels(new GeoPoint(toE6(1), toE6(31)), null)).isEqualTo(new Point(650, 460));
  }

  private void dispatchTouchEvent(int action, int x, int y) {
    mapView.dispatchTouchEvent(MotionEvent.obtain(0, 0, action, x, y, 0));
  }

  public static class MyOnTouchListener implements View.OnTouchListener {
    private MotionEvent lastMotionEvent;

    @Override public boolean onTouch(View v, MotionEvent event) {
      lastMotionEvent = event;
      return false;
    }
  }

  private class MyOverlay extends ShadowingTest.ItemizedOverlayForTests {
    private MotionEvent lastMotionEvent;
    private boolean shouldConsumeEvent = true;

    public MyOverlay() {
      super(null);
    }

    @Override public boolean onTouchEvent(MotionEvent motionEvent, MapView mapView) {
      lastMotionEvent = motionEvent;
      return shouldConsumeEvent;
    }
  }
}
