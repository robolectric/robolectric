package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(LinearLayout.class)
public class ShadowLinearLayout extends ShadowViewGroup {
    private int orientation;

    public ShadowLinearLayout() {
        setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    @Override
    @Implementation
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LinearLayout.LayoutParams(0, 0);
    }

    @Implementation
    public int getOrientation() {
        return orientation;
    }

    @Implementation
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
