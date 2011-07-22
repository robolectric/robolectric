package com.xtremelabs.robolectric.res.drawable;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.res.ResourceExtractor;

/**
 * DrawableBuilderTest
 */
@RunWith(WithTestDefaultsRunner.class)
public class DrawableBuilderTest {
    /** builder */
    protected DrawableBuilder builder;

    /**
     * Setup builder.
     * @throws Exception 
     */
    @Before
    public void setup() throws Exception {
        ResourceExtractor extractor = new ResourceExtractor();
        extractor.addLocalRClass(R.class);
        DrawableFactory drawableFactory = new DrawableFactory(extractor);
        builder = new DrawableBuilder(drawableFactory);
    }

    @Test
    public void testBuild_bitmap() {
        Drawable result = builder.build(R.drawable.l0_red);
        assertNotNull("result", result);
        assertEquals("BitmapDrawable", true, result instanceof BitmapDrawable);
    }

    @Test
    public void testBuild_layer() {
        Drawable result = builder.build(R.drawable.rainbow);
        assertNotNull("result", result);
        assertEquals("LayerDrawable", true, result instanceof LayerDrawable);
    }
}
