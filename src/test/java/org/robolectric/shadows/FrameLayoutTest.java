package org.robolectric.shadows;

import android.view.View;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * {@link ShadowFrameLayout} test suite.
 */
@RunWith(TestRunners.WithDefaults.class)
public class FrameLayoutTest {

  private FrameLayout frameLayout;

  @Before
  public void setUp() throws Exception {
    frameLayout = new FrameLayout(Robolectric.application);
  }

  @Test
  public void testNotNull() {
    assertNotNull(frameLayout);
  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void test_measuredDimension() {
    assertThat(frameLayout.getMeasuredHeight()).isEqualTo(0);
    assertThat(frameLayout.getMeasuredWidth()).isEqualTo(0);

    frameLayout.measure(View.MeasureSpec.makeMeasureSpec(150, View.MeasureSpec.AT_MOST),
        View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.AT_MOST));

    assertThat(frameLayout.getMeasuredHeight()).isEqualTo(300);
    assertThat(frameLayout.getMeasuredWidth()).isEqualTo(150);
  }

  @Test
  public void onMeasure_shouldNotLayout() throws Exception {
    assertThat(frameLayout.getHeight()).isEqualTo(0);
    assertThat(frameLayout.getWidth()).isEqualTo(0);

    frameLayout.measure(View.MeasureSpec.makeMeasureSpec(150, View.MeasureSpec.AT_MOST),
        View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.AT_MOST));

    assertThat(frameLayout.getHeight()).isEqualTo(0);
    assertThat(frameLayout.getWidth()).isEqualTo(0);
  }
}
