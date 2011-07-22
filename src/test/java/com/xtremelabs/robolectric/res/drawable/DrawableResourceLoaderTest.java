package com.xtremelabs.robolectric.res.drawable;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.res.DocumentLoader;
import com.xtremelabs.robolectric.res.ResourceExtractor;
import com.xtremelabs.robolectric.res.drawable.DrawableResourceLoader;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

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

        resourceLoader = new DrawableResourceLoader(extractor,
                resourceFile("res"));
        DocumentLoader documentLoader = new DocumentLoader(resourceLoader);
        documentLoader.loadResourceXmlDir(new File(resourceFile("res"),
                "drawable"));
    }

    @Test
    public void testProcessResourceXml() throws Exception {
        assertTrue("drawable/rainbow",
                resourceLoader.documents.containsKey("drawable/rainbow"));
        assertEquals("documents.size", 1, resourceLoader.documents.size());
    }

    @Test
    public void testIsXml_rainbow() throws Exception {
        boolean result = resourceLoader.isXml(R.drawable.rainbow);
        assertTrue("result", result);
    }

    @Test
    public void testIsXml_red() throws Exception {
        boolean result = resourceLoader.isXml(R.drawable.l0_red);
        assertFalse("result", result);
    }

    @Test
    public void testGetDrawableIds() {
        int[] expected = new int[] { R.drawable.l7_white, R.drawable.l0_red,
                R.drawable.l1_orange, R.drawable.l2_yellow,
                R.drawable.l3_green, R.drawable.l4_blue, R.drawable.l5_indigo,
                R.drawable.l6_violet };

        int[] result = resourceLoader.getDrawableIds(R.drawable.rainbow);
        for (int i = 0; i < expected.length; i++) {
            assertEquals("result[" + i + "]", expected[i], result[i]);
        }
    }
}
