package org.robolectric;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import org.robolectric.shadows.ShadowAnimator;
import org.robolectric.shadows.ShadowObjectAnimator;
import org.robolectric.shadows.ShadowValueAnimator;

public class RobolectricShadowOfLevel16 {

  public static ShadowAnimator shadowOf(Animator instance) {
    return (ShadowAnimator) Robolectric.shadowOf_(instance);
  }

  public static ShadowObjectAnimator shadowOf(ObjectAnimator instance) {
    return (ShadowObjectAnimator) Robolectric.shadowOf_(instance);
  }

  public static ShadowValueAnimator shadowOf(ValueAnimator instance) {
    return (ShadowValueAnimator) Robolectric.shadowOf_(instance);
  }
}
