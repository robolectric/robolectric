package org.robolectric.android;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.shadows.ShadowAssetManager.useLegacy;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.VectorDrawable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class DrawableResourceLoaderTest {
  private Resources resources;

  @Before
  public void setup() throws Exception {
    assumeTrue(useLegacy());
    resources = ApplicationProvider.getApplicationContext().getResources();
  }

  @Test
  public void testGetDrawable_rainbow() throws Exception {
    assertNotNull(
        ApplicationProvider.getApplicationContext().getResources().getDrawable(R.drawable.rainbow));
  }

  @Test
  public void testGetDrawableBundle_shouldWorkWithSystem() throws Exception {
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

  @Test @Config(maxSdk = KITKAT_WATCH)
  public void testVectorDrawableType_preVectors() {
    assertThat(resources.getDrawable(R.drawable.an_image_or_vector)).isInstanceOf(BitmapDrawable.class);
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void testVectorDrawableType() {
    assertThat(resources.getDrawable(R.drawable.an_image_or_vector)).isInstanceOf(VectorDrawable.class);
  }

  @Test
  @Config(qualifiers = "land")
  public void testLayerDrawable_xlarge() {
    assertEquals(
        6,
        ((LayerDrawable)
                ApplicationProvider.getApplicationContext()
                    .getResources()
                    .getDrawable(R.drawable.rainbow))
            .getNumberOfLayers());
  }

  @Test
  public void testLayerDrawable() {
    assertEquals(
        8,
        ((LayerDrawable)
                ApplicationProvider.getApplicationContext()
                    .getResources()
                    .getDrawable(R.drawable.rainbow))
            .getNumberOfLayers());
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
