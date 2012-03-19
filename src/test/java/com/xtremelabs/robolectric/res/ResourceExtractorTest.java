package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ResourceExtractorTest {
    private ResourceExtractor resourceExtractor;

    @Before
    public void setUp() throws Exception {
        resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        resourceExtractor.addSystemRClass(android.R.class);
    }

    @Test
    public void shouldHandleStyleable() throws Exception {
        assertThat(resourceExtractor.getLocalResourceId("id/textStyle"), equalTo(R.id.textStyle));
        assertThat(resourceExtractor.getLocalResourceId("styleable/TitleBar_textStyle"), CoreMatchers.<Object>nullValue());
    }

    @Test
    public void shouldPrefixAllSystemResourcesWithAndroid() throws Exception {
        assertThat(resourceExtractor.getResourceId("android:id/text1"), equalTo(android.R.id.text1));
    }
    
    @Test
    public void shouldHandleNull() throws Exception {
        assertThat(resourceExtractor.getLocalResourceId("@null"), equalTo(0));
    }
}
