package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowRenderNodeAnimatorTest {
  private Activity activity;
  private View view;
  private TestListener listener;

  @Before
  public void setUp() {
    activity = Robolectric.setupActivity(Activity.class);
    view = new View(activity);
    activity.setContentView(view);
    listener = new TestListener();
  }

  @Test
  public void normal() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);
    animator.start();

    assertThat(listener.startCount).isEqualTo(1);
    assertThat(listener.endCount).isEqualTo(1);
  }

  @Test
  public void canceled() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);

    Robolectric.getForegroundThreadScheduler().pause();
    animator.start();
    animator.cancel();

    assertThat(listener.startCount).isEqualTo(1);
    assertThat(listener.cancelCount).isEqualTo(1);
    assertThat(listener.endCount).isEqualTo(1);
  }

  @Test
  public void delayed() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.setStartDelay(1000);
    animator.addListener(listener);

    animator.start();

    assertThat(listener.startCount).isEqualTo(1);
    assertThat(listener.endCount).isEqualTo(1);
  }

  @Test
  public void neverStartedCanceled() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);

    animator.cancel();

    assertThat(listener.startCount).isEqualTo(0);
    // This behavior changed between L and L MR1. In older versions, onAnimationCancel and
    // onAnimationEnd would always be called regardless of whether the animation was started.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      assertThat(listener.cancelCount).isEqualTo(0);
      assertThat(listener.endCount).isEqualTo(0);
    } else {
      assertThat(listener.cancelCount).isEqualTo(1);
      assertThat(listener.endCount).isEqualTo(1);
    }
  }

  @Test
  public void neverStartedEnded() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);

    animator.end();

    // This behavior changed between L and L MR1. In older versions, onAnimationEnd would always be
    // called without any guarantee that onAnimationStart had been called first.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      assertThat(listener.startCount).isEqualTo(1);
      assertThat(listener.endCount).isEqualTo(1);
    } else {
      assertThat(listener.startCount).isEqualTo(0);
      assertThat(listener.endCount).isEqualTo(1);
    }
  }

  @Test
  public void doubleCanceled() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);

    Robolectric.getForegroundThreadScheduler().pause();
    animator.start();
    animator.cancel();
    animator.cancel();

    assertThat(listener.startCount).isEqualTo(1);
    assertThat(listener.cancelCount).isEqualTo(1);
    assertThat(listener.endCount).isEqualTo(1);
  }

  @Test
  public void doubleEnded() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);

    Robolectric.getForegroundThreadScheduler().pause();
    animator.start();
    animator.end();
    animator.end();

    assertThat(listener.startCount).isEqualTo(1);
    assertThat(listener.endCount).isEqualTo(1);
  }

  @Test
  public void delayedAndCanceled() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.setStartDelay(1000);
    animator.addListener(listener);

    Robolectric.getForegroundThreadScheduler().pause();
    animator.start();
    animator.cancel();

    // This behavior changed between L and L MR1. In older versions, onAnimationStart gets called
    // *twice* if you cancel a delayed animation before any of its frames run (as both cancel() and
    // onFinished() implement special behavior for STATE_DELAYED, but the state only gets set to
    // finished after onFinished()).
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      assertThat(listener.startCount).isEqualTo(1);
    } else {
      assertThat(listener.startCount).isEqualTo(2);
    }
    assertThat(listener.cancelCount).isEqualTo(1);
    assertThat(listener.endCount).isEqualTo(1);
  }

  private static class TestListener extends AnimatorListenerAdapter {
    public int startCount;
    public int cancelCount;
    public int endCount;

    @Override
    public void onAnimationStart(Animator animation) {
      startCount++;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
      cancelCount++;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
      endCount++;
    }
  }
}
