package com.xtremelabs.robolectric.shadows;

import android.widget.ViewFlipper;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

/**
 * ViewFlipper
 */
@Implements(ViewFlipper.class)
public class ShadowViewFlipper extends ShadowViewAnimator {
    @RealObject
    protected ViewFlipper realObject;

    /** flipping */
    protected boolean isFlipping;

    @Implementation
    public void startFlipping() {
        this.isFlipping = true;
    }

    @Implementation
    public void stopFlipping() {
        this.isFlipping = false;
    }

    @Implementation
    public boolean isFlipping() {
        return isFlipping;
    }
}
