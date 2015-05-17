package org.robolectric.shadows;

import android.animation.ValueAnimator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowValueAnimatorTest {

  @Test
  public void start_shouldRunAnimation() {
    final List<Integer> values = new ArrayList<>();

    ShadowChoreographer.setFrameInterval(100 * TimeUtils.NANOS_PER_MS);

    final ValueAnimator animator = ValueAnimator.ofInt(0, 10);
    animator.setDuration(1000);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        values.add((int) animation.getAnimatedValue());
      }
    });
    animator.start();

    Robolectric.flushForegroundThreadScheduler();

    assertThat(values).containsExactly(0, 0, 0, 0, 2, 3, 5, 6, 7, 9, 9, 10);
  }

  @Test
  public void test_WithInfiniteRepeatCount_CountIsSetToOne() {
    final ValueAnimator animator = ValueAnimator.ofInt(0, 10);
    animator.setRepeatCount(ValueAnimator.INFINITE);

    assertThat(Shadows.shadowOf(animator).getActualRepeatCount()).isEqualTo(ValueAnimator.INFINITE);
    assertThat(animator.getRepeatCount()).isEqualTo(1);
  }

  @Test(timeout = 1000)
  public void test_WhenInfiniteAnimationIsPlayed_AnimationIsOnlyPlayedOnce() {
    ShadowChoreographer.setFrameInterval(100 * TimeUtils.NANOS_PER_MS);

    final ValueAnimator animator = ValueAnimator.ofInt(0, 10);
    animator.setDuration(200);
    animator.setRepeatCount(ValueAnimator.INFINITE);

    animator.start();

    Robolectric.flushForegroundThreadScheduler();
    assertThat(animator.isRunning()).isFalse();
  }
}
