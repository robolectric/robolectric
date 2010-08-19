package com.xtremelabs.droidsugar.util;

import com.xtremelabs.droidsugar.R;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AttrResourceLoaderTest {
    @Test
    public void testAttributesAreResolved() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(R.class);
        AttrResourceLoader attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        attrResourceLoader.loadDirs(new File("test/res/values"));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class.getName(), "xxx", "itemType", "integer"),
                equalTo("0"));
        assertThat(attrResourceLoader.handles(CustomView.class.getName(), "xxx", "itemType"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class.getName(), "xxx", "itemType", "string"),
                equalTo("1"));

        assertThat(attrResourceLoader.handles(CustomView.class.getName(), "xxx", "otherItemType"), equalTo(false));
    }
}
