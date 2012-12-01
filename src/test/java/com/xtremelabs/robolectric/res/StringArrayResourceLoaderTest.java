package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.xtremelabs.robolectric.util.TestUtil.systemResources;
import static com.xtremelabs.robolectric.util.TestUtil.testResources;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public class StringArrayResourceLoaderTest {
    private StringArrayResourceLoader stringArrayResourceLoader;

    @Before
    public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor(testResources(), systemResources());

        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(testResources(), "values");
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(systemResources(), "values");

        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor, stringResourceLoader);
        new DocumentLoader(stringArrayResourceLoader).loadResourceXmlDir(testResources(), "values");
        new DocumentLoader(stringArrayResourceLoader).loadResourceXmlDir(systemResources(), "values");
    }

    @Test
    public void testStringsAreResolved() throws Exception {
        assertThat(Arrays.asList(stringArrayResourceLoader.getArrayValue(R.array.items)), hasItems("foo", "bar"));
    }

    @Test
    public void testStringsAreWithReferences() throws Exception {
        assertThat(Arrays.asList(stringArrayResourceLoader.getArrayValue(R.array.greetings)), hasItems("hola", "Hello"));
    }

    @Test
    public void testLazyResolution_ReferencesWorkEvenIfLoadedOutOfOrder() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor(testResources(), systemResources());
        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor, stringResourceLoader);
        new DocumentLoader(stringArrayResourceLoader).loadResourceXmlDir(testResources(), "values");
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(testResources(), "values");

        assertThat(Arrays.asList(stringArrayResourceLoader.getArrayValue(R.array.greetings)), hasItems("hola", "Hello"));
    }

    @Test
    public void shouldAddAndroidToSystemStringArrayName() throws Exception {
        assertThat(Arrays.asList(stringArrayResourceLoader.getArrayValue(android.R.array.emailAddressTypes)), hasItems("Home", "Work", "Other", "Custom"));
        assertThat(Arrays.asList(stringArrayResourceLoader.getArrayValue(R.array.emailAddressTypes)), hasItems("Doggy", "Catty"));
    }
}
