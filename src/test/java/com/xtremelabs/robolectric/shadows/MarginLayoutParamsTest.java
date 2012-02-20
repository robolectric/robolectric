package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import android.view.ViewGroup;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * {@link ShadowMarginLayoutParams} test suite.
 */
@RunWith(WithTestDefaultsRunner.class)
public class MarginLayoutParamsTest  {

    @Test
    public void testSetMargins() {
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(0, 0);
        marginLayoutParams.setMargins(1, 2, 3, 4);
        assertThat(marginLayoutParams.leftMargin, equalTo(1));
        assertThat(marginLayoutParams.topMargin, equalTo(2));
        assertThat(marginLayoutParams.rightMargin, equalTo(3));
        assertThat(marginLayoutParams.bottomMargin, equalTo(4));
    }
}
