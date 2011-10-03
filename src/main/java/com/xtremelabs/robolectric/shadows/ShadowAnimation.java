package com.xtremelabs.robolectric.shadows;

import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ShadowAnimationBridge;
import android.view.animation.Transformation;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

/**
 * Shadow implementation of {@code Animation} that provides support for invoking listener callbacks.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Animation.class)
public class ShadowAnimation {

    private Animation.AnimationListener listener;
    private Interpolator interpolator;
    private boolean startFlag = false;
    private long durationMillis = 0;

    @RealObject
    private Animation realAnimation;

    @Implementation
    public void setAnimationListener(Animation.AnimationListener l) {
        listener = l;
    }

    @Implementation
    public void start() {
        startFlag = true;
        if (listener != null) {
            listener.onAnimationStart(realAnimation);
        }
    }

    @Implementation
    public void cancel() {
        startFlag = false;
        if (listener != null) {
            listener.onAnimationEnd(realAnimation);
        }
    }

    @Implementation
    public boolean hasStarted() {
        return startFlag;
    }

    @Implementation
    public void setDuration(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    @Implementation
    public long getDuration() {
        return durationMillis;
    }

    @Implementation
    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    @Implementation
    public Interpolator getInterpolator() {
        return interpolator;
    }

    /**
     * Non-Android accessor.  Returns most recently set animation listener.
     *
     * @return
     */
    public Animation.AnimationListener getAnimationListener() {
        return listener;
    }

    /**
     * Non-Android accessor.  Use to simulate repeat loops of animation.
     */
    public void invokeRepeat() {
        if (listener != null) {
            listener.onAnimationRepeat(realAnimation);
        }
    }

    /**
     * Non-Android accessor.  Use to simulate end of animation.
     */
    public void invokeEnd() {
        if (listener != null) {
            listener.onAnimationEnd(realAnimation);
        }
        new ShadowAnimationBridge(realAnimation).applyTransformation(1.0f, new Transformation());
    }
}
