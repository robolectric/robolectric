package org.robolectric.shadows;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.view.View;
import android.view.animation.LinearInterpolator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AnimatorSetTest {
  private Context context;

  @Before
  public void setup() throws Exception {
    context = Robolectric.application;
  }

  @Test
  public void start_withPlayTogether_shouldSetTheInitialValuesOfAllChildAnimators() throws Exception {
    AnimatorSet subject = new AnimatorSet();
    View target = new View(context);
    ObjectAnimator childAnimator1 = ObjectAnimator.ofFloat(target, "alpha", 0.0f, 3.0f);
    ObjectAnimator childAnimator2 = ObjectAnimator.ofFloat(target, "scaleX", 0.5f, 0.0f);
    subject.playTogether(childAnimator1, childAnimator2);
    subject.setDuration(70);

    subject.start();

    assertThat(target.getAlpha()).isEqualTo(0.0f);
    assertThat(target.getScaleX()).isEqualTo(0.5f);
  }

  @Test
  public void startAndWaitForAnimationEnd_withPlayTogether_shouldSetTheFinalValuesOfAllChildAnimators() throws Exception {
    AnimatorSet subject = new AnimatorSet();
    View target = new View(context);
    ObjectAnimator childAnimator1 = ObjectAnimator.ofFloat(target, "alpha", 0.0f, 3.0f);
    ObjectAnimator childAnimator2 = ObjectAnimator.ofFloat(target, "scaleX", 0.5f, 0.0f);
    subject.playTogether(childAnimator1, childAnimator2);
    subject.setDuration(70);

    subject.start();
    Robolectric.idleMainLooper(70);

    assertThat(target.getAlpha()).isEqualTo(3.0f);
    assertThat(target.getScaleX()).isEqualTo(0.0f);
  }

  @Test
  public void setInterpolator_shouldImmediatelySetInterpolatorsOfAllChildren() throws Exception {
    AnimatorSet subject = new AnimatorSet();
    View target = new View(context);
    ObjectAnimator childAnimator = ObjectAnimator.ofFloat(target, "alpha", 0.0f, 3.0f);
    subject.playTogether(childAnimator);
    TimeInterpolator expectedInterpolator = new LinearInterpolator();
    subject.setInterpolator(expectedInterpolator);

    assertThat(childAnimator.getInterpolator()).isSameAs(expectedInterpolator);
  }
}
