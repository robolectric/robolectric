package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.xtremelabs.droidsugar.AndroidTranslatorTest;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
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
    public void getProjection_shouldPerformCorrectTranslations() throws Exception {

    }

    @Test
    public void shouldPositionCaptionAboveTappedPin() throws Exception {
        mapView.getController().setCenter(new GeoPoint(toE6(10), toE6(15)));
        mapView.getController().zoomToSpan(toE6(20), toE6(30));
        mapView.layout(0, 0, 600, 400);
//        fakeMapView.height = 400;
//        fakeMapView.width = 600;
//
//        captionLayoutParams.height = 5;
//        captionLayoutParams.width = 10;
//
//        proxyFor(pinDrawable).bounds = new Rect(0, 0, 5, 10);
//
//        overlay = new ListingsPinOverlay(mapView, pinDrawable, captionViewWrapper, captionView, listener);
//        overlay.setListings(listings.iterator());
//
//        overlay.onTap(0);
//        // bottom of pin (at middle of the MapView), minus 10 for pin height, minus 5 for padding between pin and caption, minus 5 for caption height
//        int centerOfMap = fakeMapView.height / 2;
//        int pinHeight = pinDrawable.getBounds().height();
//        int captionHeight = captionLayoutParams.height;
//        int pinCaptionPadding = 5;
//        expect(captionViewWrapper.getPaddingTop()).toEqual(centerOfMap - pinHeight - pinCaptionPadding - captionHeight);
//        expect(captionViewWrapper.getPaddingLeft()).toEqual(295);
//
//        overlay.onTap(1);
//        expect(captionViewWrapper.getPaddingTop()).toEqual(200);
//        expect(captionViewWrapper.getPaddingLeft()).toEqual(275);
    }

    public static int toE6(double d) {
        return (int) (d * 1e6);
    }

    public static double fromE6(int i) {
        return i / 1e6;
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
