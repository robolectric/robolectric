package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.R;
import android.app.Activity;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowAnimationUtilsTest {

  @Test
  public void loadAnimation_shouldCreateAnimation() {
    assertThat(AnimationUtils.loadAnimation(Robolectric.setupActivity(Activity.class), R.anim.fade_in)).isNotNull();
  }

  @Test
  public void loadLayoutAnimation_shouldCreateAnimation() {
    assertThat(AnimationUtils.loadLayoutAnimation(Robolectric.setupActivity(Activity.class), 1)).isNotNull();
  }

  @Test
  public void getLoadedFromResourceId_forAnimationController_shouldReturnAnimationResourceId() {
    final LayoutAnimationController anim = AnimationUtils.loadLayoutAnimation(Robolectric.setupActivity(Activity.class), R.anim.fade_in);
    assertThat(Shadows.shadowOf(anim).getLoadedFromResourceId()).isEqualTo(R.anim.fade_in);
  }
}
