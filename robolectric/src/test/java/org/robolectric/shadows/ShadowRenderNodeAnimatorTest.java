package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.view.View;
import android.view.ViewAnimationUtils;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;

@RunWith(AndroidJUnit4.class)
@GraphicsMode(Mode.LEGACY)
public class ShadowRenderNodeAnimatorTest {
  private Activity activity;
  private View view;
  private TestListener listener;

  @Before
  public void setUp() {
    activity = Robolectric.buildActivity(Activity.class).setup().get();
    view = new View(activity);
    activity.setContentView(view);
    listener = new TestListener();
  }

  @Test
  public void normal() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);
    animator.start();

    shadowMainLooper().idle();
    assertThat(listener.startCount).isEqualTo(1);
    assertThat(listener.endCount).isEqualTo(1);
  }

  @Test
  public void canceled() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);

    shadowMainLooper().pause();
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

    shadowMainLooper().idle();
    assertThat(listener.startCount).isEqualTo(1);
    assertThat(listener.endCount).isEqualTo(1);
  }

  @Test
  public void neverStartedCanceled() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);

    animator.cancel();

    assertThat(listener.startCount).isEqualTo(0);
    assertThat(listener.cancelCount).isEqualTo(0);
    assertThat(listener.endCount).isEqualTo(0);
  }

  @Test
  public void neverStartedEnded() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);

    animator.end();

    shadowMainLooper().idle();

    assertThat(listener.startCount).isEqualTo(1);
    assertThat(listener.endCount).isEqualTo(1);
  }

  @Test
  public void doubleCanceled() {
    Animator animator = ViewAnimationUtils.createCircularReveal(view, 10, 10, 10f, 100f);
    animator.addListener(listener);

    shadowMainLooper().pause();
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

    shadowMainLooper().pause();
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

    shadowMainLooper().pause();
    animator.start();
    animator.cancel();

    assertThat(listener.startCount).isEqualTo(1);

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
