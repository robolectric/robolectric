package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewGroup.LayoutParams.class)
public class ShadowLayoutParams {
    private final ViewGroup.LayoutParams realLayoutParams;

    public ShadowLayoutParams(ViewGroup.LayoutParams realLayoutParams) {
        this.realLayoutParams = realLayoutParams;
    }

    public void __constructor__(int w, int h) {
        realLayoutParams.width = w;
        realLayoutParams.height = h;
    }
}
