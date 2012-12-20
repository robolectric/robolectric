package com.xtremelabs.robolectric.shadows;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ValueAnimator.class)
public class ShadowValueAnimator extends ShadowAnimator {
    private TimeInterpolator interpolator;

    @Implementation
    public void setInterpolator(TimeInterpolator value) {
        if (value != null) {
            interpolator = value;
        } else {
            interpolator = new LinearInterpolator();
        }
    }

    @Implementation
    public TimeInterpolator getInterpolator() {
        return interpolator;
    }
}
