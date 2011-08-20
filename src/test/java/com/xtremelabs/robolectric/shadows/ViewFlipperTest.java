package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.widget.ViewFlipper;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ViewFlipperTest {
    protected ViewFlipper flipper;

    @Before
    public void setUp() {
        flipper = new ViewFlipper(new Activity());
    }

    @Test
    public void testStartFlipping() {
        flipper.startFlipping();
        assertEquals("flipping", true, flipper.isFlipping());
    }

    @Test
    public void testStopFlipping() {
        flipper.stopFlipping();
        assertEquals("flipping", false, flipper.isFlipping());
    }
}
