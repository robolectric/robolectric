package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(FrameLayout.class)
public class ShadowFrameLayout extends ShadowViewGroup {
    private FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0);

    @Implementation
    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        return layoutParams;
    }

    @Override
    protected void setChildLayoutParams(View child) {
        child.setLayoutParams(new FrameLayout.LayoutParams(0, 0));
    }

}
