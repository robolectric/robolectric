package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
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
