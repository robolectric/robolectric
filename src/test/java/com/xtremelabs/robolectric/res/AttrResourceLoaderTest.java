package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.util.CustomView;
import org.junit.Before;
import org.junit.Test;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AttrResourceLoaderTest {
    private AttrResourceLoader attrResourceLoader;

    @Before
    public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        new DocumentLoader(attrResourceLoader).loadResourceXmlDir(resourceFile("res", "values"));
    }

    @Test
    public void testAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "itemType", "integer"), equalTo("0"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, "xxx", "itemType"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "itemType", "string"), equalTo("1"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, "xxx", "otherItemType"), equalTo(false));
    }

    @Test
    public void shouldResolveAttributesForSubClasses() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(SubCustomView.class, "xxx", "itemType", "integer"), equalTo("0"));
        assertThat(attrResourceLoader.hasAttributeFor(SubCustomView.class, "xxx", "itemType"), equalTo(true));
    }

    private class SubCustomView extends CustomView {
        public SubCustomView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }
}
