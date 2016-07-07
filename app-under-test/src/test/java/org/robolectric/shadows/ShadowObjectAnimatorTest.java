package org.robolectric.shadows;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowObjectAnimatorTest {
  private final AnimatorTarget target = new AnimatorTarget();
  private final Animator.AnimatorListener listener = mock(Animator.AnimatorListener.class);

  @Test
  public void start_shouldRunAnimation() {
    final ObjectAnimator animator = ObjectAnimator.ofInt(target, "transparency", 0, 1, 2, 3, 4);

    Robolectric.getForegroundThreadScheduler().pause();
    animator.setDuration(1000);
    animator.addListener(listener);
    animator.start();

    verify(listener).onAnimationStart(animator);
    assertThat(target.getTransparency()).isEqualTo(0);

    Robolectric.flushForegroundThreadScheduler();

    verify(listener).onAnimationEnd(animator);
    assertThat(target.getTransparency()).isEqualTo(4);
  }

  @SuppressWarnings("unused")
  public static class AnimatorTarget {
    private int transparency;

    public void setTransparency(int transparency) {
      this.transparency = transparency;
    }

    public int getTransparency() {
      return transparency;
    }
  }
}
