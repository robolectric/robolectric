package org.robolectric.res;

import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.builder.DrawableBuilder;
import org.robolectric.shadows.ShadowStateListDrawable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.robolectric.util.TestUtil.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(TestRunners.WithDefaults.class)
public class DrawableResourceLoaderTest {
    protected DrawableResourceLoader drawableResourceLoader;
    private DrawableBuilder drawableBuilder;
    private ResBundle<DrawableNode> drawableNodes;
    private ResourceIndex resourceIndex;

    @Before
    public void setup() throws Exception {
        drawableNodes = new ResBundle<DrawableNode>();
        drawableResourceLoader = new DrawableResourceLoader(drawableNodes);
        DocumentLoader documentLoader = new DocumentLoader(drawableResourceLoader);

        documentLoader.loadResourceXmlSubDirs(testResources(), "drawable");
        documentLoader.loadResourceXmlSubDirs(systemResources(), "drawable");

        resourceIndex = new ResourceExtractor(testResources(), systemResources());
        drawableBuilder = new DrawableBuilder(resourceIndex);
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
    public void testGetDrawable_rainbow() throws Exception {
        ResName resName = getResName(R.drawable.rainbow);
        assertNotNull(drawableBuilder.getDrawable(resName, mock(Resources.class), drawableNodes.get(resName, "")));
    }

    @Test
    public void testGetDrawable_shouldWorkWithSystem() throws Exception {
        ResName resName = getResName(android.R.drawable.ic_popup_sync);
        assertNotNull(drawableBuilder.getDrawable(resName, mock(Resources.class), drawableNodes.get(resName, "")));
    }

    @Test
    public void testGetDrawable_red() throws Exception {
        ResName resName = getResName(R.drawable.l0_red);
        assertNotNull(drawableBuilder.getDrawable(resName, mock(Resources.class), drawableNodes.get(resName, "")));
    }

    @Test
    public void testNotXmlDrawable() {
        int[] drawables = {R.drawable.l7_white, R.drawable.l0_red,
                R.drawable.l1_orange, R.drawable.l2_yellow,
                R.drawable.l3_green, R.drawable.l4_blue, R.drawable.l5_indigo,
                R.drawable.l6_violet};

        for (int i = 0; i < drawables.length; i++) {
            ResName resName = getResName(drawables[i]);
            Drawable drawable = drawableBuilder.getDrawable(resName, null, drawableNodes.get(resName, ""));
            assertThat(drawable, instanceOf(BitmapDrawable.class));
        }
    }

    @Test
    public void testLayerDrawable() {
        ResName resName = getResName(R.drawable.rainbow);
        Drawable drawable = drawableBuilder.getDrawable(resName, mock(Resources.class), drawableNodes.get(resName, ""));
        assertThat(drawable, instanceOf(LayerDrawable.class));
        assertEquals(8, ((LayerDrawable) drawable).getNumberOfLayers());

        assertEquals(6, ((LayerDrawable) drawableBuilder.getDrawable(resName, mock(Resources.class), drawableNodes.get(resName, "xlarge"))).getNumberOfLayers());
    }

    @Test
    public void testStateListDrawable() {
        ResName resName = getResName(R.drawable.state_drawable);
        Drawable drawable = drawableBuilder.getDrawable(resName, null, drawableNodes.get(resName, ""));
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
        ResName resName1 = getResName(R.anim.test_anim_1);
        assertInstanceOf(AnimationDrawable.class, drawableBuilder.getDrawable(resName1, null, drawableNodes.get(resName1, "")));
        ResName resName2 = getResName(R.color.grey42);
        assertInstanceOf(ColorDrawable.class, drawableBuilder.getDrawable(resName2, null, drawableNodes.get(resName2, "")));
    }

    @Test
    public void shouldIdentifyNinePatchDrawables() {
        assertThat(drawableBuilder.isNinePatchDrawable(drawableNodes.get(getResName(R.drawable.nine_patch_drawable), "")), equalTo(true));
        assertThat(drawableBuilder.isNinePatchDrawable(drawableNodes.get(getResName(R.drawable.l2_yellow), "")), equalTo(false));
        assertThat(drawableBuilder.isNinePatchDrawable(drawableNodes.get(getResName(R.drawable.state_drawable), "")), equalTo(false));
        assertThat(drawableBuilder.isNinePatchDrawable(drawableNodes.get(getResName(R.drawable.animation_list), "")), equalTo(false));
        assertThat(drawableBuilder.isNinePatchDrawable(drawableNodes.get(null, "")), equalTo(false));
    }

    private ResName getResName(int resourceId) {
        return resourceIndex.getResName(resourceId);
    }
}
