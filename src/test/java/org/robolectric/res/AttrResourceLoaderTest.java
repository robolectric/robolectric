package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.xtremelabs.robolectric.util.CustomView;
import org.junit.Before;
import org.junit.Test;

import static com.xtremelabs.robolectric.util.TestUtil.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AttrResourceLoaderTest {
    private AttrResourceLoader attrResourceLoader;

    @Before
    public void setUp() throws Exception {
        attrResourceLoader = new AttrResourceLoader();
        new DocumentLoader(attrResourceLoader).loadResourceXmlDir(testResources(), "values");
        new DocumentLoader(attrResourceLoader).loadResourceXmlDir(systemResources(), "values");
    }

    @Test
    public void testUnknownAttributesAreUnknown() throws Exception {
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "otherItemType"), equalTo(false));
    }

    @Test
    public void testInlineEnumAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "itemType", "marsupial"), equalTo("0"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "itemType"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "itemType", "ungulate"), equalTo("1"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "otherItemType"), equalTo(false));
    }

    @Test
    public void testAttributesAreResolvedForSystemAttrs() throws Exception {
        String expected = "" + ImageView.ScaleType.FIT_CENTER.ordinal();
        assertThat(attrResourceLoader.convertValueToEnum(ImageView.class, SYSTEM_PACKAGE, "scaleType", "fitCenter"), equalTo(expected));
        assertThat(attrResourceLoader.hasAttributeFor(ImageView.class, SYSTEM_PACKAGE, "scaleType"), equalTo(true));
    }

    @Test
    public void testGlobalEnumAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "keycode", "KEYCODE_SOFT_RIGHT"), equalTo("2"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "keycode"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "keycode", "KEYCODE_HOME"), equalTo("3"));
    }

    @Test
    public void testInlineFlagAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "scrollBars", "horizontal"), equalTo("0x00000100"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "scrollBars"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "scrollBars", "vertical"), equalTo("0x00000200"));
    }

    @Test
    public void testGlobalFlagAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "gravity", "center"), equalTo("0x11"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "gravity"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "gravity", "fill_vertical"), equalTo("0x70"));
    }

    @Test
    public void shouldResolveAttributesForSubClasses() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(SubCustomView.class, TEST_PACKAGE, "itemType", "marsupial"), equalTo("0"));
        assertThat(attrResourceLoader.hasAttributeFor(SubCustomView.class, TEST_PACKAGE, "itemType"), equalTo(true));
    }
    
    @Test
    public void systemResourcesArePrefixedAndroid() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, SYSTEM_PACKAGE, "gravity", "center"), equalTo("0x11"));
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, SYSTEM_PACKAGE, "gravity"), equalTo(true));

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, SYSTEM_PACKAGE, "gravity", "fill_vertical"), equalTo("0x70"));
    }

    private class SubCustomView extends CustomView {
        public SubCustomView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }
}
