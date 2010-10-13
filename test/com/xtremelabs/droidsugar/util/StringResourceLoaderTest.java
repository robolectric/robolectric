package com.xtremelabs.droidsugar.util;

import com.xtremelabs.droidsugar.R;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class StringResourceLoaderTest {
    private StringResourceLoader stringResourceLoader;

    @Before public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(R.class);
        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringResourceLoader.loadDirs(new File("test/res/values"));
    }

    @Test
    public void testStringsAreResolved() throws Exception {
        assertThat(stringResourceLoader.getValue(R.string.hello), equalTo("Hello"));
        assertThat(stringResourceLoader.getValue(R.string.howdy), equalTo("Howdy"));
    }

    @Test
    public void testHtmlTagsAreRemovedFromStrings() throws Exception {
        assertThat(stringResourceLoader.getValue(R.string.some_html), equalTo("Hello, world"));
    }
}
