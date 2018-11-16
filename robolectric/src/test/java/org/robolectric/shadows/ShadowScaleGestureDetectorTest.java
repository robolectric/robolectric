package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.robolectric.Shadows.shadowOf;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowScaleGestureDetectorTest {

  private ScaleGestureDetector detector;
  private MotionEvent motionEvent;

  @Before
  public void setUp() throws Exception {
    detector = new ScaleGestureDetector(ApplicationProvider.getApplicationContext(), null);
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
    assertThat(2.0f).isEqualTo(detector.getScaleFactor());
  }

  @Test
  public void test_getFocusXY() throws Exception {
    shadowOf(detector).setFocusXY(2.0f, 3.0f);
    assertThat(2.0f).isEqualTo(detector.getFocusX());
    assertThat(3.0f).isEqualTo(detector.getFocusY());
  }

  @Test
  public void test_getListener() throws Exception {
    TestOnGestureListener listener = new TestOnGestureListener();
    assertSame(
        listener,
        shadowOf(new ScaleGestureDetector(ApplicationProvider.getApplicationContext(), listener))
            .getListener());
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
    assertThat(1f).isEqualTo(detector.getScaleFactor());
    assertThat(0f).isEqualTo(detector.getFocusX());
    assertThat(0f).isEqualTo(detector.getFocusY());
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
