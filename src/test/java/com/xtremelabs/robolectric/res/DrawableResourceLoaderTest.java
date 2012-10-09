package com.xtremelabs.robolectric.res;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.ShadowStateListDrawable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.getSystemResourceDir;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * DrawableResourceLoaderTest
 */
@RunWith(WithTestDefaultsRunner.class)
public class DrawableResourceLoaderTest {
    protected DrawableResourceLoader resourceLoader;

    @Before
    public void setup() throws Exception {
        ResourceExtractor extractor = new ResourceExtractor();
        extractor.addLocalRClass(R.class);
        extractor.addSystemRClass(android.R.class);

        resourceLoader = new DrawableResourceLoader(extractor, resourceFile("res"));
        DocumentLoader documentLoader = new DocumentLoader(resourceLoader);

        documentLoader.loadResourceXmlDir(resourceFile("res", "drawable"));
        documentLoader.loadSystemResourceXmlDir(getSystemResourceDir("drawable"));
    }

    @Test
    public void testProcessResourceXml() throws Exception {
        assertTrue("drawable/rainbow", resourceLoader.documents.containsKey("drawable/rainbow"));
        assertEquals("documents.size", 218, resourceLoader.documents.size());
    }

    @Test
    public void testIsXml_rainbow() throws Exception {
        assertTrue(resourceLoader.isXml(R.drawable.rainbow));
    }

    @Test
    public void testIsXml_shouldWorkWithSystem() throws Exception {
        assertTrue(resourceLoader.isXml(android.R.drawable.ic_popup_sync));
    }

    @Test
    public void testIsXml_red() throws Exception {
        boolean result = resourceLoader.isXml(R.drawable.l0_red);
        assertFalse("result", result);
    }

    @Test
    public void testGetDrawableIds() {
        int[] expected = { R.drawable.l7_white, R.drawable.l0_red,
                R.drawable.l1_orange, R.drawable.l2_yellow,
                R.drawable.l3_green, R.drawable.l4_blue, R.drawable.l5_indigo,
                R.drawable.l6_violet };

        int[] result = resourceLoader.getDrawableIds(R.drawable.rainbow);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("result[" + i + "]", expected[i], result[i]);
        }
    }

    @Test
    public void testGetDrawableIds_shouldWorkWithSystem() throws Exception {
        int[] result = resourceLoader.getDrawableIds(android.R.drawable.ic_popup_sync);
        for (int resultItem : result) {
            assertEquals(-1, resultItem);
        }
    }

    @Test
    public void testNotXmlDrawable() {
        int[] drawables = { R.drawable.l7_white, R.drawable.l0_red,
                R.drawable.l1_orange, R.drawable.l2_yellow,
                R.drawable.l3_green, R.drawable.l4_blue, R.drawable.l5_indigo,
                R.drawable.l6_violet };

        for (int i = 0; i < drawables.length; i++) {
        	Drawable drawable = resourceLoader.getXmlDrawable( drawables[i] );
        	assertThat( drawable, nullValue() );        	
        }
    }

    @Test
    public void testLayerDrawable() {
    	Drawable drawable = resourceLoader.getXmlDrawable( R.drawable.rainbow );
    	assertThat( drawable, instanceOf( LayerDrawable.class ) );
    }
    
    @Test
    public void testStateListDrawable() {
    	Drawable drawable = resourceLoader.getXmlDrawable( R.drawable.state_drawable );
    	assertThat( drawable, instanceOf( StateListDrawable.class ) );
    	ShadowStateListDrawable shDrawable = Robolectric.shadowOf( ( StateListDrawable ) drawable );
    	assertThat( shDrawable.getResourceIdForState( android.R.attr.state_selected ), equalTo( R.drawable.l0_red ) );
    	assertThat( shDrawable.getResourceIdForState( android.R.attr.state_pressed ), equalTo( R.drawable.l1_orange ) );
    	assertThat( shDrawable.getResourceIdForState( android.R.attr.state_focused ), equalTo( R.drawable.l2_yellow ) );
    	assertThat( shDrawable.getResourceIdForState( android.R.attr.state_checkable ), equalTo( R.drawable.l3_green ) );
    	assertThat( shDrawable.getResourceIdForState( android.R.attr.state_checked ), equalTo( R.drawable.l4_blue ) );
    	assertThat( shDrawable.getResourceIdForState( android.R.attr.state_enabled ), equalTo( R.drawable.l5_indigo ) );
    	assertThat( shDrawable.getResourceIdForState( android.R.attr.state_window_focused ), equalTo( R.drawable.l6_violet ) );
    	assertThat( shDrawable.getResourceIdForState( android.R.attr.state_active ), equalTo( R.drawable.l7_white ) );
    }
}
