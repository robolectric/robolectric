package org.robolectric.shadows;

import android.animation.ObjectAnimator;
import android.animation.AnimatorInflater;
import android.animation.TypeEvaluator;
import android.util.Property;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

import static org.robolectric.RuntimeEnvironment.application;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowObjectAnimatorTest {

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
  public void shouldCreateForFloat_withPropertyValues() {
    View expectedTarget = new View(RuntimeEnvironment.application);
    Property<View, Float> expectedProperty = View.ALPHA;

    ObjectAnimator animator = ObjectAnimator.ofFloat(expectedTarget, expectedProperty, 0.5f, 0.4f);

    assertThat(animator).isNotNull();
    assertThat(animator.getTarget()).isEqualTo(expectedTarget);
    assertThat(animator.getPropertyName()).isEqualTo(expectedProperty.getName());
  }


  @Test
  public void shouldNotThrowExceptionWithMultipleValues() {
    View expectedTarget = new View(RuntimeEnvironment.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(expectedTarget, View.ALPHA, 0.5f, 0.4f, 0.3f);

    assertThat(animator).isNotNull();
    assertThat(animator.getTarget()).isEqualTo(expectedTarget);
    animator.start(); // should not throw an exception
  }

  @Test
  public void shouldCreateForFloatViaInflater() {
    View expectedTarget = new View(RuntimeEnvironment.application);
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
    View target = new View(RuntimeEnvironment.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0.5f, 0.4f);
    animator.setDuration(1000);

    animator.start();
    assertThat(target.getTranslationX()).isEqualTo(0.5f);
    ShadowLooper.idleMainLooper(999);
    assertThat(target.getTranslationX()).isNotEqualTo(0.4f);
    ShadowLooper.idleMainLooper(1);
    assertThat(target.getTranslationX()).isEqualTo(0.4f);
  }

  @Test
  public void intAnimator_shouldSetTheStartingAndEndingValues() throws Exception {
    View target = new View(RuntimeEnvironment.application);
    ObjectAnimator animator = ObjectAnimator.ofInt(target, "bottom", 1, 4);
    animator.setDuration(1000);

    animator.start();
    assertThat(target.getBottom()).isEqualTo(1);
    ShadowLooper.idleMainLooper(1000);
    assertThat(target.getBottom()).isEqualTo(4);
  }

  @Test
  public void objectAnimator_shouldSetTheStartingAndEndingValues() throws Exception {
    ValueObject object = new ValueObject();
    ObjectAnimator animator = ObjectAnimator.ofObject(object, "value", new TypeEvaluator<String>() {
      @Override
      public String evaluate(float fraction, String startValue, String endValue) {
        if (fraction < 0.5) {
          return startValue;
        } else {
          return endValue;
        }
      }
    }, "human", "replicant", "unicorn");
    animator.setDuration(2000);

    animator.start();

    assertThat(object.getValue()).isEqualTo("human");
    ShadowLooper.idleMainLooper(1000);
    assertThat(object.getValue()).isEqualTo("replicant");
    ShadowLooper.idleMainLooper(1000);
    assertThat(object.getValue()).isEqualTo("unicorn");
  }

  @Test
  public void shouldCallAnimationListenerAtStartAndEnd() throws Exception {
    View target = new View(RuntimeEnvironment.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0.5f, 0.4f);
    animator.setDuration(1);
    TestAnimatorListener startListener = new TestAnimatorListener();
    TestAnimatorListener endListener = new TestAnimatorListener();
    animator.addListener(startListener);
    animator.addListener(endListener);
    animator.start();

    assertThat(startListener.startWasCalled).isTrue();
    assertThat(endListener.endWasCalled).isFalse();
    ShadowLooper.idleMainLooper(1);
    assertThat(endListener.endWasCalled).isTrue();
  }

  @Test
  public void testIsRunning() throws Exception {
    View target = new View(RuntimeEnvironment.application);
    ObjectAnimator expectedAnimator = ObjectAnimator.ofFloat(target, "translationX", 0f, 1f);
    long duration = 70;
    expectedAnimator.setDuration(duration);

    assertThat(expectedAnimator.isRunning()).isFalse();
    expectedAnimator.start();
    assertThat(expectedAnimator.isRunning()).isTrue();
    ShadowLooper.idleMainLooper(duration);
    assertThat(expectedAnimator.isRunning()).isFalse();
  }

  @Test
  public void pauseAndRunEndNotifications() throws Exception {
    View target = new View(RuntimeEnvironment.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", 0.5f, 0.4f);
    animator.setDuration(1);
    TestAnimatorListener endListener = new TestAnimatorListener();
    animator.addListener(endListener);

    animator.start();

    assertThat(endListener.endWasCalled).isFalse();
    ShadowObjectAnimator.pauseEndNotifications();
    ShadowLooper.idleMainLooper(1);
    assertThat(endListener.endWasCalled).isFalse();
    ShadowObjectAnimator.unpauseEndNotifications();
    assertThat(endListener.endWasCalled).isTrue();
  }

  @Test
  public void animatesMultipleKeyFrames() throws Exception {
    View target = new View(RuntimeEnvironment.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "alpha", 0f, 1f, 0.5f, 1f);
    animator.setDuration(3000);

    animator.start();

    assertThat(target.getAlpha()).isEqualTo(0f);
    ShadowLooper.idleMainLooper(1000);
    assertThat(target.getAlpha()).isEqualTo(1f);
    ShadowLooper.idleMainLooper(1000);
    assertThat(target.getAlpha()).isEqualTo(0.5f);
    ShadowLooper.idleMainLooper(1000);
    assertThat(target.getAlpha()).isEqualTo(1f);
  }

  @Test
  public void animatesSingleKeyFrame() throws Exception {
    View target = new View(RuntimeEnvironment.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "alpha", 0.4f);
    animator.setDuration(100);

    animator.start();

    assertThat(target.getAlpha()).isEqualTo(1f);
    ShadowLooper.idleMainLooper(100);
    assertThat(target.getAlpha()).isEqualTo(0.4f);
  }

  @Test
  public void cancel_cancelsAnimation() throws Exception {
    View target = new View(RuntimeEnvironment.application);
    ObjectAnimator animator = ObjectAnimator.ofFloat(target, "alpha", 0.4f);
    animator.setDuration(100);

    animator.start();
    assertThat(animator.isRunning()).isTrue();

    animator.cancel();
    assertThat(animator.isRunning()).isFalse();
  }

  public static class ValueObject {
    private String value;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
