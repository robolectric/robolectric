package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowFrameLayoutTest {

  private FrameLayout frameLayout;

  @Before
  public void setUp() throws Exception {
    frameLayout = new FrameLayout(RuntimeEnvironment.application);
  }

  @Test
  public void testNotNull() {
    assertNotNull(frameLayout);
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
