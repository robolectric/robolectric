package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * {@link ShadowFrameLayout} test suite.
 */
@RunWith(WithTestDefaultsRunner.class)
public class FrameLayoutTest {

    private FrameLayout frameLayout;

    @Before
    public void setUp() throws Exception {
        frameLayout = new FrameLayout(null);
    }

    @Test
    public void testNotNull() {
        assertNotNull(frameLayout);
    }

    @Test
    public void getLayoutParamsShouldReturnInstanceOfMarginLayoutParams() {
        FrameLayout frameLayout = new FrameLayout(null);
        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        assertThat(layoutParams, instanceOf(ViewGroup.MarginLayoutParams.class));
    }

    @Test
    public void test_measuredDimension() {
        MatcherAssert.assertThat(frameLayout.getMeasuredHeight(), equalTo(0));
        MatcherAssert.assertThat(frameLayout.getMeasuredWidth(), equalTo(0));

        frameLayout.measure(View.MeasureSpec.makeMeasureSpec(150, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.AT_MOST));

        MatcherAssert.assertThat(frameLayout.getMeasuredHeight(), equalTo(300));
        MatcherAssert.assertThat(frameLayout.getMeasuredWidth(), equalTo(150));
    }

    @Test
    public void onMeasure_shouldNotLayout() throws Exception {
        MatcherAssert.assertThat(frameLayout.getHeight(), equalTo(0));
        MatcherAssert.assertThat(frameLayout.getWidth(), equalTo(0));

        frameLayout.measure(View.MeasureSpec.makeMeasureSpec(150, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.AT_MOST));

        MatcherAssert.assertThat(frameLayout.getHeight(), equalTo(0));
        MatcherAssert.assertThat(frameLayout.getWidth(), equalTo(0));
    }
}
