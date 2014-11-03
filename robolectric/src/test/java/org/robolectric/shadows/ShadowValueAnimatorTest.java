package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import android.animation.ValueAnimator;
import android.app.Application;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowValueAnimatorTest {

  final Application application = Robolectric.application;

  @Test
  public void testOfIntPassesArgumentsToAnimator() {
    ValueAnimator valueAnimator = ShadowValueAnimator.ofInt(1,4,6);
    valueAnimator.start();

    assertThat(valueAnimator.getAnimatedValue()).isEqualTo(6);
  }
}
