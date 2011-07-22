package com.xtremelabs.robolectric.res;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

/**
 * DrawableFactoryTest
 */
@RunWith(WithTestDefaultsRunner.class)
public class DrawableFactoryTest {
    /** key */
    protected final static int KEY = 0x00000013;

    /** drawable factory */
    protected DrawableFactory factory;

    /**
     * Setup drawable factory.
     * @throws Exception 
     */
    @Before
    public void setup() throws Exception {
        ResourceExtractor extractor = new ResourceExtractor();
        extractor.addLocalRClass(R.class);

        factory = new DrawableFactory(extractor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDrawable_String_invalid() {
        factory.getDrawable("fake/key");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDrawable_int_invalid() {
        factory.getDrawable(-KEY);
    }

    @Test
    public void testGetDrawable_cached() {
        Drawable value = new LayerDrawable(new Drawable[0]);
        factory.cacheById.put(KEY, value);
        Drawable result = factory.getDrawable(KEY);
        assertSame("result", value, result);
    }

    @Test
    public void testContains_true() {
        factory.cacheById.put(KEY, new LayerDrawable(new Drawable[0]));
        boolean result = factory.contains(KEY);
        assertEquals("result", true, result);
    }

    @Test
    public void testContains_false() {
        boolean result = factory.contains(KEY);
        assertEquals("result", false, result);
    }

    @Test
    public void testHit() {
        Drawable value = new LayerDrawable(new Drawable[0]);
        factory.cacheById.put(KEY, value);
        Drawable result = factory.hit(KEY);
        assertSame("result", result, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMiss_bad_id() {
        factory.getDrawable(-KEY);
    }
}
