package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.animation.ValueAnimator;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Ordering;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.util.TimeUtils;

@RunWith(AndroidJUnit4.class)
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

    assertThat(values).isInOrder(Ordering.natural());
  }

  @Test
  public void test_WithInfiniteRepeatCount_CountIsSetToOne() {
    final ValueAnimator animator = ValueAnimator.ofInt(0, 10);
    animator.setRepeatCount(ValueAnimator.INFINITE);

    assertThat(Shadows.shadowOf(animator).getActualRepeatCount()).isEqualTo(ValueAnimator.INFINITE);
    assertThat(animator.getRepeatCount()).isEqualTo(1);
  }

  @Test
  public void test_WhenInfiniteAnimationIsPlayed_AnimationIsOnlyPlayedOnce() throws InterruptedException {
    ShadowChoreographer.setFrameInterval(100 * TimeUtils.NANOS_PER_MS);
    final ValueAnimator animator = ValueAnimator.ofInt(0, 10);
    animator.setDuration(200);
    animator.setRepeatCount(ValueAnimator.INFINITE);

    shadowMainLooper().pause();
    animator.start();
    assertThat(animator.isRunning()).isTrue();

    shadowMainLooper().idleFor(Duration.ofMillis(200));
    assertThat(animator.isRunning()).isFalse();
  }

  @Test
  public void animation_setPostFrameCallbackDelay() {
    ShadowChoreographer.setPostFrameCallbackDelay(16);
    ValueAnimator animator = ValueAnimator.ofInt(0, 10);
    animator.setDuration(1000);
    animator.setRepeatCount(0);
    animator.start();
    // without setPostFrameCallbackDelay this would finish the animation. Verify it doesn't, so
    // tests can verify in progress animation state
    shadowMainLooper().idleFor(Duration.ofMillis(16));
    assertThat(animator.isRunning()).isTrue();
    // advance 1000 frames - the duration of the animation
    for (int i = 0; i < 999; i++) {
      shadowMainLooper().idleFor(Duration.ofMillis(16));
    }
    assertThat(animator.isRunning()).isFalse();
  }
}
