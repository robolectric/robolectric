package org.robolectric.shadows;

import android.R;
import android.app.Activity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowAnimationUtilsTest {

  @Test
  public void loadAnimation_shouldCreateAnimation() {
    assertThat(AnimationUtils.loadAnimation(new Activity(), 1)).isNotNull();
  }

  @Test
  public void getLoadedFromResourceId_forAnimation_shouldReturnAnimationResourceId() {
    final Animation anim = AnimationUtils.loadAnimation(new Activity(), R.anim.fade_in);
    assertThat(Shadows.shadowOf(anim).getLoadedFromResourceId()).isEqualTo(R.anim.fade_in);
  }

  @Test
  public void loadLayoutAnimation_shouldCreateAnimation() {
    assertThat(AnimationUtils.loadLayoutAnimation(new Activity(), 1)).isNotNull();
  }

  @Test
  public void getLoadedFromResourceId_forAnimationController_shouldReturnAnimationResourceId() {
    final LayoutAnimationController anim = AnimationUtils.loadLayoutAnimation(new Activity(), R.anim.fade_in);
    assertThat(Shadows.shadowOf(anim).getLoadedFromResourceId()).isEqualTo(R.anim.fade_in);
  }
}
