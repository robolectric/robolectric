package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static com.xtremelabs.robolectric.util.TestUtil.systemResources;
import static com.xtremelabs.robolectric.util.TestUtil.testResources;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceExtractorTest {
    private ResourceExtractor resourceExtractor;

    @Before
    public void setUp() throws Exception {
        resourceExtractor = new ResourceExtractor(testResources(), systemResources());
    }

    @Test
    public void shouldHandleStyleable() throws Exception {
        assertThat(resourceExtractor.getResourceId("id/textStyle", R.class.getPackage().getName()), equalTo(R.id.textStyle));
        assertThat(resourceExtractor.getResourceId("styleable/TitleBar_textStyle", R.class.getPackage().getName()), CoreMatchers.<Object>nullValue());
    }

    @Test
    public void shouldPrefixResourcesWithPackageContext() throws Exception {
        assertThat(resourceExtractor.getResourceId("id/text1", "android"), equalTo(android.R.id.text1));
        assertThat(resourceExtractor.getResourceId("id/text1", R.class.getPackage().getName()), equalTo(R.id.text1));
    }

    @Test
    public void shouldPrefixAllSystemResourcesWithAndroid() throws Exception {
        assertThat(resourceExtractor.getResourceId("android:id/text1", "android"), equalTo(android.R.id.text1));
    }

    @Test
    public void shouldHandleNull() throws Exception {
        assertThat(resourceExtractor.getResourceId("@null", ""), equalTo(0));
        assertThat(resourceExtractor.getResourceId("@null", "android"), equalTo(0));
        assertThat(resourceExtractor.getResourceId("@null", "anything"), equalTo(0));
    }

    @Test public void shouldRetainPackageNameForFullyQualifiedQueries() throws Exception {
        assertThat(resourceExtractor.getResName(android.R.id.text1).getFullyQualifiedName(), equalTo("android:id/text1"));
        assertThat(resourceExtractor.getResName(R.id.burritos).getFullyQualifiedName(), equalTo("com.xtremelabs.robolectric:id/burritos"));
    }
}
