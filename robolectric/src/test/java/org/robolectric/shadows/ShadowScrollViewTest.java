package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;

import android.widget.ScrollView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class ShadowScrollViewTest {
  @Test
  public void shouldSmoothScrollTo() throws Exception {
    ScrollView scrollView = new ScrollView(RuntimeEnvironment.application);
    scrollView.smoothScrollTo(7, 6);

    assertEquals(7, scrollView.getScrollX());
    assertEquals(6, scrollView.getScrollY());
  }

  @Test
  public void shouldSmoothScrollBy() throws Exception {
    ScrollView scrollView = new ScrollView(RuntimeEnvironment.application);
    scrollView.smoothScrollTo(7, 6);
    scrollView.smoothScrollBy(10, 20);

    assertEquals(17, scrollView.getScrollX());
    assertEquals(26, scrollView.getScrollY());
  }
}
