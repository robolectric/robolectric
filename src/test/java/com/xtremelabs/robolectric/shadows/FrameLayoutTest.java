package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class FrameLayoutTest {
    @Test
    public void getLayoutParams_shouldReturnFrameLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams = new FrameLayout(null).getLayoutParams();

        assertThat(layoutParams, instanceOf(FrameLayout.LayoutParams.class));
    }
}
