package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewGroup.LayoutParams.class)
public class ShadowLayoutParams {
    @RealObject private ViewGroup.LayoutParams realLayoutParams;

    public void __constructor__(int w, int h) {
        realLayoutParams.width = w;
        realLayoutParams.height = h;
    }

    public void __constructor__(ViewGroup.LayoutParams source) {
        __constructor__(source.width, source.height);
    }
}
