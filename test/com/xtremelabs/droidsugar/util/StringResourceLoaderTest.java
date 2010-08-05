package com.xtremelabs.droidsugar.util;

import com.xtremelabs.droidsugar.R;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class StringResourceLoaderTest {
    @Test
    public void testStringsAreResolved() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(R.class);
        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringResourceLoader.loadDirs(new File("test/res/values"));
        assertThat(stringResourceLoader.getValue(R.string.hello), equalTo("Hello"));
        assertThat(stringResourceLoader.getValue(R.string.howdy), equalTo("Howdy"));
    }
}
