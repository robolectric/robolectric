package com.xtremelabs.droidsugar.util;

import com.xtremelabs.droidsugar.R;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class StringArrayResourceLoaderTest {
    private StringArrayResourceLoader stringArrayResourceLoader;

    @Before public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(R.class);
        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor);
        new DocumentLoader(stringArrayResourceLoader).loadResourceXmlDir(new File("test/res/values"));
    }

    @Test
    public void testStringsAreResolved() throws Exception {
        assertThat(Arrays.asList(stringArrayResourceLoader.getArrayValue(R.array.items)), contains("foo", "bar"));
    }
}
