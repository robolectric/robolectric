package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.xtremelabs.robolectric.util.TestUtil.getSystemResourceDir;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class IntegerArrayResourceLoaderTest {
    private IntegerArrayResourceLoader integerArrayResourceLoader;

    @Before public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        resourceExtractor.addSystemRClass(android.R.class);

        File resourceXmlDir = resourceFile("res", "values");
        File systemResourceXmlDir = getSystemResourceDir("values");

        IntegerResourceLoader integerResourceLoader = new IntegerResourceLoader(resourceExtractor);
        new DocumentLoader(integerResourceLoader).loadResourceXmlDir(resourceXmlDir);
        new DocumentLoader(integerResourceLoader).loadSystemResourceXmlDir(systemResourceXmlDir);

        integerArrayResourceLoader = new IntegerArrayResourceLoader(resourceExtractor, integerResourceLoader);
        new DocumentLoader(integerArrayResourceLoader).loadResourceXmlDir(resourceXmlDir);
        new DocumentLoader(integerArrayResourceLoader).loadSystemResourceXmlDir(systemResourceXmlDir);
    }

    @Test
    public void testIntegersAreResolved() throws Exception {
        assertArrayEquals(
        		new int[]{0, 1, 2, 3, 4},
        		integerArrayResourceLoader.getArrayValue(R.array.zero_to_four_int_array)
        		);
    }
    
    @Test
    public void testEmptyArray() throws Exception {
        assertThat(integerArrayResourceLoader.getArrayValue(R.array.empty_int_array).length,
        		equalTo(0));
    }
    
    @Test
    public void testIntegersWithReferences() throws Exception {
        assertArrayEquals(
        		new int[]{0, 2000, 1},
        		integerArrayResourceLoader.getArrayValue(R.array.with_references_int_array)
        		);
    }
}
