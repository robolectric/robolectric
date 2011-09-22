package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.getSystemResourceDir;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        assertEquals("documents.size", 114, resourceLoader.documents.size());
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
}
