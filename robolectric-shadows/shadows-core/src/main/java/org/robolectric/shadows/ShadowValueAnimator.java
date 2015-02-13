package org.robolectric.shadows;

import android.animation.ValueAnimator;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(ValueAnimator.class)
public class ShadowValueAnimator {

  @Resetter
  public static void reset() {
    ValueAnimator.clearAllAnimations();
    /* ValueAnimator.sAnimationHandler is a static thread local that otherwise would survive between
     * tests. The AnimationHandler.mAnimationScheduled is set to true when the scheduleAnimation() is
     * called and the reset to false when run() is called by the Choreographer. If an animation is
     * already scheduled, it will not post to the Choreographer. This is a problem if a previous
     * test leaves animations on the Choreographers callback queue without running them as it will
     * cause the AnimationHandler not to post a callback. We reset the thread local here so a new
     * one will be created for each test with a fresh state.
     */
    ReflectionHelpers.setStaticField(ValueAnimator.class, "sAnimationHandler", new ThreadLocal<>());
  }
}
