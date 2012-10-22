package com.xtremelabs.robolectric.shadows;

import android.view.GestureDetector;
import android.view.MotionEvent;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class GestureDetectorTest {

    private GestureDetector detector;
    private MotionEvent motionEvent;

    @Before
    public void setUp() throws Exception {
        detector = new GestureDetector(null);
        motionEvent = MotionEvent.obtain(-1, -1, MotionEvent.ACTION_UP, 100, 30, -1);
    }

    @Test
    public void test_getOnTouchEventMotionEvent() throws Exception {
        detector.onTouchEvent(motionEvent);
        assertSame(motionEvent, shadowOf(detector).getOnTouchEventMotionEvent());
    }

    @Test
    public void test_reset() throws Exception {
        detector.onTouchEvent(motionEvent);
        assertSame(motionEvent, shadowOf(detector).getOnTouchEventMotionEvent());

        shadowOf(detector).reset();
        assertNull(shadowOf(detector).getOnTouchEventMotionEvent());
    }

    @Test
    public void test_setNextOnTouchEventReturnValue() throws Exception {
        assertTrue(detector.onTouchEvent(motionEvent));

        shadowOf(detector).setNextOnTouchEventReturnValue(true);
        assertTrue(detector.onTouchEvent(motionEvent));

        shadowOf(detector).setNextOnTouchEventReturnValue(false);
        assertFalse(detector.onTouchEvent(motionEvent));
    }

    @Test
    public void test_getListener() throws Exception {
        TestOnGestureListener listener = new TestOnGestureListener();
        assertSame(listener, shadowOf(new GestureDetector(listener)).getListener());
        assertSame(listener, shadowOf(new GestureDetector(null, listener)).getListener());
    }

    private static class TestOnGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
}
