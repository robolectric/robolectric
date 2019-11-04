package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowTouchDelegateTest {

  private ShadowTouchDelegate td;
  private Rect rect;
  private View view;

  @Before
  public void setUp() throws Exception {
    rect = new Rect(1, 2, 3, 4);
    view = new View(ApplicationProvider.getApplicationContext());
    TouchDelegate realTD = new TouchDelegate(rect, view);
    td = Shadows.shadowOf(realTD);
  }

  @Test
  public void testBounds() {
    Rect bounds = td.getBounds();
    assertThat(bounds).isEqualTo(rect);
  }

  @Test
  public void testDelegateView() {
    View view = td.getDelegateView();
    assertThat(view).isEqualTo(this.view);
  }

  @Test
  public void testRealObjectIsFunctional() {
    // Instantiate a TouchDelegate using the Shadow construction APIs and make sure that the
    // underlying real object's constructor gets instantiated by verifying that the returned object
    // behaves as expected.
    Rect rect = new Rect(100, 5000, 200, 6000);
    TouchDelegate td =
        Shadow.newInstance(
            TouchDelegate.class,
            new Class[] { Rect.class, View.class },
            new Object[] {rect, view});
    // Make the underlying view clickable. This ensures that if a touch event does get delegated, it
    // gets reported as having been handled.
    view.setClickable(true);

    // Verify that a touch event in the center of the rectangle is handled.
    assertThat(
        td.onTouchEvent(
            MotionEvent.obtain(1, 1, MotionEvent.ACTION_DOWN, rect.centerX(), rect.centerY(), 0)))
        .isTrue();
    // Verify that a touch event outside of the rectangle is not handled.
    assertThat(td.onTouchEvent(MotionEvent.obtain(1, 1, MotionEvent.ACTION_DOWN, 5f, 10f, 0)))
        .isFalse();
  }
}
