package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import org.junit.Before;
import org.junit.Test;

import static com.xtremelabs.robolectric.util.TestUtil.testResources;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PluralResourceLoaderTest {
    private PluralResourceLoader pluralResourceLoader;

    @Before
    public void setUp() throws Exception {
        ResourceExtractor extractor = new ResourceExtractor(testResources());

        StringResourceLoader stringResourceLoader = new StringResourceLoader(extractor);
        pluralResourceLoader = new PluralResourceLoader(extractor, stringResourceLoader);

        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(testResources(), "values");
        new DocumentLoader(pluralResourceLoader).loadResourceXmlDir(testResources(), "values");
    }

    @Test
    public void testPluralsAreResolved() throws Exception {
        assertThat(pluralResourceLoader.getValue(R.plurals.beer, 0), equalTo("Howdy"));
        assertThat(pluralResourceLoader.getValue(R.plurals.beer, 1), equalTo("One beer"));
        assertThat(pluralResourceLoader.getValue(R.plurals.beer, 2), equalTo("Two beers"));
        assertThat(pluralResourceLoader.getValue(R.plurals.beer, 3), equalTo("%d beers, yay!"));
    }
}
