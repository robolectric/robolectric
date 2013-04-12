package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.robolectric.util.TestUtil.TEST_RESOURCE_PATH;
import static org.robolectric.util.TestUtil.testResources;
import static org.robolectric.util.Util.file;

@RunWith(TestRunners.WithDefaults.class)
public class RawResourceLoaderTest {

    private ResourceExtractor resourceIndex;
    private ResBundle<File> rawResourceFiles;

    @Before public void setUp() throws Exception {
        resourceIndex = new ResourceExtractor(testResources());
        rawResourceFiles = new ResBundle<File>();
        RawResourceLoader rawResourceLoader = new RawResourceLoader(TEST_RESOURCE_PATH);
        rawResourceLoader.loadTo(rawResourceFiles);
    }

    @Test
    public void shouldReturnRawResourcesWithExtensions() throws Exception {
        File f = rawResourceFiles.get(resourceIndex.getResName(R.raw.raw_resource), "");
        assertEquals(file(TEST_RESOURCE_PATH.rawDir, "raw_resource.txt"), f);
    }

    @Test
    public void shouldReturnRawResourcesWithoutExtensions() throws Exception {
        File f = rawResourceFiles.get(resourceIndex.getResName(R.raw.raw_no_ext), "");
        assertEquals(file(TEST_RESOURCE_PATH.rawDir, "raw_no_ext"), f);
    }
}
