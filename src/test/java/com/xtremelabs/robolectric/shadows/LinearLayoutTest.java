package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class LinearLayoutTest {
    @Test
    public void getLayoutParams_shouldReturnLinearLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams = new LinearLayout(null).getLayoutParams();

        assertThat(layoutParams, instanceOf(LinearLayout.LayoutParams.class));
    }
}
