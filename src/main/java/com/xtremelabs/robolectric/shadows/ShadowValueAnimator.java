package com.xtremelabs.robolectric.shadows;

import android.animation.ValueAnimator;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ValueAnimator.class)
public class ShadowValueAnimator extends ShadowAnimator {
    protected long duration;

    // Tested via ObjectAnimatorTest for now
    @Implementation
    public long getDuration() {
        return duration;
    }
}
