package com.xtremelabs.robolectric.shadows;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;
import org.robolectric.shadows.ShadowAnimator;

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
