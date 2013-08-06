package com.xtremelabs.robolectric;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.SearchView;
import com.xtremelabs.robolectric.shadows.*;

public class RobolectricShadowOfLevel16 {

    public static ShadowAnimator shadowOf(Animator instance) {
        return (ShadowAnimator) Robolectric.shadowOf_(instance);
    }

    public static ShadowObjectAnimator shadowOf(ObjectAnimator instance) {
        return (ShadowObjectAnimator) Robolectric.shadowOf_(instance);
    }

    public static ShadowSearchView shadowOf(SearchView instance) {
        return (ShadowSearchView) Robolectric.shadowOf_(instance);
    }

    public static ShadowValueAnimator shadowOf(ValueAnimator instance) {
        return (ShadowValueAnimator) Robolectric.shadowOf_(instance);
    }

    public static ShadowAnimatorSet shadowOf(AnimatorSet instance) {
        return (ShadowAnimatorSet) Robolectric.shadowOf_(instance);
    }
}
