package org.robolectric.res;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.shadows.ShadowStateListDrawable;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.TestUtil.TEST_PACKAGE;
import static org.robolectric.util.TestUtil.assertInstanceOf;
import static org.robolectric.util.TestUtil.systemResources;
import static org.robolectric.util.TestUtil.testResources;

@RunWith(TestRunners.WithDefaults.class)
public class DrawableResourceLoaderTest {
  protected DrawableResourceLoader drawableResourceLoader;
  private ResBundle<DrawableNode> drawableNodes;
  private ResourceIndex resourceIndex;
  private Resources resources;

  @Before
  public void setup() throws Exception {
    drawableNodes = new ResBundle<DrawableNode>();
    drawableResourceLoader = new DrawableResourceLoader(drawableNodes);
    new DocumentLoader(testResources()).load("drawable", drawableResourceLoader);
    new DocumentLoader(testResources()).load("anim", drawableResourceLoader);
    new DocumentLoader(systemResources()).load("drawable", drawableResourceLoader);

    resourceIndex = new MergedResourceIndex(
        new ResourceExtractor(testResources()),
        new ResourceExtractor(getClass().getClassLoader()));
    drawableResourceLoader.findDrawableResources(testResources());
    drawableResourceLoader.findDrawableResources(systemResources());
    resources = Robolectric.application.getResources();
  }

  @Test
  public void testProcessResourceXml() throws Exception {
    drawableNodes = new ResBundle<DrawableNode>();
    drawableResourceLoader = new DrawableResourceLoader(drawableNodes);

    new DocumentLoader(testResources()).load("drawable", drawableResourceLoader);
    drawableResourceLoader.findDrawableResources(testResources());

    assertNotNull(drawableNodes.get(new ResName(TEST_PACKAGE, "drawable", "rainbow"), ""));
    assertEquals(28, drawableNodes.size());
  }

  @Test
  public void testGetDrawable_rainbow() throws Exception {
    assertNotNull(Robolectric.getShadowApplication().getResources().getDrawable(R.drawable.rainbow));
  }

  @Test
  public void testGetDrawable_shouldWorkWithSystem() throws Exception {
    assertNotNull(resources.getDrawable(android.R.drawable.ic_popup_sync));
  }

  @Test
  public void testGetDrawable_red() throws Exception {
    assertNotNull(Resources.getSystem().getDrawable(android.R.drawable.ic_menu_help));
  }

  @Test
  public void testDrawableTypes() {
    assertThat(resources.getDrawable(R.drawable.l7_white)).isInstanceOf(BitmapDrawable.class);
    assertThat(resources.getDrawable(R.drawable.l0_red)).isInstanceOf(BitmapDrawable.class);
    assertThat(resources.getDrawable(R.drawable.nine_patch_drawable)).isInstanceOf(NinePatchDrawable.class);
    assertThat(resources.getDrawable(R.drawable.rainbow)).isInstanceOf(LayerDrawable.class);
  }

  @Test
  public void testLayerDrawable() {
    Resources resources = Robolectric.getShadowApplication().getResources();
    Drawable drawable = resources.getDrawable(R.drawable.rainbow);
    assertThat(drawable).isInstanceOf(LayerDrawable.class);
    assertEquals(8, ((LayerDrawable) drawable).getNumberOfLayers());

    Configuration configuration = new Configuration();
    shadowOf(configuration).overrideQualifiers("xlarge");
    resources.updateConfiguration(configuration, new DisplayMetrics());

    assertEquals(6, ((LayerDrawable) resources.getDrawable(R.drawable.rainbow)).getNumberOfLayers());
  }

  @Ignore("badly broken right now") @Test
  public void testStateListDrawable() {
    Drawable drawable = resources.getDrawable(R.drawable.state_drawable);
    assertThat(drawable).isInstanceOf(StateListDrawable.class);
    ShadowStateListDrawable shDrawable = shadowOf((StateListDrawable) drawable);
    assertThat(shDrawable.getResourceIdForState(android.R.attr.state_selected)).isEqualTo(R.drawable.l0_red);
    assertThat(shDrawable.getResourceIdForState(android.R.attr.state_pressed)).isEqualTo(R.drawable.l1_orange);
    assertThat(shDrawable.getResourceIdForState(android.R.attr.state_focused)).isEqualTo(R.drawable.l2_yellow);
    assertThat(shDrawable.getResourceIdForState(android.R.attr.state_checkable)).isEqualTo(R.drawable.l3_green);
    assertThat(shDrawable.getResourceIdForState(android.R.attr.state_checked)).isEqualTo(R.drawable.l4_blue);
    assertThat(shDrawable.getResourceIdForState(android.R.attr.state_enabled)).isEqualTo(R.drawable.l5_indigo);
    assertThat(shDrawable.getResourceIdForState(android.R.attr.state_window_focused)).isEqualTo(R.drawable.l6_violet);
    assertThat(shDrawable.getResourceIdForState(android.R.attr.state_active)).isEqualTo(R.drawable.l7_white);
  }

  @Test @Ignore("this seems to be wrong...")
  public void shouldCreateAnims() throws Exception {
    assertInstanceOf(AnimationDrawable.class, resources.getDrawable(R.anim.test_anim_1));
  }

  @Test
  public void shouldCreateAnimators() throws Exception {
    Animator animator = AnimatorInflater.loadAnimator(application, R.animator.spinning);
    assertInstanceOf(Animator.class, animator);
  }

  @Test
  public void shouldCreateAnimsAndColors() throws Exception {
    assertInstanceOf(ColorDrawable.class, resources.getDrawable(R.color.grey42));
  }
}
