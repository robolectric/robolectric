package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.robolectric.Shadows.shadowOf;

import android.view.GestureDetector;
import android.view.MotionEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
public class ShadowGestureDetectorTest {

  private GestureDetector detector;
  private MotionEvent motionEvent;

  @Before
  public void setUp() throws Exception {
    detector = new GestureDetector(new TestOnGestureListener());
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
  public void test_getListener() throws Exception {
    TestOnGestureListener listener = new TestOnGestureListener();
    assertSame(listener, shadowOf(new GestureDetector(listener)).getListener());
    assertSame(listener, shadowOf(new GestureDetector(null, listener)).getListener());
  }

  @Test
  public void canAnswerLastGestureDetector() throws Exception {
    GestureDetector newDetector = new GestureDetector(RuntimeEnvironment.application, new TestOnGestureListener());
    assertNotSame(newDetector, ShadowGestureDetector.getLastActiveDetector());
    newDetector.onTouchEvent(Shadow.newInstanceOf(MotionEvent.class));
    assertSame(newDetector, ShadowGestureDetector.getLastActiveDetector());
  }

  @Test
  public void getOnDoubleTapListener_shouldReturnSetDoubleTapListener() throws Exception {
    GestureDetector subject = new GestureDetector(RuntimeEnvironment.application, new TestOnGestureListener());
    GestureDetector.OnDoubleTapListener onDoubleTapListener = new GestureDetector.OnDoubleTapListener() {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
      }

      @Override
      public boolean onDoubleTap(MotionEvent e) {
        return false;
      }

      @Override
      public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
      }
    };

    subject.setOnDoubleTapListener(onDoubleTapListener);
    assertEquals(shadowOf(subject).getOnDoubleTapListener(), onDoubleTapListener);

    subject.setOnDoubleTapListener(null);
    assertEquals(shadowOf(subject).getOnDoubleTapListener(), null);
  }

  @Test
  public void getOnDoubleTapListener_shouldReturnOnGestureListenerFromConstructor() throws Exception {
    GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener();
    GestureDetector subject = new GestureDetector(RuntimeEnvironment.application, onGestureListener);
    assertEquals(shadowOf(subject).getOnDoubleTapListener(), onGestureListener);
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
