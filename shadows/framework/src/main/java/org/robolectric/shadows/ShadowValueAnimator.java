package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.animation.AnimationHandler;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
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
  private static boolean animatorsEnabled = true;
  private static ContentResolver contentResolver;

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
    animatorsEnabled = true;
    if (contentResolver != null) {
      ContentResolver resolver = context.getContentResolver();
      Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0);
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

  @Implementation(minSdk = VERSION_CODES.O)
  protected static boolean areAnimatorsEnabled() {
    return animatorsEnabled;
  }

  /**
   * Sets the value for {@link ValueAnimator#areAnimatorsEnabled()}.
   *
   * <p>This supports older versions of Android and legacy checks which use {@link
   * Settings.Global#ANIMATOR_DURATION_SCALE}.
   */
  public static void setAreAnimatorsEnabled(Context context, boolean enabled) {
    animatorsEnabled = enabled;

    int value = enabled ? 1 : 0;
    if (contentResolver == null) {
      contentResolver = context.getContentResolver();
    }
    Settings.Global.putFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, value);
  }
}
