package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import org.junit.Before;
import org.junit.Test;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class IntegerResourceLoaderTest {
    private IntegerResourceLoader integerResourceLoader;

    @Before public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        integerResourceLoader = new IntegerResourceLoader(resourceExtractor);
        new DocumentLoader(integerResourceLoader).loadResourceXmlDir(resourceFile("res", "values"));
    }

    @Test
    public void testIntegersAreResolved() throws Exception {
        assertThat(integerResourceLoader.getValue(R.integer.meaning_of_life), equalTo(42));
        assertThat(integerResourceLoader.getValue(R.integer.loneliest_number), equalTo(1));
    }

    @Test
    public void testHexValuesAreResolved() throws Exception {
        assertThat(integerResourceLoader.getValue(R.integer.hex_int), equalTo((int)Long.parseLong("FFFF0000", 16)));
    }

    @Test
    public void shouldResolveStringReferences() throws Exception {
        assertThat(integerResourceLoader.getValue(R.integer.there_can_be_only), equalTo(1));
    }
}
