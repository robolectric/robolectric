package org.robolectric.android;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.RuntimeEnvironment.application;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.VectorDrawable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class) // todo: @Config(sdk=ALL_SDKS) or something
public class DrawableResourceLoaderTest {
  private Resources resources;

  @Before
  public void setup() throws Exception {
    resources = RuntimeEnvironment.application.getResources();
  }

  @Test
  public void testGetDrawable_rainbow() throws Exception {
    assertNotNull(RuntimeEnvironment.application.getResources().getDrawable(R.drawable.rainbow));
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
    assertThat(resources.getDrawable(R.drawable.an_image_or_vector)).isInstanceOf(BitmapDrawable.class);
  }

  @Test @Config(qualifiers = "anydpi", minSdk = LOLLIPOP)
  public void testVectorDrawableType() {
    assertThat(resources.getDrawable(R.drawable.an_image_or_vector)).isInstanceOf(VectorDrawable.class);
  }

  @Test
  @Config(qualifiers = "xlarge")
  public void testLayerDrawable_xlarge() {
    assertEquals(6, ((LayerDrawable) RuntimeEnvironment.application.getResources().getDrawable(R.drawable.rainbow)).getNumberOfLayers());
  }

  @Test
  public void testLayerDrawable() {
    assertEquals(8, ((LayerDrawable) RuntimeEnvironment.application.getResources().getDrawable(R.drawable.rainbow)).getNumberOfLayers());
  }

  @Test
  public void shouldCreateAnimators() throws Exception {
    Animator animator = AnimatorInflater.loadAnimator(application, R.animator.spinning);
    assertThat(animator).isInstanceOf((Class<? extends Animator>) Animator.class);
  }

  @Test
  public void shouldCreateAnimsAndColors() throws Exception {
    assertThat(resources.getDrawable(R.color.grey42)).isInstanceOf((Class<? extends android.graphics.drawable.Drawable>) ColorDrawable.class);
  }
}
