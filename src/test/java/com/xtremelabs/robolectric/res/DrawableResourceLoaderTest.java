package com.xtremelabs.robolectric.res;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.shadows.ShadowStateListDrawable;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
public class DrawableResourceLoaderTest {
    protected DrawableResourceLoader drawableResourceLoader;
    private DrawableBuilder drawableBuilder;
    private ResBundle<DrawableNode> drawableNodes;
    private ResourceExtractor resourceExtractor;

    @Before
    public void setup() throws Exception {
        drawableNodes = new ResBundle<DrawableNode>();
        drawableResourceLoader = new DrawableResourceLoader(drawableNodes);
        DocumentLoader documentLoader = new DocumentLoader(drawableResourceLoader);

        documentLoader.loadResourceXmlSubDirs(testResources(), "drawable");
        documentLoader.loadResourceXmlSubDirs(systemResources(), "drawable");

        resourceExtractor = new ResourceExtractor(testResources(), systemResources());
        drawableBuilder = new DrawableBuilder(drawableNodes, resourceExtractor);
        drawableResourceLoader.findNinePatchResources(testResources());
        drawableResourceLoader.findNinePatchResources(systemResources());
    }

    @Test
    public void testProcessResourceXml() throws Exception {
        drawableNodes = new ResBundle<DrawableNode>();
        drawableResourceLoader = new DrawableResourceLoader(drawableNodes);
        DocumentLoader documentLoader = new DocumentLoader(drawableResourceLoader);

        documentLoader.loadResourceXmlSubDirs(testResources(), "drawable");
        drawableResourceLoader.findNinePatchResources(testResources());

        assertNotNull(drawableNodes.get(new ResName(TEST_PACKAGE, "drawable", "rainbow"), ""));
        assertEquals(4, drawableNodes.size());
    }

    @Test
    public void testIsXml_rainbow() throws Exception {
        assertTrue(drawableBuilder.isXml(R.drawable.rainbow, ""));
    }

    @Test
    public void testIsXml_shouldWorkWithSystem() throws Exception {
        assertTrue(drawableBuilder.isXml(android.R.drawable.ic_popup_sync, ""));
    }

    @Test
    public void testIsXml_red() throws Exception {
        boolean result = drawableBuilder.isXml(R.drawable.l0_red, "");
        assertFalse("result", result);
    }

    @Test
    public void testNotXmlDrawable() {
        int[] drawables = {R.drawable.l7_white, R.drawable.l0_red,
                R.drawable.l1_orange, R.drawable.l2_yellow,
                R.drawable.l3_green, R.drawable.l4_blue, R.drawable.l5_indigo,
                R.drawable.l6_violet};

        for (int i = 0; i < drawables.length; i++) {
            Drawable drawable = drawableBuilder.getDrawable(resourceExtractor.getResName(drawables[i]), null, "");
            assertThat(drawable, instanceOf(BitmapDrawable.class));
        }
    }

    @Test
    public void testLayerDrawable() {
        Drawable drawable = drawableBuilder.getDrawable(resourceExtractor.getResName(R.drawable.rainbow), null, "");
        assertThat(drawable, instanceOf(LayerDrawable.class));
        assertEquals(8, ((LayerDrawable) drawable).getNumberOfLayers());

        assertEquals(6, ((LayerDrawable) drawableBuilder.getDrawable(resourceExtractor.getResName(R.drawable.rainbow), null, "xlarge")).getNumberOfLayers());
    }

    @Test
    public void testStateListDrawable() {
        Drawable drawable = drawableBuilder.getDrawable(resourceExtractor.getResName(R.drawable.state_drawable), null, "");
        assertThat(drawable, instanceOf(StateListDrawable.class));
        ShadowStateListDrawable shDrawable = Robolectric.shadowOf((StateListDrawable) drawable);
        assertThat(shDrawable.getResourceIdForState(android.R.attr.state_selected), equalTo(R.drawable.l0_red));
        assertThat(shDrawable.getResourceIdForState(android.R.attr.state_pressed), equalTo(R.drawable.l1_orange));
        assertThat(shDrawable.getResourceIdForState(android.R.attr.state_focused), equalTo(R.drawable.l2_yellow));
        assertThat(shDrawable.getResourceIdForState(android.R.attr.state_checkable), equalTo(R.drawable.l3_green));
        assertThat(shDrawable.getResourceIdForState(android.R.attr.state_checked), equalTo(R.drawable.l4_blue));
        assertThat(shDrawable.getResourceIdForState(android.R.attr.state_enabled), equalTo(R.drawable.l5_indigo));
        assertThat(shDrawable.getResourceIdForState(android.R.attr.state_window_focused), equalTo(R.drawable.l6_violet));
        assertThat(shDrawable.getResourceIdForState(android.R.attr.state_active), equalTo(R.drawable.l7_white));
    }

    @Test public void shouldCreateAnimsAndColors() throws Exception {
        assertInstanceOf(AnimationDrawable.class, drawableBuilder.getDrawable(resourceExtractor.getResName(R.anim.test_anim_1), null, ""));
        assertInstanceOf(ColorDrawable.class, drawableBuilder.getDrawable(resourceExtractor.getResName(R.color.grey42), null, ""));
    }

    @Test
    public void shouldIdentifyNinePatchDrawables() {
        assertThat(drawableBuilder.isNinePatchDrawable(resourceExtractor.getResName(R.drawable.nine_patch_drawable), ""), equalTo(true));
        assertThat(drawableBuilder.isNinePatchDrawable(resourceExtractor.getResName(R.drawable.l2_yellow), ""), equalTo(false));
        assertThat(drawableBuilder.isNinePatchDrawable(resourceExtractor.getResName(R.drawable.state_drawable), ""), equalTo(false));
        assertThat(drawableBuilder.isNinePatchDrawable(resourceExtractor.getResName(R.drawable.animation_list), ""), equalTo(false));
        assertThat(drawableBuilder.isNinePatchDrawable(null, ""), equalTo(false));
    }
}
