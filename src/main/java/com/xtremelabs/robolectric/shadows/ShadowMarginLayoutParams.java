package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.view.ViewGroup;

/**
 * Shadow for {@link ViewGroup.MarginLayoutParams} that simulates its implementation.
 */
@SuppressWarnings("UnusedDeclaration")
@Implements(ViewGroup.MarginLayoutParams.class)
public class ShadowMarginLayoutParams extends ShadowLayoutParams {

    @RealObject
    private ViewGroup.MarginLayoutParams realMarginLayoutParams;

    @Implementation
    public void setMargins(int left, int top, int right, int bottom) {
        realMarginLayoutParams.leftMargin = left;
        realMarginLayoutParams.topMargin = top;
        realMarginLayoutParams.rightMargin = right;
        realMarginLayoutParams.bottomMargin = bottom;
    }
}
