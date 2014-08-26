package org.robolectric.shadows;

import android.R;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.util.TestAnimationListener;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class AnimationTest {

  private TestAnimation animation;
  private ShadowAnimation shadow;
  private TestAnimationListener listener;

  @Before
  public void setUp() throws Exception {
    animation = new TestAnimation();
    shadow = shadowOf(animation);
    listener = new TestAnimationListener();
    animation.setAnimationListener(listener);
  }

  @Test
  public void startShouldInvokeStartCallback() throws Exception {
    assertThat(listener.wasStartCalled).isFalse();
    animation.start();
    assertThat(listener.wasStartCalled).isTrue();
    assertThat(listener.wasEndCalled).isFalse();
    assertThat(listener.wasRepeatCalled).isFalse();
  }

  @Test
  public void cancelShouldInvokeEndCallback() throws Exception {
    assertThat(listener.wasEndCalled).isFalse();
    animation.cancel();
    assertThat(listener.wasStartCalled).isFalse();
    assertThat(listener.wasEndCalled).isTrue();
    assertThat(listener.wasRepeatCalled).isFalse();
  }

  @Test
  public void invokeRepeatShouldInvokeRepeatCallback() throws Exception {
    assertThat(listener.wasRepeatCalled).isFalse();
    shadow.invokeRepeat();
    assertThat(listener.wasStartCalled).isFalse();
    assertThat(listener.wasEndCalled).isFalse();
    assertThat(listener.wasRepeatCalled).isTrue();
  }

  @Test
  public void invokeEndShouldInvokeEndCallback() throws Exception {
    assertThat(listener.wasEndCalled).isFalse();
    shadow.invokeEnd();
    assertThat(listener.wasStartCalled).isFalse();
    assertThat(listener.wasEndCalled).isTrue();
    assertThat(listener.wasRepeatCalled).isFalse();
  }

  @Test
  public void invokeEnd_endsTheAnimation() throws Exception {
    shadow.invokeEnd();
    assertThat(animation.hasEnded()).isTrue();
  }

  @Test
  public void cancel_endsTheAnimation() throws Exception {
    animation.cancel();
    assertThat(animation.hasEnded()).isTrue();
  }

  @Test
  public void simulateAnimationEndShouldInvokeApplyTransformationWith1() throws Exception {
    assertThat(animation.interpolatedTime).isEqualTo(0f);
    shadow.invokeEnd();
    assertThat(animation.interpolatedTime).isEqualTo(1f);
  }

  @Test
  public void testHasStarted() throws Exception {
    assertThat(animation.hasStarted()).isFalse();
    animation.start();
    assertThat(animation.hasStarted()).isTrue();
    animation.cancel();
    assertThat(animation.hasStarted()).isFalse();
  }

  @Test
  public void testDuration() throws Exception {
    assertThat(animation.getDuration()).isNotEqualTo(1000l);
    animation.setDuration(1000);
    assertThat(animation.getDuration()).isEqualTo(1000l);
  }

  @Test
  public void testInterpolation() throws Exception {
    assertThat(animation.getInterpolator()).isNull();
    LinearInterpolator i = new LinearInterpolator();
    animation.setInterpolator(i);
    assertThat((LinearInterpolator) animation.getInterpolator()).isSameAs(i);
  }

  @Test
  public void testRepeatCount() throws Exception {
    assertThat(animation.getRepeatCount()).isNotEqualTo(5);
    animation.setRepeatCount(5);
    assertThat(animation.getRepeatCount()).isEqualTo(5);
  }

  @Test
  public void testRepeatMode() throws Exception {
    assertThat(animation.getRepeatMode()).isNotEqualTo(Animation.REVERSE);
    animation.setRepeatMode(Animation.REVERSE);
    assertThat(animation.getRepeatMode()).isEqualTo(Animation.REVERSE);
  }

  @Test
  public void testStartOffset() throws Exception {
    assertThat(animation.getStartOffset()).isNotEqualTo(500l);
    animation.setStartOffset(500l);
    assertThat(animation.getStartOffset()).isEqualTo(500l);
  }

  @Test(expected=IllegalStateException.class)
  public void testNotLoadedFromResourceId() throws Exception {
    shadow.getLoadedFromResourceId();
  }

  @Test
  public void testLoadedFromResourceId() throws Exception {
    shadow.setLoadedFromResourceId(R.anim.fade_in);
    assertThat(shadow.getLoadedFromResourceId()).isEqualTo(R.anim.fade_in);
  }

  private class TestAnimation extends Animation {
    float interpolatedTime;
    Transformation t;

    @Override protected void applyTransformation(float interpolatedTime, Transformation t) {
      this.interpolatedTime = interpolatedTime;
      this.t = t;
    }
  }
}
