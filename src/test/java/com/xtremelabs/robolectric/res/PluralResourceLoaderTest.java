package com.xtremelabs.robolectric.res;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.xtremelabs.robolectric.R;
import org.junit.Before;
import org.junit.Test;

public class PluralResourceLoaderTest {
    private PluralResourceLoader pluralResourceLoader;

    @Before
    public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);

        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        pluralResourceLoader = new PluralResourceLoader(resourceExtractor, stringResourceLoader);

        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(resourceFile("res", "values"));
        new DocumentLoader(pluralResourceLoader).loadResourceXmlDir(resourceFile("res", "values"));
    }

    @Test
    public void testPluralsAreResolved() throws Exception {
        assertThat(pluralResourceLoader.getValue(R.plurals.beer, 0), equalTo("Howdy"));
        assertThat(pluralResourceLoader.getValue(R.plurals.beer, 1), equalTo("One beer"));
        assertThat(pluralResourceLoader.getValue(R.plurals.beer, 2), equalTo("Two beers"));
        assertThat(pluralResourceLoader.getValue(R.plurals.beer, 3), equalTo("%d beers, yay!"));
    }
}
