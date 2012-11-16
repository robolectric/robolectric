package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.TestRunners;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * {@link ShadowFrameLayout} test suite.
 */
@RunWith(TestRunners.WithDefaults.class)
public class FrameLayoutTest {

    @Test
    public void testNotNull() {
        FrameLayout frameLayout = new FrameLayout(null);
        assertNotNull(frameLayout);
    }

    @Test
    public void getLayoutParamsShouldReturnInstanceOfMarginLayoutParams() {
        FrameLayout frameLayout = new FrameLayout(null);
        ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
        assertThat(layoutParams, instanceOf(ViewGroup.MarginLayoutParams.class));
    }

    @Test
    public void getLayoutParams_shouldReturnFrameLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams = new FrameLayout(null).getLayoutParams();

        assertThat(layoutParams, instanceOf(FrameLayout.LayoutParams.class));
    }
}
