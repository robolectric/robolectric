package org.robolectric.shadows;

import android.animation.ValueAnimator;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(ValueAnimator.class)
public class ShadowValueAnimator extends ShadowAnimator {
    protected long duration;

    // Tested via ObjectAnimatorTest for now
    @Implementation
    public long getDuration() {
        return duration;
    }
}
