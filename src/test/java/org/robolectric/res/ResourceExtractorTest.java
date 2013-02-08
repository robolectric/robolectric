package org.robolectric.res;

import org.robolectric.R;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.tester.android.util.ResName;

import static org.robolectric.util.TestUtil.systemResources;
import static org.robolectric.util.TestUtil.testResources;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceExtractorTest {
    private ResourceIndex resourceIndex;

    @Before
    public void setUp() throws Exception {
        resourceIndex = new ResourceExtractor(testResources(), systemResources());
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

    @Test public void shouldRetainPackageNameForFullyQualifiedQueries() throws Exception {
        assertThat(resourceIndex.getResName(android.R.id.text1).getFullyQualifiedName(), equalTo("android:id/text1"));
        assertThat(resourceIndex.getResName(R.id.burritos).getFullyQualifiedName(), equalTo("org.robolectric:id/burritos"));
    }
}
