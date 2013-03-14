package org.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.junit.Before;
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

    @Test
    public void getLayoutParamsShouldReturnInstanceOfMarginLayoutParams() {
        FrameLayout frameLayout = new FrameLayout(Robolectric.application);
        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        assertThat(layoutParams).isInstanceOf(ViewGroup.MarginLayoutParams.class);
    }

    @Test
    public void getLayoutParams_shouldReturnFrameLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams = new FrameLayout(Robolectric.application).getLayoutParams();

        assertThat(layoutParams).isInstanceOf(FrameLayout.LayoutParams.class);
    }

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
