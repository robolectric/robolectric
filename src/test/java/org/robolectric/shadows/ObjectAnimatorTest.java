package org.robolectric.shadows;

import android.animation.ObjectAnimator;
import android.animation.AnimatorInflater;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.robolectric.Robolectric.application;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ObjectAnimatorTest {
  @Test
  public void shouldCreateForFloat() throws Exception {
    Object expectedTarget = new Object();
    String propertyName = "expectedProperty";
    ObjectAnimator animator = ObjectAnimator.ofFloat(expectedTarget, propertyName, 0.5f, 0.4f);
    assertThat(animator).isNotNull();
    assertThat(animator.getTarget()).isEqualTo(expectedTarget);
    assertThat(animator.getPropertyName()).isEqualTo(propertyName);
  }

  @Test
  public void shouldCreateForFloatViaInflater() {
    View expectedTarget = new View(Robolectric.application);
    ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(application, R.animator.fade);
    assertThat(animator).isNotNull();
    assertThat(animator.getPropertyName()).isEqualTo("alpha");

    animator.setTarget(expectedTarget);
    assertThat(animator.getTarget()).isEqualTo(expectedTarget);
    animator.start(); // start should not throw an exception
  }

  @Test
  public void shouldSetAndGetDuration() throws Exception {
    Object expectedTarget = new Object();
    String propertyName = "expectedProperty";
    ObjectAnimator animator = ObjectAnimator.ofFloat(expectedTarget, propertyName, 0.5f, 0.4f);

    assertThat(animator.setDuration(2876)).isEqualTo(animator);
    assertThat(animator.getDuration()).isEqualTo(2876l);
  }

  @Test
  public void floatAnimator_shouldSetTheStartingAndEndingValues() throws Exception {
    View target = new View(Robolectric.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0.5f, 0.4f);
    animator.setDuration(1000);

    animator.start();
    assertThat(target.getTranslationX()).isEqualTo(0.5f);
    Robolectric.idleMainLooper(999);
    assertThat(target.getTranslationX()).isNotEqualTo(0.4f);
    Robolectric.idleMainLooper(1);
    assertThat(target.getTranslationX()).isEqualTo(0.4f);
  }

  @Test
  public void intAnimator_shouldSetTheStartingAndEndingValues() throws Exception {
    View target = new View(Robolectric.application);
    ObjectAnimator animator = ObjectAnimator.ofInt(target, "bottom", 1, 4);
    animator.setDuration(1000);

    animator.start();
    assertThat(target.getBottom()).isEqualTo(1);
    Robolectric.idleMainLooper(1000);
    assertThat(target.getBottom()).isEqualTo(4);
  }

  @Test
  public void shouldCallAnimationListenerAtStartAndEnd() throws Exception {
    View target = new View(Robolectric.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0.5f, 0.4f);
    animator.setDuration(1);
    TestAnimatorListener startListener = new TestAnimatorListener();
    TestAnimatorListener endListener = new TestAnimatorListener();
    animator.addListener(startListener);
    animator.addListener(endListener);
    animator.start();

    assertThat(startListener.startWasCalled).isTrue();
    assertThat(endListener.endWasCalled).isFalse();
    Robolectric.idleMainLooper(1);
    assertThat(endListener.endWasCalled).isTrue();
  }

  @Test
  public void getAnimatorsFor_shouldReturnAMapOfAnimatorsCreatedForTarget() throws Exception {
    View target = new View(Robolectric.application);
    ObjectAnimator expectedAnimator = ObjectAnimator.ofFloat(target, "translationX", 0f, 1f);

    assertThat(ShadowObjectAnimator.getAnimatorsFor(target).get("translationX")).isSameAs(expectedAnimator);
  }

  @Test
  public void testIsRunning() throws Exception {
    View target = new View(Robolectric.application);
    ObjectAnimator expectedAnimator = ObjectAnimator.ofFloat(target, "translationX", 0f, 1f);
    long duration = 70;
    expectedAnimator.setDuration(duration);

    assertThat(expectedAnimator.isRunning()).isFalse();
    expectedAnimator.start();
    assertThat(expectedAnimator.isRunning()).isTrue();
    Robolectric.idleMainLooper(duration);
    assertThat(expectedAnimator.isRunning()).isFalse();
  }

  @Test
  public void pauseAndRunEndNotifications() throws Exception {
    View target = new View(Robolectric.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0.5f, 0.4f);
    animator.setDuration(1);
    TestAnimatorListener endListener = new TestAnimatorListener();
    animator.addListener(endListener);

    animator.start();

    assertThat(endListener.endWasCalled).isFalse();
    ShadowObjectAnimator.pauseEndNotifications();
    Robolectric.idleMainLooper(1);
    assertThat(endListener.endWasCalled).isFalse();
    ShadowObjectAnimator.unpauseEndNotifications();
    assertThat(endListener.endWasCalled).isTrue();
  }

  @Test
  public void animatesMultipleKeyFrames() throws Exception {
    View target = new View(Robolectric.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "alpha", 0f, 1f, 0.5f, 1f);
    animator.setDuration(3000);

    animator.start();

    assertThat(target.getAlpha()).isEqualTo(0f);
    Robolectric.idleMainLooper(1000);
    assertThat(target.getAlpha()).isEqualTo(1f);
    Robolectric.idleMainLooper(1000);
    assertThat(target.getAlpha()).isEqualTo(0.5f);
    Robolectric.idleMainLooper(1000);
    assertThat(target.getAlpha()).isEqualTo(1f);
  }

  @Test
  public void animatesSingleKeyFrame() throws Exception {
    View target = new View(Robolectric.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "alpha", 0.4f);
    animator.setDuration(100);

    animator.start();

    assertThat(target.getAlpha()).isEqualTo(1f);
    Robolectric.idleMainLooper(100);
    assertThat(target.getAlpha()).isEqualTo(0.4f);
  }
}
