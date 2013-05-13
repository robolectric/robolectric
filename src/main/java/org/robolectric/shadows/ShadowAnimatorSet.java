package org.robolectric.shadows;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(AnimatorSet.class)
public class ShadowAnimatorSet extends ShadowAnimator {
  @RealObject
  private AnimatorSet realObject;
  private Animator[] childAnimators;

  @Implementation
  public void playTogether(Animator... items) {
    childAnimators = items;
  }

  @Implementation
  public void start() {
    for (Animator childAnimator : childAnimators) {
      childAnimator.setDuration(duration);
      childAnimator.start();
    }
  }

  @Implementation
  public AnimatorSet setDuration(long duration) {
    this.duration = duration;
    return realObject;
  }

  @Implementation
  public void setInterpolator(TimeInterpolator interpolator) {
    for (Animator childAnimator : childAnimators) {
      childAnimator.setInterpolator(interpolator);
    }
  }

}
