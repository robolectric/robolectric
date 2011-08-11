package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static com.xtremelabs.robolectric.util.TestUtil.getSystemResourceDir;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public class StringArrayResourceLoaderTest {
    private StringArrayResourceLoader stringArrayResourceLoader;

    @Before public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        resourceExtractor.addSystemRClass(android.R.class);

        File resourceXmlDir = resourceFile("res", "values");
        File systemResourceXmlDir = getSystemResourceDir("values");

        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(resourceXmlDir);
        new DocumentLoader(stringResourceLoader).loadSystemResourceXmlDir(systemResourceXmlDir);

        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor, stringResourceLoader);
        new DocumentLoader(stringArrayResourceLoader).loadResourceXmlDir(resourceXmlDir);
        new DocumentLoader(stringArrayResourceLoader).loadSystemResourceXmlDir(systemResourceXmlDir);
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
    public void shouldAddAndroidToSystemStringArrayName() throws Exception {
        assertThat(Arrays.asList(stringArrayResourceLoader.getArrayValue(android.R.array.emailAddressTypes)), hasItems("Home", "Work", "Other", "Custom"));
        assertThat(Arrays.asList(stringArrayResourceLoader.getArrayValue(R.array.emailAddressTypes)), hasItems("Doggy", "Catty"));
    }
}
