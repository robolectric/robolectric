package org.robolectric.shadows.maps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.shadows.maps.ShadowMapView.toE6;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class ShadowMapViewTest {
  private MapView mapView;
  private MyOverlay overlay1;
  private MyOverlay overlay2;
  private MotionEvent sourceEvent;
  private MyOnTouchListener mapTouchListener;

  @Before
  public void setUp() throws Exception {
    mapView = new MapView(new Activity(), "foo");

    overlay1 = new MyOverlay(null);
    overlay2 = new MyOverlay(null);
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

  private void initMapForDrag() {
    mapView = new MapView(RuntimeEnvironment.application, "");
    mapView.layout(0, 0, 50, 50);
    mapView.getController().setCenter(new GeoPoint(toE6(25), toE6(25)));
    mapView.getController().zoomToSpan(toE6(50), toE6(50));
  }

  private void dispatchTouchEvent(int action, int x, int y) {
    mapView.dispatchTouchEvent(MotionEvent.obtain(0, 0, action, x, y, 0));
  }

  public static class MyOnTouchListener implements View.OnTouchListener {
    private MotionEvent lastMotionEvent;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      lastMotionEvent = event;
      return false;
    }
  }

  @Implements(ItemizedOverlay.class)
  public static class MyOverlay extends ItemizedOverlay {
    private MotionEvent lastMotionEvent;
    private boolean shouldConsumeEvent = true;

    public MyOverlay(Drawable drawable) {
      super(drawable);
    }

    @Override
    protected OverlayItem createItem(int i) {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent, MapView mapView) {
      lastMotionEvent = motionEvent;
      return shouldConsumeEvent;
    }
  }
}
