package android.animation;

import static com.google.common.truth.Truth.assertThat;

import android.animation.Animator.AnimatorListener;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.ActivityWithoutTheme;

/** Compatibility test for animation-related logic. */
@RunWith(AndroidJUnit4.class)
public final class AnimationTest {

  @Test
  public void propertyValuesHolder() throws Exception {
    PropertyValuesHolder pVHolder = PropertyValuesHolder.ofFloat("x", 100f, 150f);
    PropertyBag object = new PropertyBag();
    assertThat(Modifier.isPrivate(PropertyBag.class.getModifiers())).isTrue();
    ObjectAnimator objAnimator = ObjectAnimator.ofPropertyValuesHolder(object, pVHolder);
    objAnimator.setDuration(500);
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.play(objAnimator);

    CountDownLatch countDownLatch = new CountDownLatch(1);
    objAnimator.addListener(
        new AnimatorListener() {
          @Override
          public void onAnimationEnd(Animator a) {
            countDownLatch.countDown();
          }

          @Override
          public void onAnimationStart(Animator a) {}

          @Override
          public void onAnimationCancel(Animator a) {}

          @Override
          public void onAnimationRepeat(Animator a) {}
        });

    // In Android animations can only run on Looper threads.
    try (ActivityScenario<ActivityWithoutTheme> scenario =
        ActivityScenario.launch(ActivityWithoutTheme.class)) {
      scenario.onActivity(
          activity -> {
            animatorSet.start();
          });
    }

    Espresso.onIdle();
    countDownLatch.await(5, TimeUnit.SECONDS);
    assertThat(object.x).isEqualTo(150f);
  }

  /** Private class with a public member. */
  @SuppressWarnings("unused")
  private static class PropertyBag {
    public float x;

    public void setX(float x) {
      this.x = x;
    }
  }
}
