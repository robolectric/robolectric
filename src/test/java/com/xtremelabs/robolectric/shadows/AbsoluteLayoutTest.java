package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AbsoluteLayoutTest {
    @Test
    public void getLayoutParams_shouldReturnAbsoluteLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams = new AbsoluteLayout(null).getLayoutParams();

        assertThat(layoutParams, instanceOf(AbsoluteLayout.LayoutParams.class));
    }
}
