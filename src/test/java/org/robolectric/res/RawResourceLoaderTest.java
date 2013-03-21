package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;
import org.robolectric.util.TestUtil;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.robolectric.util.TestUtil.resourceFile;
import static org.robolectric.util.TestUtil.testResources;

@RunWith(TestRunners.WithDefaults.class)
public class RawResourceLoaderTest {

    private RawResourceLoader rawResourceLoader;
    private ResourceExtractor resourceIndex;

    @Before public void setUp() throws Exception {
        resourceIndex = new ResourceExtractor(testResources());
        rawResourceLoader = new RawResourceLoader(resourceFile("res"));
    }

    @Test
    public void shouldReturnRawResourcesWithExtensions() throws Exception {
        InputStream is = rawResourceLoader.getValue(resourceIndex.getResName(R.raw.raw_resource));
        assertEquals("raw txt file contents", TestUtil.readString(is));
    }

    @Test
    public void shouldReturnRawResourcesWithoutExtensions() throws Exception {
        InputStream is = rawResourceLoader.getValue(resourceIndex.getResName(R.raw.raw_no_ext));
        assertEquals("no ext file contents", TestUtil.readString(is));
    }
}
