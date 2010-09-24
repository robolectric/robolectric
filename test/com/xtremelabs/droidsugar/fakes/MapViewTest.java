package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.xtremelabs.droidsugar.AndroidTranslatorTest;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.droidsugar.fakes.FakeMapView.toE6;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class MapViewTest {
    private MapView mapView;
    private MyOverlay overlay1;
    private MyOverlay overlay2;
    private MotionEvent sourceEvent;
    private MyOnTouchListener mapTouchListener;


    @Before public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addGenericProxies();
        
        mapView = new MapView(new Activity(), "foo");

        overlay1 = new MyOverlay();
        overlay2 = new MyOverlay();
        mapTouchListener = new MyOnTouchListener();

        mapView.getOverlays().add(overlay1);
        mapView.getOverlays().add(overlay2);
        mapView.setOnTouchListener(mapTouchListener);

        sourceEvent = MotionEvent.obtain(0, 0, 0, 0, 0, 0);
    }

    @Test
    public void shouldDispatchTouchEventsToOverlays() throws Exception {
        mapView.dispatchTouchEvent(sourceEvent);

        assertThat(overlay1.lastMotionEvent, sameInstance(sourceEvent));
        assertThat(overlay2.lastMotionEvent, nullValue());
        assertThat(mapTouchListener.lastMotionEvent, nullValue());
    }

    @Test
    public void shouldDispatchTouchEventsToOverlaysUntilEventIsConsumed() throws Exception {
        overlay1.shouldConsumeEvent = false;
        overlay2.shouldConsumeEvent = true;

        mapView.dispatchTouchEvent(sourceEvent);

        assertThat(overlay1.lastMotionEvent, sameInstance(sourceEvent));
        assertThat(overlay2.lastMotionEvent, sameInstance(sourceEvent));
        assertThat(mapTouchListener.lastMotionEvent, nullValue());
    }

    @Test
    public void shouldDispatchTouchEventsToMapViewIfNoOverlayConsumesEvent() throws Exception {
        overlay1.shouldConsumeEvent = false;
        overlay2.shouldConsumeEvent = false;

        mapView.dispatchTouchEvent(sourceEvent);

        assertThat(overlay1.lastMotionEvent, sameInstance(sourceEvent));
        assertThat(overlay2.lastMotionEvent, sameInstance(sourceEvent));
        assertThat(mapTouchListener.lastMotionEvent, sameInstance(sourceEvent));
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

        assertThat(mapView.getProjection().fromPixels(50, 60),
                equalTo(new GeoPoint(toE6(centerLat - spanLat / 2), toE6(centerLng - spanLng / 2))));
        assertThat(mapView.getProjection().fromPixels(350, 260),
                equalTo(new GeoPoint(toE6(centerLat), toE6(centerLng))));
        assertThat(mapView.getProjection().fromPixels(650, 460),
                equalTo(new GeoPoint(toE6(centerLat + spanLat / 2), toE6(centerLng + spanLng / 2))));
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

        assertThat(mapView.getProjection().toPixels(new GeoPoint(toE6(centerLat - spanLat / 2), toE6(centerLng - spanLng / 2)), null),
                equalTo(new Point(50, 60)));
        assertThat(mapView.getProjection().toPixels(new GeoPoint(toE6(centerLat), toE6(centerLng)), null),
                equalTo(new Point(350, 260)));
        assertThat(mapView.getProjection().toPixels(new GeoPoint(toE6(centerLat + spanLat / 2), toE6(centerLng + spanLng / 2)), null),
                equalTo(new Point(650, 460)));
    }

    private static class MyOnTouchListener implements View.OnTouchListener {
        private MotionEvent lastMotionEvent;

        @Override public boolean onTouch(View v, MotionEvent event) {
            lastMotionEvent = event;
            return false;
        }
    }

    private class MyOverlay extends AndroidTranslatorTest.FakeItemizedOverlayForTests {
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
