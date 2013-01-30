package org.robolectric.shadows;

import android.animation.Animator;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

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
