package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowObjectAnimatorTest {
  private final AnimatorTarget target = new AnimatorTarget();
  private List<String> listenerEvents = new ArrayList<>();

  private final Animator.AnimatorListener listener = new Animator.AnimatorListener() {
    @Override
    public void onAnimationStart(Animator animation) {
      listenerEvents.add("started");
    }

    @Override
    public void onAnimationEnd(Animator animation) {
      listenerEvents.add("ended");
    }

    @Override
    public void onAnimationCancel(Animator animation) {
      listenerEvents.add("cancelled");
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
      listenerEvents.add("repeated");
    }
  };

  @Test
  public void start_shouldRunAnimation() {
    final ObjectAnimator animator = ObjectAnimator.ofInt(target, "transparency", 0, 1, 2, 3, 4);

    Robolectric.getForegroundThreadScheduler().pause();
    animator.setDuration(1000);
    animator.addListener(listener);
    animator.start();

    assertThat(listenerEvents).containsExactly("started");
    assertThat(target.getTransparency()).isEqualTo(0);

    Robolectric.flushForegroundThreadScheduler();

    assertThat(listenerEvents).containsExactly("started", "ended");
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
