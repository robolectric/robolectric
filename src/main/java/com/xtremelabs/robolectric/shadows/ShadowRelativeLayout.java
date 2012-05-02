package com.xtremelabs.robolectric.shadows;

import android.widget.RelativeLayout;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(RelativeLayout.class)
public class ShadowRelativeLayout extends ShadowViewGroup {

    public ShadowRelativeLayout() {
        setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
    }

}
