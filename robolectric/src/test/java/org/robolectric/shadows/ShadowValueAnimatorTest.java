package org.robolectric.shadows;

import android.animation.ValueAnimator;
import android.os.Looper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
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

  @Test
// It would be nice if timeout worked properly; however internally JUnit sets
// up a separate thread when timeout is used, which violates some assumptions
// that the rest of Robolectric makes about the test thread being invariant.
//  @Test(timeout = 1000)
  public void test_WhenInfiniteAnimationIsPlayed_AnimationIsOnlyPlayedOnce() throws InterruptedException {
    ShadowChoreographer.setFrameInterval(100 * TimeUtils.NANOS_PER_MS);
    final ValueAnimator animator = ValueAnimator.ofInt(0, 10);
    animator.setDuration(200);
    animator.setRepeatCount(ValueAnimator.INFINITE);

    animator.start();

    Thread flush = new Thread("test_WhenInfiniteAnimationIsPlayed_AnimationIsOnlyPlayedOnce") {
      @Override
      public void run() {
        Robolectric.flushForegroundThreadScheduler();
      }
    };
    flush.start();
    flush.join(1000);
    assertThat(animator.isRunning()).isFalse();
  }
}
