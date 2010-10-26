package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.Gallery;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DogfoodRobolectricTestRunner.class)
public class LayoutParamsTest {
    @Test
    public void testConstructor() throws Exception {
        Robolectric.bindShadowClass(ViewGroup.LayoutParams.class, ShadowLayoutParams.class);

        Gallery.LayoutParams layoutParams = new Gallery.LayoutParams(123, 456);
        assertThat(layoutParams.width, equalTo(123));
        assertThat(layoutParams.height, equalTo(456));
    }
}
