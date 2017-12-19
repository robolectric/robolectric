package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.animation.AnimationHandler;
import android.animation.ValueAnimator;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(ValueAnimator.class)
public class ShadowValueAnimator {

  @RealObject
  private ValueAnimator realObject;

  private int actualRepeatCount;

  @Resetter
  public static void reset() {
    /* ValueAnimator.sAnimationHandler is a static thread local that otherwise would survive between
     * tests. The AnimationHandler.mAnimationScheduled is set to true when the scheduleAnimation() is
     * called and the reset to false when run() is called by the Choreographer. If an animation is
     * already scheduled, it will not post to the Choreographer. This is a problem if a previous
     * test leaves animations on the Choreographers callback queue without running them as it will
     * cause the AnimationHandler not to post a callback. We reset the thread local here so a new
     * one will be created for each test with a fresh state.
     */
    if (RuntimeEnvironment.getApiLevel() >= N) {
      ThreadLocal<AnimationHandler> animatorHandlerTL =
          ReflectionHelpers.getStaticField(AnimationHandler.class, "sAnimatorHandler");
      animatorHandlerTL.remove();
    } else {
      ReflectionHelpers.callStaticMethod(ValueAnimator.class, "clearAllAnimations");
      ThreadLocal<AnimationHandler> animatorHandlerTL =
          ReflectionHelpers.getStaticField(ValueAnimator.class, "sAnimationHandler");
      animatorHandlerTL.remove();
    }
  }

  @Implementation
  protected void setRepeatCount(int count) {
    actualRepeatCount = count;
    if (count == ValueAnimator.INFINITE) {
      count = 1;
    }
    directlyOn(realObject, ValueAnimator.class).setRepeatCount(count);
  }

  /**
   * Returns the value that was set as the repeat count. This is otherwise the same
   * as getRepeatCount(), except when the count was set to infinite.
   *
   * @return Repeat count.
   */
  public int getActualRepeatCount() {
    return actualRepeatCount;
  }
}
