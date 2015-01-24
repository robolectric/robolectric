package org.robolectric.shadows;

import android.animation.ValueAnimator;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(ValueAnimator.class)
public class ShadowValueAnimator extends ShadowAnimator {

  @Resetter
  public static void reset() {
    ValueAnimator.clearAllAnimations();
  }
}
