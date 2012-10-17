package com.xtremelabs.robolectric.shadows;

import android.graphics.Color;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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

    @Test
    public void testParseColor() throws Exception {
        assertEquals(-1, Color.parseColor("#ffffffff"));
        assertEquals(0, Color.parseColor("#00000000"));
        assertEquals(-5588020, Color.parseColor("#ffaabbcc"));
    }
}