package com.xtremelabs.robolectric;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import com.xtremelabs.robolectric.shadows.ShadowAnimator;
import com.xtremelabs.robolectric.shadows.ShadowObjectAnimator;
import com.xtremelabs.robolectric.shadows.ShadowValueAnimator;

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
