package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithDefaults.class)
public class RawResourceLoaderTest {

    private RawResourceLoader rawResourceLoader;

    @Before public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        rawResourceLoader = new RawResourceLoader(resourceExtractor, resourceFile("res"));
    }

    @Test
    public void shouldReturnRawResourcesWithExtensions() throws Exception {
        InputStream is = rawResourceLoader.getValue(R.raw.raw_resource);
        assertEquals("raw txt file contents", TestUtil.readString(is));
    }

    @Test
    public void shouldReturnRawResourcesWithoutExtensions() throws Exception {
        InputStream is = rawResourceLoader.getValue(R.raw.raw_no_ext);
        assertEquals("no ext file contents", TestUtil.readString(is));
    }
}
