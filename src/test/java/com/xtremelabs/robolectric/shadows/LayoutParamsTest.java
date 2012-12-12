package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Gallery;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class LayoutParamsTest {
    @Test
    public void testConstructor() throws Exception {
        Gallery.LayoutParams layoutParams = new Gallery.LayoutParams(123, 456);
        assertThat(layoutParams.width, equalTo(123));
        assertThat(layoutParams.height, equalTo(456));
    }
    
    @Test
    public void constructor_canTakeSourceLayoutParams() throws Exception {
        ViewGroup.LayoutParams sourceLayoutParams = new ViewGroup.LayoutParams(123, 456);
        ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(sourceLayoutParams);
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(sourceLayoutParams);
        assertThat(layoutParams1.height, equalTo(456));
        assertThat(layoutParams1.width, equalTo(123));
        assertThat(layoutParams2.height, equalTo(456));
        assertThat(layoutParams1.width, equalTo(123));
    }
}
