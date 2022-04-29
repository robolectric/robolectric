package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
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
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN)
public class ShadowValueAnimatorTest {

  @Test
  public void start_shouldRunAnimation() {
    final List<Integer> values = new ArrayList<>();

    final ValueAnimator animator = ValueAnimator.ofInt(0, 10);
    animator.setDuration(1000);
    animator.addUpdateListener(animation -> values.add((int) animation.getAnimatedValue()));
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
  public void test_WhenInfiniteAnimationIsPlayed_AnimationIsOnlyPlayedOnce() {
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

  @Test
  public void setDurationScale_disablesDurations() {
    ShadowValueAnimator.setDurationScale(0);
    ValueAnimator animator = ValueAnimator.ofInt(0, 10);
    animator.setDuration(Duration.ofDays(100).toMillis());
    animator.setRepeatCount(0);
    animator.start();
    // this would time out without the duration scale being set to zero
    shadowMainLooper().runToEndOfTasks();
  }
}
