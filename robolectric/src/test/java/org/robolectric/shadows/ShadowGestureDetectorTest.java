package org.robolectric.shadows;

import static androidx.test.core.view.MotionEventBuilder.newBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowGestureDetectorTest {

  private GestureDetector detector;
  private MotionEvent motionEvent;
  private Application context;

  @Before
  public void setUp() throws Exception {
    detector = new GestureDetector(new TestOnGestureListener());
    motionEvent = newBuilder().setAction(MotionEvent.ACTION_UP).setPointer(100, 30).build();
    context = ApplicationProvider.getApplicationContext();
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
    GestureDetector newDetector = new GestureDetector(context, new TestOnGestureListener());
    assertNotSame(newDetector, ShadowGestureDetector.getLastActiveDetector());
    newDetector.onTouchEvent(motionEvent);
    assertSame(newDetector, ShadowGestureDetector.getLastActiveDetector());
  }

  @Test
  public void getOnDoubleTapListener_shouldReturnSetDoubleTapListener() throws Exception {
    GestureDetector subject = new GestureDetector(context, new TestOnGestureListener());
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
    assertNull(shadowOf(subject).getOnDoubleTapListener());
  }

  @Test
  public void getOnDoubleTapListener_shouldReturnOnGestureListenerFromConstructor() throws Exception {
    GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener();
    GestureDetector subject = new GestureDetector(context, onGestureListener);
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
