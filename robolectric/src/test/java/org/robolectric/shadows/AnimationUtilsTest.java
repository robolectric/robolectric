package org.robolectric.shadows;

import android.R;
import android.app.Activity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AnimationUtilsTest {

  @Test
  public void testLoadAnimation() {
    assertThat(AnimationUtils.loadAnimation(new Activity(), 1)).isNotNull();
  }

  @Test
  public void testLoadAnimationResourceId() {
    Animation anim = AnimationUtils.loadAnimation(new Activity(), R.anim.fade_in);
    assertThat(Robolectric.shadowOf(anim).getLoadedFromResourceId()).isEqualTo(R.anim.fade_in);
  }

  @Test
  public void testLoadLayoutAnimation() {
    assertThat(AnimationUtils.loadLayoutAnimation(new Activity(), 1)).isNotNull();
  }

  @Test
  public void testLoadLayoutAnimationControllerResourceId() {
    LayoutAnimationController layoutAnim = AnimationUtils.loadLayoutAnimation(new Activity(), R.anim.fade_in);
    assertThat(Robolectric.shadowOf(layoutAnim).getLoadedFromResourceId()).isEqualTo(R.anim.fade_in);
  }
}
