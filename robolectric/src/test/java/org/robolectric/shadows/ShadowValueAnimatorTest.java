package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Ordering;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;

@RunWith(AndroidJUnit4.class)
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

  @Test
  public void viewPropertyAnimator_attachedView_listenerCalled() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    View view = new View(activity);
    activity.setContentView(view);
    CountingListener listener = new CountingListener();

    view.animate().setStartDelay(500).alpha(0.5f).setDuration(250).setListener(listener);
    shadowMainLooper().idleFor(Duration.ofSeconds(1));

    assertThat(listener.starts).isEqualTo(1);
    assertThat(listener.ends).isEqualTo(1);
    assertThat(view.getAlpha()).isEqualTo(0.5f);
  }

  @Test
  public void viewPropertyAnimator_detachedView_startsOnceAttached() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    View view = new View(activity);
    CountingListener listener = new CountingListener();

    view.animate().alpha(0.5f).setDuration(250).setListener(listener);
    shadowMainLooper().idleFor(Duration.ofSeconds(1));
    assertThat(listener.starts).isEqualTo(0);

    activity.setContentView(view);
    shadowMainLooper().idleFor(Duration.ofSeconds(1));
    assertThat(listener.starts).isEqualTo(1);
    assertThat(listener.ends).isEqualTo(1);
  }

  private static class CountingListener extends AnimatorListenerAdapter {
    int starts;
    int ends;

    @Override
    public void onAnimationStart(Animator animation) {
      starts++;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
      ends++;
    }
  }
}
