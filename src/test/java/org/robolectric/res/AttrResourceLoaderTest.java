package org.robolectric.res;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import org.robolectric.util.CustomView;
import org.junit.Before;
import org.junit.Test;

import static org.robolectric.util.TestUtil.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.fest.assertions.api.Assertions.assertThat;

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
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "otherItemType")).isFalse();
    }

    @Test
    public void testInlineEnumAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "itemType", "marsupial")).isEqualTo("0");
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "itemType")).isTrue();

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "itemType", "ungulate")).isEqualTo("1");
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "otherItemType")).isFalse();
    }

    @Test
    public void testAttributesAreResolvedForSystemAttrs() throws Exception {
        String expected = "" + ImageView.ScaleType.FIT_CENTER.ordinal();
        assertThat(attrResourceLoader.convertValueToEnum(ImageView.class, SYSTEM_PACKAGE, "scaleType", "fitCenter")).isEqualTo(expected);
        assertThat(attrResourceLoader.hasAttributeFor(ImageView.class, SYSTEM_PACKAGE, "scaleType")).isTrue();
    }

    @Test
    public void testGlobalEnumAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "keycode", "KEYCODE_SOFT_RIGHT")).isEqualTo("2");
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "keycode")).isTrue();

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "keycode", "KEYCODE_HOME")).isEqualTo("3");
    }

    @Test
    public void testInlineFlagAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "scrollBars", "horizontal")).isEqualTo("0x00000100");
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "scrollBars")).isTrue();

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "scrollBars", "vertical")).isEqualTo("0x00000200");
    }

    @Test
    public void testGlobalFlagAttributesAreResolved() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "gravity", "center")).isEqualTo("0x11");
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, TEST_PACKAGE, "gravity")).isTrue();

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, TEST_PACKAGE, "gravity", "fill_vertical")).isEqualTo("0x70");
    }

    @Test
    public void shouldResolveAttributesForSubClasses() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(SubCustomView.class, TEST_PACKAGE, "itemType", "marsupial")).isEqualTo("0");
        assertThat(attrResourceLoader.hasAttributeFor(SubCustomView.class, TEST_PACKAGE, "itemType")).isTrue();
    }
    
    @Test
    public void systemResourcesArePrefixedAndroid() throws Exception {
        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, SYSTEM_PACKAGE, "gravity", "center")).isEqualTo("0x11");
        assertThat(attrResourceLoader.hasAttributeFor(CustomView.class, SYSTEM_PACKAGE, "gravity")).isTrue();

        assertThat(attrResourceLoader.convertValueToEnum(CustomView.class, SYSTEM_PACKAGE, "gravity", "fill_vertical")).isEqualTo("0x70");
    }

    private class SubCustomView extends CustomView {
        public SubCustomView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }
}
