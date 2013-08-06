package com.xtremelabs.robolectric.shadows;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(AnimatorSet.class)
public class ShadowAnimatorSet extends ShadowAnimator {
    private static AnimatorSet lastStartedSet;
    @RealObject
    private AnimatorSet realObject;
    private Animator[] childAnimators = new Animator[0];

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
        lastStartedSet = realObject;
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

    public int size() {
        return childAnimators.length;
    }

    public static AnimatorSet getLastStartedSet() {
        return lastStartedSet;
    }

    public Animator get(int pos) {
        return childAnimators[pos];
    }
}
