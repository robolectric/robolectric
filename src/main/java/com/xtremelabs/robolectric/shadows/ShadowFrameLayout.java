package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow for {@link FrameLayout} that simulates its implementation.
 */
@SuppressWarnings("UnusedDeclaration")
@Implements(FrameLayout.class)
public class ShadowFrameLayout extends ShadowViewGroup {

    public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
        setLayoutParams(new ViewGroup.MarginLayoutParams(0, 0));
        super.__constructor__(context, attributeSet, defStyle);
    }
}
