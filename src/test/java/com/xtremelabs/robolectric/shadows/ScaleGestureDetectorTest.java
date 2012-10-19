package com.xtremelabs.robolectric.shadows;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;

@RunWith(WithTestDefaultsRunner.class)
public class ScaleGestureDetectorTest {

    private ScaleGestureDetector detector;
    private MotionEvent motionEvent;

    @Before
    public void setUp() throws Exception {
        detector = new ScaleGestureDetector(null, null);
        motionEvent = MotionEvent.obtain(-1, -1, MotionEvent.ACTION_UP, 100, 30, -1);
    }

    @Test
    public void test_getOnTouchEventMotionEvent() throws Exception {
        detector.onTouchEvent(motionEvent);
        assertSame(motionEvent, shadowOf(detector).getOnTouchEventMotionEvent());
    }

    @Test
    public void test_getScaleFactor() throws Exception {
        shadowOf(detector).setScaleFactor(2.0f);
        assertEquals(detector.getScaleFactor(), 2.0f);
    }

    @Test
    public void test_getFocusXY() throws Exception {
        shadowOf(detector).setFocusXY(2.0f, 3.0f);
        assertEquals(detector.getFocusX(), 2.0f);
        assertEquals(detector.getFocusY(), 3.0f);
    }

    @Test
    public void test_getListener() throws Exception {
        TestOnGestureListener listener = new TestOnGestureListener();
        assertSame(listener, shadowOf(new ScaleGestureDetector(null, listener)).getListener());
    }

    @Test
    public void test_reset() throws Exception {
        assertDefaults();

        detector.onTouchEvent(motionEvent);
        shadowOf(detector).setFocusXY(3f, 3f);
        shadowOf(detector).setScaleFactor(4f);
        assertSame(motionEvent, shadowOf(detector).getOnTouchEventMotionEvent());

        shadowOf(detector).reset();

        assertDefaults();
    }

    private void assertDefaults() {
        assertNull(shadowOf(detector).getOnTouchEventMotionEvent());
        assertEquals(detector.getScaleFactor(), 1f);
        assertEquals(detector.getFocusX(), 0f);
        assertEquals(detector.getFocusY(), 0f);
    }

    private static class TestOnGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }
}
