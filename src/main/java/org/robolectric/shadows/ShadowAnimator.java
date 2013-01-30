package com.xtremelabs.robolectric.shadows;

import android.animation.Animator;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Animator.class)
public class ShadowAnimator {
    @RealObject
    private Animator realObject;
    private List<Animator.AnimatorListener> listeners = new ArrayList<Animator.AnimatorListener>();

    protected void notifyStart() {
        for (Animator.AnimatorListener listener : listeners) {
            listener.onAnimationStart(realObject);
        }
    }

    protected void notifyEnd() {
        for (Animator.AnimatorListener listener : listeners) {
            listener.onAnimationEnd(realObject);
        }
    }

    @Implementation
    public void addListener(Animator.AnimatorListener listener) {
        listeners.add(listener);
    }
}
