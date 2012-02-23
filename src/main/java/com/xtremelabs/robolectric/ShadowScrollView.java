package com.xtremelabs.robolectric;

import android.widget.ScrollView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowFrameLayout;

@Implements(ScrollView.class)
public class ShadowScrollView extends ShadowFrameLayout {
    @Implementation
    public void smoothScrollTo(int x, int y) {
        scrollTo(x, y);
    }
}
