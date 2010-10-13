package com.xtremelabs.droidsugar.util;

import com.xtremelabs.droidsugar.R;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ColorResourceLoaderTest {
    private ColorResourceLoader colorResourceLoader;

    @Before public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(R.class);
        colorResourceLoader = new ColorResourceLoader(resourceExtractor);
        new DocumentLoader(colorResourceLoader).loadResourceXmlDir(new File("test/res/values"));
    }

    @Test
    public void testStringsAreResolved() throws Exception {
        assertThat(colorResourceLoader.getValue(R.color.black), equalTo(0x000000));
        assertThat(colorResourceLoader.getValue(R.color.white), equalTo(0xffffff));
    }

    @Test
    public void shouldHandleColorsWithAlpha() throws Exception {
        assertThat(colorResourceLoader.getValue(R.color.clear), equalTo(0x000001));
    }
}
