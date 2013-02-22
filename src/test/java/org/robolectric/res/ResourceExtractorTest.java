package org.robolectric.res;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.R;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.robolectric.util.TestUtil.*;

public class ResourceExtractorTest {
    private ResourceIndex resourceIndex;

    @Before
    public void setUp() throws Exception {
        resourceIndex = new MergedResourceIndex(
                new ResourceExtractor(testResources()),
                new ResourceExtractor(systemResources()));
    }

    @Test
    public void shouldHandleStyleable() throws Exception {
        assertThat(ResName.getResourceId(resourceIndex, "id/textStyle", R.class.getPackage().getName()), equalTo(R.id.textStyle));
        assertThat(ResName.getResourceId(resourceIndex, "styleable/TitleBar_textStyle", R.class.getPackage().getName()), CoreMatchers.<Object>nullValue());
    }

    @Test
    public void shouldPrefixResourcesWithPackageContext() throws Exception {
        assertThat(ResName.getResourceId(resourceIndex, "id/text1", "android"), equalTo(android.R.id.text1));
        assertThat(ResName.getResourceId(resourceIndex, "id/text1", R.class.getPackage().getName()), equalTo(R.id.text1));
    }

    @Test
    public void shouldPrefixAllSystemResourcesWithAndroid() throws Exception {
        assertThat(ResName.getResourceId(resourceIndex, "android:id/text1", "android"), equalTo(android.R.id.text1));
    }

    @Test
    public void shouldHandleNull() throws Exception {
        assertThat(ResName.getResourceId(resourceIndex, "@null", ""), equalTo(0));
        assertThat(ResName.getResourceId(resourceIndex, "@null", "android"), equalTo(0));
        assertThat(ResName.getResourceId(resourceIndex, "@null", "anything"), equalTo(0));
    }

    @Test
    public void shouldRetainPackageNameForFullyQualifiedQueries() throws Exception {
        assertThat(resourceIndex.getResName(android.R.id.text1).getFullyQualifiedName(), equalTo("android:id/text1"));
        assertThat(resourceIndex.getResName(R.id.burritos).getFullyQualifiedName(), equalTo("org.robolectric:id/burritos"));
    }

    @Test
    public void shouldResolveEquivalentResNames() throws Exception {
        OverlayResourceIndex overlayResourceIndex = new OverlayResourceIndex(
                "org.robolectric",
                new ResourceExtractor(testResources()),
                new ResourceExtractor(lib1Resources()),
                new ResourceExtractor(lib2Resources()),
                new ResourceExtractor(lib3Resources()));
        resourceIndex = new MergedResourceIndex(overlayResourceIndex, new ResourceExtractor(systemResources()));

        assertThat(resourceIndex.getResourceId(new ResName("org.robolectric", "string", "in_all_libs")), equalTo(R.string.in_all_libs));
        assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib1", "string", "in_all_libs")), equalTo(R.string.in_all_libs));
        assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib2", "string", "in_all_libs")), equalTo(R.string.in_all_libs));
        assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib3", "string", "in_all_libs")), equalTo(R.string.in_all_libs));
    }
}
