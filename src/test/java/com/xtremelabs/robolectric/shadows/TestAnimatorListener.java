package com.xtremelabs.robolectric.shadows;

import android.animation.Animator;

class TestAnimatorListener implements Animator.AnimatorListener {
    boolean endWasCalled;
    boolean startWasCalled;

    @Override
    public void onAnimationStart(Animator animation) {
        startWasCalled = true;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        endWasCalled = true;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }
}
