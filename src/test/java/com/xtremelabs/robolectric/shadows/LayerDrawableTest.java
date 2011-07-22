package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * ShadowLayerDrawableTest
 */
@RunWith(WithTestDefaultsRunner.class)
public class LayerDrawableTest {
    /** drawables */
    protected Drawable l0_red;
    protected Drawable l1_orange;
    protected Drawable l2_yellow;

    /** drawables */
    protected Drawable[] drawables;

    @Before
    public void setUp() {
        l0_red = shadowOf(Robolectric.application).getResources().getDrawable(
                R.drawable.l0_red);
        l1_orange = shadowOf(Robolectric.application).getResources()
                .getDrawable(R.drawable.l1_orange);
        l2_yellow = shadowOf(Robolectric.application).getResources()
                .getDrawable(R.drawable.l2_yellow);

        drawables = new Drawable[] { l0_red, l1_orange, l2_yellow };
    }

    @Test
    public void testConstruction() {
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        assertSame("drawables", drawables, shadowOf(layerDrawable).drawables);
    }

    @Test
    public void testGetNumberOfLayers() {
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        assertEquals("count", 3, layerDrawable.getNumberOfLayers());
    }

    @Test
    public void testSetDrawableByLayerId_not_found() {
        Drawable replacer = shadowOf(Robolectric.application).getResources()
                .getDrawable(R.drawable.l3_green);

        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        boolean result = layerDrawable.setDrawableByLayerId(R.drawable.l4_blue,
                replacer);

        assertSame("drawables[0]", l0_red, drawables[0]);
        assertSame("drawables[1]", l1_orange, drawables[1]);
        assertSame("drawables[2]", l2_yellow, drawables[2]);
        assertEquals("result", false, result);
    }

    @Test
    public void testSetDrawableByLayerId_found() {
        Drawable replacer = shadowOf(Robolectric.application).getResources()
                .getDrawable(R.drawable.l3_green);

        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        boolean result = layerDrawable.setDrawableByLayerId(
                R.drawable.l1_orange, replacer);

        assertSame("drawables[0]", l0_red, drawables[0]);
        assertSame("drawables[1]", replacer, drawables[1]);
        assertSame("drawables[2]", l2_yellow, drawables[2]);
        assertEquals("result", true, result);
    }
}
