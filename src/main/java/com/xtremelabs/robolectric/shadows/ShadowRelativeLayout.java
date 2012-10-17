package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;

@Implements(RelativeLayout.class)
public class ShadowRelativeLayout extends ShadowViewGroup {

    public void __constructor__(Context context) {
        __constructor__(context, new TestAttributeSet(), 0);
    }

    public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
        setLayoutParams(new ViewGroup.MarginLayoutParams(0, 0));
        super.__constructor__(context, attributeSet, defStyle);
    }
}
