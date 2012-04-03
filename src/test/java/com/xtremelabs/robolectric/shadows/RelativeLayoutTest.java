package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class RelativeLayoutTest {
    @Test
    public void getLayoutParams_shouldReturnRelativeLayoutParams() throws Exception {
        ViewGroup.LayoutParams layoutParams = new RelativeLayout(null).getLayoutParams();

        assertThat(layoutParams, instanceOf(RelativeLayout.LayoutParams.class));
    }

    @Test
    public void getLayoutParams_shouldReturnTheSameRelativeLayoutParamsFromTheSetter() throws Exception {
    	RelativeLayout relativeLayout = new RelativeLayout(null);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1, 2);

        relativeLayout.setLayoutParams(params);

        assertTrue(relativeLayout.getLayoutParams() == params);
    }
}
