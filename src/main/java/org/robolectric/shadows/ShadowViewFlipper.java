package org.robolectric.shadows;

import android.widget.ViewFlipper;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@Implements(value = ViewFlipper.class)
public class ShadowViewFlipper extends ShadowViewAnimator {
    @RealObject
    protected ViewFlipper realObject;

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
