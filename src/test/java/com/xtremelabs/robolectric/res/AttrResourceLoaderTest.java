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
        attrResourceLoader = makeResourceLoader();
        new DocumentLoader(attrResourceLoader).loadResourceXmlDir(resourceFile("res", "values"));
    }

    private AttrResourceLoader makeResourceLoader() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        return new AttrResourceLoader(resourceExtractor);
    }

    @Test
    public void testUnknownAttributesAreUnknown() throws Exception {
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, "xxx", "otherItemType"), equalTo(false));
    }

    @Test
    public void testInlineEnumAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "itemType", "integer"), equalTo("0"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, "xxx", "itemType"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "itemType", "string"), equalTo("1"));
    }

    @Test
    public void testGlobalEnumAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "keycode", "KEYCODE_SOFT_RIGHT"), equalTo("2"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, "xxx", "keycode"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "keycode", "KEYCODE_HOME"), equalTo("3"));
    }

    @Test
    public void testInlineFlagAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "scrollbars", "horizontal"), equalTo("0x00000100"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, "xxx", "scrollbars"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "scrollbars", "vertical"), equalTo("0x00000200"));
    }

    @Test
    public void testGlobalFlagAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "gravity", "center"), equalTo("0x11"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, "xxx", "gravity"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "xxx", "gravity", "fill_vertical"), equalTo("0x70"));
    }

    @Test
    public void shouldResolveAttributesForSubClasses() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(SubCustomView.class, "xxx", "itemType", "integer"), equalTo("0"));
        assertThat(attrResourceLoader.hasAttributeFor(SubCustomView.class, "xxx", "itemType"), equalTo(true));
    }
    
    @Test
    public void systemResourcesArePrefixedAndroid() throws Exception {
        attrResourceLoader = makeResourceLoader();
        new DocumentLoader(attrResourceLoader).loadSystemResourceXmlDir(resourceFile("res", "values"));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "http://schemas.android.com/apk/res/android",
                "gravity", "center"), equalTo("0x11"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, "http://schemas.android.com/apk/res/android",
                "gravity"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, "http://schemas.android.com/apk/res/android",
                "gravity", "fill_vertical"), equalTo("0x70"));
    }

    private class SubCustomView extends CustomView {
        public SubCustomView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }
}
