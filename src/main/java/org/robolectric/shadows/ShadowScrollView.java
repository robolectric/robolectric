package com.xtremelabs.robolectric.shadows;

import android.widget.ScrollView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ScrollView.class)
public class ShadowScrollView extends ShadowFrameLayout {
    @Implementation
    public void smoothScrollTo(int x, int y) {
        scrollTo(x, y);
    }
}
