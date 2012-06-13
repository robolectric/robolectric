package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(FrameLayout.class)
public class ShadowFrameLayout extends ShadowViewGroup {
    @Override
    protected void setChildLayoutParams(View child) {
        child.setLayoutParams(new FrameLayout.LayoutParams(0, 0));
    }

}
