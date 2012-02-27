package com.xtremelabs.robolectric.shadows;

import android.text.TextPaint;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;


@RunWith(WithTestDefaultsRunner.class)
public class TextPaintTest {
    
    @Test
    public void measureText_returnsStringLengthAsMeasurement() throws Exception {
        TextPaint paint = new TextPaint();
        assertEquals(4f, paint.measureText("1234"));
    }
}
