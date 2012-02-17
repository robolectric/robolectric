package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.Color;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class ColorTest {
    @Test
    public void testRgb() {
        int color = Color.rgb(160, 160, 160);
        assertThat(color, is(-6250336));
    }
    
    @Test
    public void testArgb() {
        int color = Color.argb(100, 160, 160, 160);
        assertThat(color, is(1688248480));
    }
}