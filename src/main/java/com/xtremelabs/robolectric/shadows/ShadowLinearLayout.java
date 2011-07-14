package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(LinearLayout.class)
public class ShadowLinearLayout extends ShadowViewGroup {
    private LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 0);

    @Implementation
    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        return layoutParams;
    }
}
