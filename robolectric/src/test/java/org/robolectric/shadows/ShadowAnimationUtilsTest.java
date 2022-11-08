package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.R;
import android.app.Activity;
import android.view.animation.AnimationUtils;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowAnimationUtilsTest {

  @Test
  public void loadAnimation_shouldCreateAnimation() {
    assertThat(
            AnimationUtils.loadAnimation(Robolectric.setupActivity(Activity.class), R.anim.fade_in))
        .isNotNull();
  }

  @Test
  public void loadLayoutAnimation_shouldCreateAnimation() {
    assertThat(AnimationUtils.loadLayoutAnimation(Robolectric.setupActivity(Activity.class), 1))
        .isNotNull();
  }
}
