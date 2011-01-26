package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import org.junit.Before;
import org.junit.Test;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class StringResourceLoaderTest {
    private StringResourceLoader stringResourceLoader;

    @Before public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(resourceFile("res", "values"));
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

    @Test
    public void shouldResolveStringReferences() throws Exception {
        assertThat(stringResourceLoader.getValue(R.string.greeting), equalTo("Howdy"));
    }
}
